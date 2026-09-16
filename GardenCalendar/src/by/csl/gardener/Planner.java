package by.csl.gardener;

import by.csl.gardener.Task;
import by.csl.gardener.Weather;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Locale;

public class Planner {
    public static final int DEFAULT_WINDOW = 21;
    private static final DecimalFormat DF;
    private final Region region;
    private final Storage store;
    private final Weather weather;

    static {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        decimalFormatSymbols.setDecimalSeparator(',');
        DF = new DecimalFormat("#.##", decimalFormatSymbols);
    }

    public Planner(Storage storage, Weather weather) {
        this.store = storage;
        this.weather = weather;
        this.region = Region.detect(storage.lat(), storage.lon());
    }

    public Region region() {
        return this.region;
    }

        /** Все задачи в окне [today, today + days). */
    public java.util.List<by.csl.gardener.Task> tasks(int days) {
        Calendar start = Dates.today();
        Set<String> plants = store.plants();
        int size = store.plantSize();
        List<Task> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String pid : plants) {
            Plant plant = Plant.byId(pid);
            if (plant == null) continue;
            for (Rule r : Rules.all()) {
                if (!r.plantId.equals(pid)) continue;
                if (region.skips(r)) continue; // регион не нуждается в этой работе
                // по уборке — задача на каждую выбранную группу спелости сортов
                List<String> groups = new ArrayList<>();
                if (Operation.HARVEST.equals(r.op)) {
                    groups.addAll(store.varietyGroups(pid));
                }
                if (groups.isEmpty()) groups.add(null);
                Set<String> doneGroups = new HashSet<>();
                Calendar cursor = (Calendar) start.clone();
                for (int i = 0; i < days && doneGroups.size() < groups.size(); i++) {
                    int y = cursor.get(Calendar.YEAR);
                    int am = cursor.get(Calendar.MONTH) + 1;
                    int ad = cursor.get(Calendar.DAY_OF_MONTH);
                    for (String g : groups) {
                        String gk = g == null ? "" : g;
                        if (doneGroups.contains(gk)) continue;
                        // юг: работы раньше; ранние сорта убираются раньше, поздние позже
                        int varOff = "early".equals(g) ? 10 : "late".equals(g) ? -10 : 0;
                        int shift = region.phenoShiftDays + varOff;
                        Calendar eval = shift == 0 ? cursor : Dates.plusDays(cursor, shift);
                        int em = eval.get(Calendar.MONTH) + 1;
                        int ed = eval.get(Calendar.DAY_OF_MONTH);
                        if (r.applies(em, Rule.decade(ed))) {
                            String key = r.stableId() + ":" + gk + ":" + y + ":" + em
                                    + ":" + Rule.decade(ed);
                            if (seen.add(key)) {
                                out.add(build(r, plant, y, am, ad, size, cursor, g));
                            }
                            doneGroups.add(gk);
                        }
                    }
                    cursor = Dates.plusDays(cursor, 1);
                }
            }
        }
        Collections.sort(out, new Comparator<Task>() {
            @Override
            public int compare(Task a, Task b) {
                Calendar ca = Dates.at(a.year, a.month, a.day);
                Calendar cb = Dates.at(b.year, b.month, b.day);
                int c = ca.compareTo(cb);
                if (c != 0) return c;
                if (a.priority != b.priority) return b.priority - a.priority;
                return a.plantName.compareTo(b.plantName);
            }
        });
        return out;
    }

    private static String groupSuffix(String str) {
        return "early".equals(str) ? " (ранние сорта)" : "late".equals(str) ? " (поздние сорта)" : "";
    }

    private Task build(Rule rule, Plant plant, int i, int i2, int i3, int i4, Calendar calendar, String str) {
        String str2;
        double d;
        Rule rule2 = rule;
        Task task = new Task();
        StringBuilder sb = new StringBuilder();
        sb.append(rule.stableId());
        if (str == null) {
            str2 = "";
        } else {
            str2 = "-" + str;
        }
        sb.append(str2);
        task.id = sb.toString();
        task.plantId = plant.id;
        task.plantName = plant.name;
        task.op = rule2.op;
        task.title = rule2.title + groupSuffix(str);
        task.text = rule2.text;
        task.year = i;
        task.month = i2;
        task.day = i3;
        task.priority = rule2.priority;
        task.rainBlocks = rule2.rainBlocks;
        task.minTemp = rule2.minTemp;
        task.maxTemp = rule2.maxTemp;
        task.solutionL = rule2.solutionL;
        task.currency = this.region.symbol;
        task.window = windowText(i, i2, Rule.decade(i3));
        task.done = this.store.isDone(task.id, i);
        double sizeFactor = Plant.sizeFactor(i4, plant.id);
        for (MatRef matRef : rule2.mats) {
            Material byId = Material.byId(matRef.materialId);
            if (byId == null) continue;
            double amount = amount(matRef, rule2, sizeFactor);
            if (amount <= 0.0d) continue;
            Task.Item item = new Task.Item();
            item.name = byId.name;
            item.pack = byId.pack;
            item.price = this.region.priceOf(byId);
            item.currency = this.region.symbol;
            item.alternative = matRef.alternative;
            item.dose = (matRef.alternative ? "или замена — " : "") + doseText(matRef, rule2);
            item.packs = (int) Math.max(1.0d, Math.ceil(amount / byId.packQty));
            item.need = needText(amount, byId, item.packs);
            task.items.add(item);
        }
        evaluateWeather(task, calendar);
        return task;
    }

    private double amount(MatRef matRef, Rule rule, double d) {
        int i = matRef.mode;
        if (i == 0) {
            double d2 = matRef.rate;
            if (!rule.scaleByPlant) {
                d = 1.0d;
            }
            return d2 * d;
        }
        if (i == 1) {
            double d3 = matRef.rate * (rule.solutionL / 10.0d);
            if (!rule.scaleByPlant) {
                d = 1.0d;
            }
            return d3 * d;
        }
        if (i == 2) {
            double d4 = matRef.rate;
            if (!rule.scaleByPlant) {
                d = 1.0d;
            }
            return d4 * d;
        }
        return matRef.rate;
    }

    private String doseText(MatRef matRef, Rule rule) {
        Material byId = Material.byId(matRef.materialId);
        String str = byId == null ? "" : byId.unit;
        int i = matRef.mode;
        if (i == 0) {
            return num(matRef.rate) + " " + str + " на растение";
        }
        if (i == 1) {
            return num(matRef.rate) + " " + str + " на 10 л раствора";
        }
        if (i == 2) {
            return num(matRef.rate) + " " + str + "/м²";
        }
        return num(matRef.rate) + " " + str;
    }

    private String needText(double d, Material material, int i) {
        String str = num(d) + " " + material.unit;
        if (material.unit.equals("шт")) {
            return i + " шт.";
        }
        if (i > 1) {
            return i + " уп. (" + material.pack + ") = " + str;
        }
        return str + " (упаковка " + material.pack + ")";
    }

    public static String num(double d) {
        if (d >= 100.0d) {
            return String.valueOf(Math.round(d));
        }
        return DF.format(d);
    }

    public static String windowText(int i, int i2, int i3) {
        String str = Dates.MONTHS[i2 - 1];
        if (i3 == 1) {
            return "1–10 " + str;
        }
        if (i3 == 2) {
            return "11–20 " + str;
        }
        return "21–31 " + str;
    }

    void evaluateWeather(Task task, Calendar calendar) {
        Weather weather = this.weather;
        if (weather == null || !weather.hasForecast()) {
            task.weatherState = 4;
            task.weatherNote = "Прогноз не загружен — обновите погоду";
            return;
        }
        Weather.Day dayFor = this.weather.dayFor(task.year, task.month, task.day);
        if (dayFor == null) {
            task.weatherState = 4;
            task.weatherNote = "Дата за пределами прогноза — ориентируйтесь по климатической норме";
            return;
        }
        if (Operation.WATER.equals(task.op)) {
            evaluateWater(task, dayFor);
            return;
        }
        if (task.rainBlocks && dayFor.rainExpected()) {
            task.weatherState = 1;
            Locale locale = Locale.US;
            Object[] objArr = new Object[2];
            objArr[0] = Double.valueOf(dayFor.workPrecipMm > 0.0d ? dayFor.workPrecipMm : dayFor.precipMm);
            objArr[1] = Double.valueOf(dayFor.workPrecipProb > 0.0d ? dayFor.workPrecipProb : dayFor.precipProb);
            task.weatherNote = String.format(locale, "Осадки %.1f мм (вероятность %.0f%%) — перенести на сухой день", objArr);
            task.suggestDayOffset = nextSuitable(task, 1);
            return;
        }
        if (dayFor.tMin < task.minTemp) {
            task.weatherState = 2;
            task.weatherNote = String.format(Locale.US, "Минимум %.0f °C — работа требует не ниже %.0f °C", Double.valueOf(dayFor.tMin), Double.valueOf(task.minTemp));
            task.suggestDayOffset = nextSuitable(task, 1);
        } else if (task.maxTemp < 90.0d && dayFor.tMax > task.maxTemp) {
            task.weatherState = 3;
            task.weatherNote = String.format(Locale.US, "Максимум %.0f °C — выше допустимых %.0f °C", Double.valueOf(dayFor.tMax), Double.valueOf(task.maxTemp));
            task.suggestDayOffset = nextSuitable(task, 1);
        } else {
            task.weatherState = 0;
            task.weatherNote = dayFor.summary() + " — условия подходят";
        }
    }

    /** Метео-логика полива: осадки на 3 дня вперёд + влажность воздуха; теплица от дождя не зависит. */
    private void evaluateWater(Task task, Weather.Day today) {
        boolean greenhouse = "greenhouse".equals(task.plantId);
        double sum = 0.0d;
        boolean wetDay = false;
        Calendar cursor = Dates.at(task.year, task.month, task.day);
        for (int i = 0; i < 3; i++) {
            Weather.Day day = this.weather.dayFor(cursor.get(Calendar.YEAR), cursor.get(Calendar.MONTH) + 1, cursor.get(Calendar.DAY_OF_MONTH));
            if (day == null) {
                break;
            }
            sum += Math.max(0.0d, day.precipMm);
            if (day.precipMm >= 3.0d && day.precipProb >= 60.0d) {
                wetDay = true;
            }
            cursor = Dates.plusDays(cursor, 1);
        }
        String extra = "";
        if (today.humidity < 40.0d && today.tMax >= 22.0d) {
            extra = String.format(Locale.US, " Воздух сухой (влажность до %.0f%%) — поливайте вечером и мульчируйте.", Double.valueOf(today.humidity));
        } else if (greenhouse && today.humidity > 85.0d) {
            extra = String.format(Locale.US, " Влажность выше %.0f%% — усиленно проветривайте, иначе фитофтора и кладоспориоз.", Double.valueOf(today.humidity));
        }
        if (greenhouse) {
            task.weatherState = 0;
            task.weatherNote = "Теплица не зависит от дождя — полив по состоянию почвы: подсох слой 3–5 см — полейте тёплой водой утром." + extra;
            return;
        }
        if (wetDay && sum >= 10.0d) {
            task.weatherState = 0;
            task.weatherNote = String.format(Locale.US, "По прогнозу %.0f мм осадков за 3 дня — полив можно пропустить, почва увлажнится сама.", Double.valueOf(sum));
            return;
        }
        if (sum < 2.0d && today.tMax >= 22.0d) {
            task.weatherState = 1;
            task.weatherNote = String.format(Locale.US, "Засушливо: за 3 дня лишь %.1f мм осадков, днём до +%.0f °C — полейте сегодня утром или вечером.", Double.valueOf(sum), Double.valueOf(today.tMax)) + extra;
            return;
        }
        task.weatherState = 0;
        task.weatherNote = today.summary() + " — полив по состоянию почвы: сухо на глубине 5 см — поливаем." + extra;
    }

    private int nextSuitable(Task task, int i) {
        while (i <= 9) {
            Calendar plusDays = Dates.plusDays(Dates.at(task.year, task.month, task.day), i);
            Weather.Day dayFor = this.weather.dayFor(plusDays.get(1), plusDays.get(2) + 1, plusDays.get(5));
            if (dayFor == null) {
                return -1;
            }
            if (!(task.rainBlocks && dayFor.rainExpected()) && dayFor.tMin >= task.minTemp && (task.maxTemp >= 90.0d || dayFor.tMax <= task.maxTemp)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public List<Task> tasksOn(int i, int i2, int i3) {
        ArrayList arrayList = new ArrayList();
        for (Task task : tasks(21)) {
            if (task.year == i && task.month == i2 && task.day == i3) {
                arrayList.add(task);
            }
        }
        return arrayList;
    }

    public List<Task> pending(int i) {
        Calendar calendar = Dates.today();
        ArrayList arrayList = new ArrayList();
        for (Task task : tasks(21)) {
            if (!Dates.at(task.year, task.month, task.day).before(calendar) && !task.done) {
                arrayList.add(task);
            }
            if (arrayList.size() >= i) {
                break;
            }
        }
        return arrayList;
    }

    public static class ShopItem {
        public String materialId;
        public String name;
        public String pack;
        public double packQty;
        public int packs;
        public double price;
        public String unit;
        public String currency = "BYN";
        public List<String> usedFor = new ArrayList();

        public double cost() {
            return this.packs * this.price;
        }
    }

    public List<ShopItem> shoppingList(int i) {
        ShopItem shopItem;
        Calendar calendar = Dates.today();
        Calendar plusDays = Dates.plusDays(calendar, i);
        // окно планирования = окну списка покупок (иначе позиции за пределами 21 дня терялись бы)
        List<Task> tasks = tasks(Math.max(i, DEFAULT_WINDOW));
        HashSet hashSet = new HashSet();
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            for (Task.Item item : it.next().items) {
                if (!item.alternative) {
                    hashSet.add(item.name);
                }
            }
        }
        ArrayList arrayList = new ArrayList();
        for (Task task : tasks) {
            Calendar at = Dates.at(task.year, task.month, task.day);
            if (!at.before(calendar) && !at.after(plusDays) && !task.done) {
                for (Task.Item item2 : task.items) {
                    if (item2.price > 0.0d && (!item2.alternative || hashSet.contains(item2.name))) {
                        Material byId = Material.byId(guessId(item2.name));
                        Iterator it2 = arrayList.iterator();
                        while (true) {
                            if (!it2.hasNext()) {
                                shopItem = null;
                                break;
                            }
                            ShopItem shopItem2 = (ShopItem) it2.next();
                            if (shopItem2.name.equals(item2.name)) {
                                shopItem = shopItem2;
                                break;
                            }
                        }
                        if (shopItem == null) {
                            shopItem = new ShopItem();
                            shopItem.materialId = byId == null ? item2.name : byId.id;
                            shopItem.name = item2.name;
                            shopItem.pack = item2.pack;
                            shopItem.price = item2.price;
                            shopItem.currency = item2.currency;
                            shopItem.unit = byId == null ? "" : byId.unit;
                            shopItem.packQty = byId == null ? 1.0d : byId.packQty;
                            arrayList.add(shopItem);
                        }
                        shopItem.packs += item2.packs;
                        if (!shopItem.usedFor.contains(task.plantName)) {
                            shopItem.usedFor.add(task.plantName);
                        }
                    }
                }
            }
        }
        Collections.sort(arrayList, new Comparator<ShopItem>() {
            public int compare(ShopItem shopItem3, ShopItem shopItem4) {
                return Double.compare(shopItem4.cost(), shopItem3.cost());
            }
        });
        return arrayList;
    }

    private String guessId(String str) {
        for (Material material : Material.all()) {
            if (material.name.equals(str)) {
                return material.id;
            }
        }
        return str;
    }

    public int[] stats() {
        List<Task> tasks = tasks(21);
        Calendar calendar = Dates.today();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (Task task : tasks) {
            i++;
            if (task.done) {
                i2++;
            }
            Calendar at = Dates.at(task.year, task.month, task.day);
            if (at.get(6) == calendar.get(6) && at.get(1) == calendar.get(1)) {
                i3++;
            }
            if (task.weatherState > 0 && task.weatherState < 4) {
                i4++;
            }
        }
        return new int[]{i, i2, i3, i4};
    }
}
