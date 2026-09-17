package by.csl.gardener;

import android.content.Context;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * JVM-проверка реального кода приложения (Rules + Planner + Material + Weather).
 * Запускается на хосте с заглушками android.content.*, чтобы убедиться,
 * что планировщик действительно формирует работы и считает количество материалов.
 */
public class LogicTest {

    private static int failures = 0;

    private static void check(String what, boolean ok, String detail) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + what + (detail.isEmpty() ? "" : " — " + detail));
        if (!ok) failures++;
    }

    public static void main(String[] args) throws Exception {
        Context ctx = new Context();
        Storage store = new Storage(ctx);

        Set<String> plants = new HashSet<>();
        plants.add("apple");
        plants.add("pear");
        plants.add("sweet_cherry");
        plants.add("currant_black");
        plants.add("gooseberry");
        plants.add("raspberry");
        plants.add("strawberry");
        plants.add("grape");
        plants.add("tomato");
        plants.add("cucumber");
        plants.add("cabbage");
        plants.add("potato");
        plants.add("greenhouse");
        store.setPlants(plants);
        store.setPlantSize(Plant.SIZE_MEDIUM);

        System.out.println("=== 1. База правил ===");
        int rules = Rules.all().size();
        check("правил агротехники в базе", rules > 80, "найдено " + rules);
        Set<String> covered = new HashSet<>();
        for (Rule r : Rules.all()) covered.add(r.plantId);
        check("правил на каждое выбранное растение", covered.containsAll(plants),
                "покрыто " + covered.size() + " культур из каталога " + Plant.all().size());
        for (Material m : Material.all()) {
            if (m.price < 0) check("цена материала " + m.id, false, "");
        }
        check("все материалы в правилах существуют в каталоге", allMaterialsExist(), "");
        boolean priceKeysOk = true;
        StringBuilder bad = new StringBuilder();
        for (Region rg : Region.allRegions()) {
            for (String key : rg.prices().keySet()) {
                if (Material.byId(key) == null) {
                    priceKeysOk = false;
                    bad.append(rg.id).append(":").append(key).append(" ");
                }
            }
        }
        check("цены регионов ссылаются на существующие материалы", priceKeysOk, bad.toString());

        System.out.println("\n=== 1b. Полив теплицы, закупка и подготовка рассады ===");
        Set<String> waterPlants = new HashSet<>();
        Set<String> buyPlants = new HashSet<>();
        Set<String> prepPlants = new HashSet<>();
        Set<String> feedSeed = new HashSet<>();
        for (Rule r : Rules.all()) {
            if (Operation.WATER.equals(r.op)) waterPlants.add(r.plantId);
            if (Operation.MONITOR.equals(r.op) && r.title.startsWith("Закупка")) buyPlants.add(r.plantId);
            if (Operation.SOIL.equals(r.op) && r.title.startsWith("Подготовка грунта")) prepPlants.add(r.plantId);
            if (Operation.FEED_ROOT.equals(r.op) && r.title.startsWith("Подкормки рассады")) feedSeed.add(r.plantId);
        }
        check("у теплицы есть правило полива", waterPlants.contains("greenhouse"), waterPlants.toString());
        String[] seedlings = {"tomato", "cucumber", "cabbage", "pepper", "eggplant"};
        boolean buyOk = true, prepOk = true, feedOk = true;
        for (String sp : seedlings) {
            if (!buyPlants.contains(sp)) buyOk = false;
            if (!prepPlants.contains(sp)) prepOk = false;
            if (!"tomato".equals(sp) && !feedSeed.contains(sp)) feedOk = false;
        }
        check("напоминания о закупке семян у рассадных культур", buyOk, buyPlants.toString());
        check("подготовка грунта под рассаду у рассадных культур", prepOk, prepPlants.toString());
        check("подкормки рассады до высадки (перец/баклажан/огурец/капуста)", feedOk, feedSeed.toString());

        System.out.println("\n=== 1c. Справочник болезней ===");
        List<Disease> dzAll = DiseaseDb.all(null);
        check("болезней+вредителей в справочнике (≥200)", dzAll.size() >= 200, "найдено " + dzAll.size());
        Map<String, Integer> perPlant = new HashMap<>();
        int badMats = 0, shortCure = 0, noPrev = 0;
        for (Disease dz : dzAll) {
            Integer n = perPlant.get(dz.plantId);
            perPlant.put(dz.plantId, n == null ? 1 : n + 1);
            for (String mid : dz.mats) if (Material.byId(mid) == null) badMats++;
            if (dz.cure == null || dz.cure.length < 3) shortCure++;
            if (dz.spring.isEmpty() || dz.autumn.isEmpty()) noPrev++;
        }
        boolean dzCover = true;
        StringBuilder dzMiss = new StringBuilder();
        for (Plant pl : Plant.all()) {
            Integer n = perPlant.get(pl.id);
            if (n == null || n < 3) { dzCover = false; dzMiss.append(pl.id).append("(").append(n == null ? 0 : n).append(") "); }
        }
        check("≥3 болезни у каждой культуры", dzCover, dzMiss.toString());
        check("препараты справочника есть в каталоге", badMats == 0, "битых ссылок: " + badMats);
        check("лечение ≥3 шагов у каждой болезни", shortCure == 0, "коротких: " + shortCure);
        check("профилактика весна+осень у каждой болезни", noPrev == 0, "пустых: " + noPrev);
        int photos = 0;
        StringBuilder noPhotoList = new StringBuilder();
        for (Disease dz : dzAll) {
            if (dz.image != null && new java.io.File("res/drawable-nodpi/" + dz.image + ".jpg").exists()) photos++;
            else noPhotoList.append(dz.image).append(' ');
        }
        // Храповик: каждая партия фото уменьшает долг; расти снова он не должен.
        int photoDebtMax = 44;
        check("фото есть у болезней и вредителей (долг фото ≤ " + photoDebtMax + ")",
            dzAll.size() - photos <= photoDebtMax,
            "есть " + photos + "/" + dzAll.size() + ", ждут фото: " + noPhotoList);

        // Вредители: полноценные карточки с повреждениями, сезонной профилактикой и лечением
        int pestCount = 0, pestShortCure = 0, pestNoSeason = 0, pestNoPhoto = 0;
        for (Disease dz : dzAll) {
            if (!dz.kind.startsWith("Вредитель")) continue;
            pestCount++;
            if (dz.cure == null || dz.cure.length < 4) pestShortCure++;
            if (dz.spring.isEmpty() || dz.summer.isEmpty() || dz.autumn.isEmpty()) pestNoSeason++;
            if (dz.image == null || !dz.image.startsWith("dzv_")) pestNoPhoto++;
        }
        check("вредителей в справочнике (≥40)", pestCount >= 40, "найдено " + pestCount);
        check("лечение вредителей ≥4 шагов у каждого", pestShortCure == 0, "коротких: " + pestShortCure);
        check("профилактика вредителей весна+лето+осень", pestNoSeason == 0, "пустых сезонов: " + pestNoSeason);
        check("у каждого вредителя назначено фото dzv_*", pestNoPhoto == 0, "без фото: " + pestNoPhoto);

        // Отметки болезней у культур → профилактика в весенних и осенних работах
        Storage stPrev = new Storage(new Context());
        stPrev.setPlants(new HashSet<>(java.util.Arrays.asList("apple")));
        stPrev.setPlantDiseases("apple", new HashSet<>(java.util.Arrays.asList("apple_scab")));
        boolean prSpring = false, prAutumn = false;
        for (Task tPrev : new Planner(stPrev, null).tasks(400)) {
            if (tPrev.id.equals("dzprev:apple_scab:spring")) prSpring = true;
            if (tPrev.id.equals("dzprev:apple_scab:autumn")) prAutumn = true;
        }
        check("отмеченная болезнь даёт задачи профилактики весной и осенью", prSpring && prAutumn,
                "spring=" + prSpring + ", autumn=" + prAutumn);
        stPrev.setPlantDiseases("apple", new HashSet<String>());
        boolean prGone = true;
        for (Task tPrev : new Planner(stPrev, null).tasks(400)) {
            if (tPrev.id.startsWith("dzprev:")) prGone = false;
        }
        check("снятая отметка болезни убирает задачу профилактики", prGone, "");
        Set<String> dzMarks = stPrev.plantDiseases("apple");
        check("отметки болезней хранятся изолированно от списка культур",
                dzMarks.isEmpty() && stPrev.plants().contains("apple"), "");

        // Рекомендации сортов в посадочных работах (регион по GPS)
        Storage stVar = new Storage(new Context());
        stVar.setPlants(new HashSet<>(java.util.Arrays.asList("apple")));
        stVar.setLocation("Лесной", 54.0, 27.68);
        boolean sortBlockBy = false;
        String sortText = "";
        for (Task tv : new Planner(stVar, null).tasks(400)) {
            if (Operation.PLANT.equals(tv.op) && tv.text.contains("Сорта, хорошо зарекомендовавшие")) {
                sortBlockBy = true;
                sortText = tv.text;
                break;
            }
        }
        check("посадочная работа содержит сорта региона (Беларусь)", sortBlockBy && sortText.contains("Алеся"), "");
        check("сорт несёт срок созревания и описание", sortText.contains("летний") || sortText.contains("зима"), "");
        List<Varieties.V> vsCrimea = Varieties.forPlant("peach", "crimea");
        boolean southern = false;
        for (Varieties.V v : vsCrimea) {
            if (v.name.equals("Краснощёкий")) southern = true;
        }
        check("региональный фильтр сортов (Крым → южные сорта)", southern && !Varieties.forPlant("peach", "by").equals(vsCrimea), "");
        String dzJson = DiseaseDb.toJson(dzAll, 1);
        List<Disease> dzBack = DiseaseDb.fromJson(dzJson);
        check("JSON справочника сериализуется и читается (файл обновлений)", dzBack.size() == dzAll.size(), dzBack.size() + " из " + dzAll.size());

        System.out.println("\n=== 2. Погода: реальный запрос Open-Meteo (Минск) ===");
        Weather w;
        try {
            w = Weather.fetch(53.9045, 27.5615, "Минск");
            check("прогноз загружен", w.hasForecast(), "дней: " + w.days.size()
                    + ", сейчас " + (Double.isNaN(w.currentTemp) ? "?" : String.format(Locale.US, "%.1f", w.currentTemp)) + " °C");
            Weather.Day d0 = w.days.get(0);
            check("сегодняшний день прогноза", d0.year >= 2026, d0.year + "-" + d0.month + "-" + d0.day
                    + " " + d0.summary());
            String roundTrip = Weather.fromJson(w.toJson()) != null ? "ok" : "broken";
            check("кэш погоды сериализуется/читается", "ok".equals(roundTrip), roundTrip);
            check("влажность воздуха и почвы в прогнозе", !Double.isNaN(d0.humidity) && !Double.isNaN(d0.soilMoist),
                    String.format(Locale.US, "влажность %.0f%%, влага почвы %.3f м³/м³", d0.humidity, d0.soilMoist));
            Weather.Day dRt = Weather.fromJson(w.toJson()).days.get(0);
            check("влажность переживает кэш", Double.compare(dRt.humidity, d0.humidity) == 0,
                    String.valueOf(dRt.humidity));
        } catch (Exception e) {
            System.out.println("SKIP  сеть недоступна: " + e);
            w = Weather.fromJson("{\"lat\":53.9,\"lon\":27.56,\"city\":\"Минск\",\"fetchedAt\":"
                    + System.currentTimeMillis() + ",\"currentTemp\":18.0,\"currentCode\":1,\"days\":[]}");
        }

        System.out.println("\n=== 2a. Погода: разбор ответа Open-Meteo (офлайн-фикстура) ===");
        String fx = "{\"current\":{\"temperature_2m\":15.4,\"weather_code\":3}"
            + ",\"daily\":{\"time\":[\"2026-09-16\"],\"temperature_2m_max\":[21.0],\"temperature_2m_min\":[11.0]"
            + ",\"precipitation_sum\":[1.2],\"precipitation_probability_max\":[60],\"wind_speed_10m_max\":[12.0],\"weather_code\":[61]}"
            + ",\"hourly\":{\"time\":[\"2026-09-16T08:00\",\"2026-09-16T12:00\",\"2026-09-16T20:00\"]"
            + ",\"precipitation\":[0.5,0.5,1.0],\"precipitation_probability\":[50,60,90]"
            + ",\"relative_humidity_2m\":[70,85,95],\"soil_temperature_6cm\":[14.0,16.5,15.0],\"soil_moisture_0_to_7cm\":[0.30,0.36,0.33]}}";
        try {
            Weather wf = Weather.parse(fx, 53.9, 27.56, "Тест");
            Weather.Day df = wf.days.get(0);
            check("влажность воздуха — суточный максимум из почасовых", Double.compare(df.humidity, 95.0) == 0, String.valueOf(df.humidity));
            check("влага почвы — суточная средняя из почасовых", Math.abs(df.soilMoist - 0.33) < 0.001, String.valueOf(df.soilMoist));
            check("осадки в рабочие часы 8–20", Math.abs(df.workPrecipMm - 1.0) < 0.001 && Double.compare(df.workPrecipProb, 60.0) == 0,
                    df.workPrecipMm + " мм / " + df.workPrecipProb + "%");
            check("URL без daily-параметров влажности (Open-Meteo их отклоняет HTTP 400)",
                    !Weather.buildUrl(53.9, 27.56).contains("relative_humidity_2m_max")
                            && !Weather.buildUrl(53.9, 27.56).contains("soil_moisture_0_to_7cm_max"), "");
            check("минимальный запасной URL задан", Weather.buildUrlMinimal(53.9, 27.56).contains("daily=weather_code"), "");
        } catch (Exception e) {
            check("разбор ответа Open-Meteo (фикстура)", false, String.valueOf(e));
        }

        System.out.println("\n=== 2b. Метео-логика полива (синтетический прогноз) ===");
        Weather wSyn = new Weather();
        wSyn.fetchedAt = System.currentTimeMillis();
        Calendar wBase = Dates.today();
        double[] precip = {0.0d, 0.0d, 0.0d};
        for (double pr : new double[]{0.0d, 12.0d}) {
            Weather wS = new Weather();
            wS.fetchedAt = System.currentTimeMillis();
            for (int k = 0; k < 4; k++) {
                Calendar c = Dates.plusDays(wBase, k);
                Weather.Day d = new Weather.Day();
                d.year = c.get(Calendar.YEAR); d.month = c.get(Calendar.MONTH) + 1; d.day = c.get(Calendar.DAY_OF_MONTH);
                d.tMax = 28.0d; d.tMin = 14.0d; d.precipMm = pr; d.precipProb = pr > 0 ? 85.0d : 5.0d; d.humidity = 55.0d;
                wS.days.add(d);
            }
            Planner plW = new Planner(store, wS);
            Task tW = new Task();
            tW.op = Operation.WATER; tW.plantId = "tomato"; tW.rainBlocks = false;
            tW.minTemp = 12.0d; tW.maxTemp = 38.0d;
            tW.year = wBase.get(Calendar.YEAR); tW.month = wBase.get(Calendar.MONTH) + 1; tW.day = wBase.get(Calendar.DAY_OF_MONTH);
            plW.evaluateWeather(tW, wBase);
            if (pr == 0.0d) {
                check("засуха → совет полить сегодня", tW.weatherNote.contains("Засушливо"), tW.weatherNote);
            } else {
                check("дождь ≥10 мм за 3 дня → полив можно пропустить", tW.weatherNote.contains("можно пропустить"), tW.weatherNote);
                tW.plantId = "greenhouse";
                plW.evaluateWeather(tW, wBase);
                check("теплица поливается независимо от дождя", tW.weatherNote.contains("не зависит от дождя"), tW.weatherNote);
            }
        }

        System.out.println("\n=== 3. Планировщик: работы на 21 день ===");
        Planner planner = new Planner(store, w);
        List<Task> tasks = planner.tasks(21);
        check("задачи сформированы", tasks.size() > 5, "всего задач: " + tasks.size());

        Calendar today = Dates.today();
        System.out.println("Сегодня " + Dates.fmt(today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH))
                + " (декада " + Rule.decade(today.get(Calendar.DAY_OF_MONTH)) + ")");
        int shown = 0;
        for (Task t : tasks) {
            if (shown++ >= 6) break;
            System.out.printf("  %s %s | %s | %s | материалов: %d | %s%n",
                    Dates.fmtShort(t.year, t.month, t.day), t.plantName, Operation.label(t.op),
                    t.title, t.items.size(), t.weatherNote);
        }

        System.out.println("\n=== 4. Расчёт количества материалов ===");
        Task withMats = null;
        for (Task t : tasks) {
            if (t.items.size() >= 2 && t.solutionL > 0) { withMats = t; break; }
        }
        check("есть задача с расчётом раствора и материалов", withMats != null,
                withMats == null ? "" : withMats.plantName + " / " + withMats.title);
        if (withMats != null) {
            System.out.println("  Растение: " + withMats.plantName + " (" + withMats.title + ")");
            System.out.println("  Рабочий раствор: " + Planner.num(withMats.solutionL) + " л");
            for (Task.Item it : withMats.items) {
                System.out.println("   • " + it.name + " — норма: " + it.dose
                        + "; нужно: " + it.need + "; цена уп.: " + Ui_money(it.price));
            }
            boolean doses = true;
            for (Task.Item it : withMats.items) {
                if (it.need == null || it.need.isEmpty() || it.packs < 1) doses = false;
            }
            check("дозы и упаковки посчитаны", doses, "");
        }

        System.out.println("\n=== 4b. Взаимозаменяемые препараты ===");
        int altCount = 0, altInShop = 0;
        for (Task t : tasks) {
            for (Task.Item it : t.items) if (it.alternative) altCount++;
        }
        // имена заменителей только из задач текущего плана
        Set<String> altNames = new HashSet<>();
        Set<String> primaryNames = new HashSet<>();
        for (Task t : tasks) {
            for (Task.Item it : t.items) {
                if (it.alternative) altNames.add(it.name);
                else primaryNames.add(it.name);
            }
        }
        altNames.removeAll(primaryNames); // препарат, нужный как основной где-то ещё, остаётся в списке
        for (Planner.ShopItem s : planner.shoppingList(30)) {
            if (altNames.contains(s.name)) altInShop++;
        }
        boolean doseMarked = false;
        for (Task t : tasks) {
            for (Task.Item it : t.items) {
                if (it.alternative && it.dose.startsWith("или замена")) doseMarked = true;
            }
        }
        check("альтернативы помечены", altCount > 0, "помечено позиций: " + altCount);
        check("заменитель подписан в карточке работы", doseMarked, "");
        check("заменители не попадают в список покупок", altInShop == 0,
                "чистых заменителей в списке покупок: " + altInShop);

        System.out.println("\n=== 5. Коэффициент возраста растения ===");
        double young = Plant.sizeFactor(Plant.SIZE_YOUNG, "apple");
        double medium = Plant.sizeFactor(Plant.SIZE_MEDIUM, "apple");
        double old = Plant.sizeFactor(Plant.SIZE_OLD, "apple");
        check("дозы растут с возрастом дерева", young < medium && medium < old,
                "молодое " + young + " / плодоносящее " + medium + " / взрослое " + old);
        double bush = Plant.sizeFactor(Plant.SIZE_MEDIUM, "currant_black");
        check("кустарник расходует меньше дерева", bush < medium, "смородина " + bush + " против яблони " + medium);

        System.out.println("\n=== 6. Список покупок ===");
        List<Planner.ShopItem> shop = planner.shoppingList(30);
        check("список покупок сформирован", !shop.isEmpty(), "позиций: " + shop.size());
        double total = 0;
        for (Planner.ShopItem s : shop) {
            total += s.cost();
            System.out.printf("  %s — %d уп. × %.2f BYN = %.2f BYN (%s)%n",
                    s.name, s.packs, s.price, s.cost(), String.join(", ", s.usedFor));
        }
        check("стоимость в BYN посчитана", total > 0,
                String.format(Locale.US, "итого %.2f BYN", total));

        System.out.println("\n=== 7. Погодные ограничения ===");
        boolean rainRule = false;
        for (Task t : tasks) {
            if (t.rainBlocks) { rainRule = true; break; }
        }
        check("есть работы, зависящие от дождя", rainRule, "");
        boolean tempRule = false;
        for (Task t : tasks) {
            if (t.minTemp > -30) { tempRule = true; break; }
        }
        check("есть работы с температурным порогом", tempRule, "");
        int blocked = 0;
        for (Task t : tasks) if (t.weatherState > 0 && t.weatherState < 4) blocked++;
        System.out.println("  по текущему прогнозу отложено работ: " + blocked);

        System.out.println("\n=== 8. Отметка «выполнено» ===");
        Task any = tasks.get(0);
        store.setDone(any.id, any.year, true);
        boolean persisted = new Storage(ctx).isDone(any.id, any.year);
        check("отметка сохраняется", persisted, any.title);
        store.clearDoneForCurrentYear(any.year);
        check("сброс отметок за год", !new Storage(ctx).isDone(any.id, any.year), "");

        System.out.println("\n=== 9. Покрытие календарного года ===");
        java.util.Map<Integer, Integer> byMonth = new java.util.TreeMap<>();
        for (Rule r : Rules.all()) {
            for (int m : r.months) {
                Integer c = byMonth.get(m);
                byMonth.put(m, c == null ? 1 : c + 1);
            }
        }
        check("правила есть во все 12 месяцев", byMonth.size() == 12, "месяцев: " + byMonth.size());
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<Integer, Integer> e : byMonth.entrySet()) {
            sb.append(Dates.MONTHS_NOM[e.getKey() - 1]).append(':').append(e.getValue()).append(' ');
        }
        System.out.println("  " + sb.toString().trim());

        System.out.println("\n=== 10. Овощные культуры и теплица ===");
        int vegPlants = 0;
        for (Plant p : Plant.all()) if (Plant.isVegetable(p.id)) vegPlants++;
        check("овощных культур в каталоге", vegPlants >= 14, "найдено " + vegPlants);
        check("теплица есть в каталоге", Plant.byId("greenhouse") != null, "");
        String[] vegIds = {"tomato", "cucumber", "pepper", "eggplant", "cabbage", "carrot",
                "beet", "onion", "garlic", "potato", "zucchini", "pumpkin", "pea", "radish", "greens"};
        int vegRules = 0;
        for (Rule r : Rules.all()) {
            for (String v : vegIds) if (r.plantId.equals(v)) { vegRules++; break; }
        }
        check("правил по овощам", vegRules >= 60, "найдено " + vegRules);
        int ghRules = 0;
        for (Rule r : Rules.all()) if ("greenhouse".equals(r.plantId)) ghRules++;
        check("правил по теплице (включая зимние)", ghRules >= 5, "найдено " + ghRules);
        boolean ghWinter = false;
        for (Rule r : Rules.all()) {
            if (!"greenhouse".equals(r.plantId)) continue;
            for (int m : r.months) if (m == 12 || m == 1 || m == 2) ghWinter = true;
        }
        check("подготовка теплицы к зиме присутствует", ghWinter, "");

        // полный цикл томата: посев на рассаду -> высадка -> уход -> уборка -> теплица зимой
        String[] needTomato = {"Посев томата на рассаду", "Высадка рассады в теплицу и открытый грунт",
                "Формировка и пасынкование томата", "Сбор плодов и уборка растительных остатков"};
        for (String title : needTomato) {
            boolean found = false;
            for (Rule r : Rules.all()) {
                if ("tomato".equals(r.plantId) && title.equals(r.title)) { found = true; break; }
            }
            check("цикл томата: «" + title + "»", found, "");
        }
        boolean rassada = false;
        for (Rule r : Rules.all()) {
            if (!r.title.contains("рассаду")) continue;
            for (int m : r.months) if (m == 2 || m == 3 || m == 4) rassada = true;
        }
        check("есть работы «на рассаду» в феврале–апреле", rassada, "");

        System.out.println("\n=== 11. Схемы обрезки ===");
        int[][] schemeCases = {
                {R.drawable.scheme_prune_pome, 1},
                {R.drawable.scheme_prune_stone, 1},
                {R.drawable.scheme_prune_currant, 1},
                {R.drawable.scheme_prune_raspberry, 1},
                {R.drawable.scheme_prune_grape, 1},
                {R.drawable.scheme_prune_rose, 1},
                {R.drawable.scheme_prune_clematis, 1},
                {R.drawable.scheme_prune_veg, 1},
        };
        check("схема обрезки семечковых", Scheme.forRule("apple", Operation.PRUNE, "Обрезка") != 0, "");
        check("схема обрезки косточковых", Scheme.forRule("sweet_cherry", Operation.PRUNE, "Обрезка") != 0, "");
        check("схема обрезки смородины", Scheme.forRule("currant_black", Operation.PRUNE, "Обрезка") != 0, "");
        check("схема обрезки малины", Scheme.forRule("raspberry", Operation.PRUNE, "Обрезка") != 0, "");
        check("схема обрезки винограда", Scheme.forRule("grape", Operation.PRUNE, "Обрезка") != 0, "");
        check("схема обрезки розы", Scheme.forRule("rose", Operation.PRUNE, "Обрезка") != 0, "");
        check("схема обрезки клематиса", Scheme.forRule("clematis", Operation.PRUNE, "Обрезка") != 0, "");
        check("схема пасынкования томата",
                Scheme.forRule("tomato", Operation.PRUNE, "Формировка и пасынкование томата") != 0, "");
        check("у опрыскивания схемы нет (не нужна)",
                Scheme.forRule("apple", Operation.SPRAY, "Опрыскивание") == 0, "");
        int withCaption = 0;
        for (int[] c2 : schemeCases) {
            if (Scheme.captionFor(c2[0]) != null && Scheme.captionFor(c2[0]).length() > 20) withCaption++;
        }
        check("у каждой схемы есть подпись", withCaption == schemeCases.length,
                withCaption + " из " + schemeCases.length);
        int schemesInPlan = 0;
        for (Task t : tasks) if (Scheme.forTask(t) != 0) schemesInPlan++;
        System.out.println("  работ со схемой в текущем плане: " + schemesInPlan);

        System.out.println("\n=== 12. Регионы: GPS-определение, цены, климат ===");
        check("Минск -> Беларусь", Region.detect(53.9045, 27.5615).id.equals("by"), "");
        check("Симферополь -> Крым", Region.detect(44.9572, 34.1108).id.equals("crimea"), "");
        check("Киев -> Украина", Region.detect(50.4501, 30.5234).id.equals("ua"), "");
        check("Москва -> Россия", Region.detect(55.7558, 37.6173).id.equals("ru"), "");
        check("Лондон -> прочий регион", Region.detect(51.5074, -0.1278).id.equals("other"), "");
        Region cr = Region.byId("crimea");
        Material skor = Material.byId("skor");
        check("Крым: валюта RUB", cr.currencyCode.equals("RUB"), "");
        check("Крым: цена Скор в рублях", cr.priceOf(skor) > skor.price * 5,
                cr.priceOf(skor) + " RUB против " + skor.price + " BYN");
        check("Беларусь: цена Скор в BYN без изменений",
                Region.byId("by").priceOf(skor) == skor.price, "");
        boolean coverExists = false;
        for (Rule r : Rules.all()) if (cr.skips(r)) coverExists = true;
        check("Крым: зимние укрытия исключаются, сроки раньше",
                coverExists && cr.phenoShiftDays >= 14,
                "сдвиг сроков " + cr.phenoShiftDays + " дн.");
        check("Крым: персик рекомендуется, голубика нетипична",
                cr.isRecommended("peach") && cr.isNotTypical("blueberry"), "");

        Context ctxCr = new Context();
        Storage stCr = new Storage(ctxCr);
        stCr.setPlants(plants);
        stCr.setLocation("Симферополь", 44.9572, 34.1108);
        Planner pCr = new Planner(stCr, w);
        List<Task> tCr = pCr.tasks(21);
        boolean rubSeen = false, coverInPlan = false;
        for (Task t : tCr) {
            for (Task.Item it : t.items) if (it.currency.equals("₽")) rubSeen = true;
            if (Operation.PROTECT.equals(t.op)
                    && (t.title.contains("Укрытие") || t.title.contains("Пригибание"))) {
                coverInPlan = true;
            }
        }
        check("план Крыма сформирован", tCr.size() > 3, "задач: " + tCr.size());
        check("в плане Крыма цены в рублях", rubSeen, "");
        check("в плане Крыма нет зимних укрытий", !coverInPlan, "");
        boolean rubShop = false;
        for (Planner.ShopItem s : pCr.shoppingList(30)) if (s.currency.equals("₽")) rubShop = true;
        check("список покупок Крыма в рублях", rubShop, "");

        System.out.println("\n=== 13. Справочник культур и сорта по срокам ===");
        int cards = 0, withVars = 0;
        for (Plant p : Plant.all()) {
            CropInfo ci = CropInfo.byId(p.id);
            if (ci != null && !ci.vitamins.isEmpty() && !ci.benefit.isEmpty() && !ci.harm.isEmpty()
                    && !ci.kinds.isEmpty() && !ci.season.isEmpty()) cards++;
            if (ci != null && ci.hasVarieties()) withVars++;
        }
        check("справочные карточки по всем культурам", cards == Plant.all().size(),
                cards + " из " + Plant.all().size());
        check("сорта ранние/средние/поздние заполнены", withVars >= 30,
                "культур с сортами: " + withVars);

        Set<String> oneCarrot = new HashSet<>();
        oneCarrot.add("carrot");
        Context cE = new Context();
        Storage sE = new Storage(cE);
        sE.setPlants(oneCarrot);
        Set<String> gE = new HashSet<>();
        gE.add("early");
        sE.setVarietyGroups("carrot", gE);
        int dE = firstHarvestDay(new Planner(sE, w).tasks(30));
        Context cL = new Context();
        Storage sL = new Storage(cL);
        sL.setPlants(oneCarrot);
        Set<String> gL = new HashSet<>();
        gL.add("late");
        sL.setVarietyGroups("carrot", gL);
        int dL = firstHarvestDay(new Planner(sL, w).tasks(30));
        // инвариант, не зависящий от даты: поздние сорта убираются СТРОГО позже ранних
        // (числовая разница плавает внутри сезона: 20 дней в его середине, меньше у края)
        check("поздние сорта сдвигают уборку позже (морковь)", dE > 0 && dL > dE,
                "ранние: день " + dE + ", поздние: день " + dL);

        // несколько групп сроков сразу: уборка планируется по каждой
        Context cM = new Context();
        Storage sM = new Storage(cM);
        sM.setPlants(oneCarrot);
        Set<String> two = new HashSet<>();
        two.add("early");
        two.add("late");
        sM.setVarietyGroups("carrot", two);
        List<Task> tm = new Planner(sM, w).tasks(30);
        int harv = 0;
        boolean earlyT = false, lateT = false;
        for (Task t : tm) {
            if (!Operation.HARVEST.equals(t.op)) continue;
            harv++;
            if (t.title.contains("(ранние сорта)")) earlyT = true;
            if (t.title.contains("(поздние сорта)")) lateT = true;
        }
        check("несколько сроков — уборка по каждой группе", harv >= 2 && earlyT && lateT,
                "задач уборки: " + harv);

        System.out.println("\n=== 14. Данные виджета «Задачи на сегодня» ===");
        List<Task> week = planner.tasks(7);
        check("виджет: окно 7 дней даёт задачи", week.size() > 0, "задач: " + week.size());
        String line = "";
        for (Task t : week) {
            if (t.done) continue;
            line = Operation.icon(t.op) + " " + t.plantName + " — " + t.title;
            break;
        }
        check("виджет: строка задачи содержит культуру и работу",
                !line.isEmpty() && line.length() > 10 && line.contains("—"), line);

        System.out.println("\n=== 15. Пиктограммы и сорта (v2.0) ===");
        boolean artifacts = false, mismatch = false;
        for (Plant pl : Plant.all()) {
            for (char c : pl.icon.toCharArray()) {
                if ((c >= 0x0370 && c <= 0x03FF) || (c >= 0x1F00 && c <= 0x1FFF)) artifacts = true;
            }
            if (pl.id.equals("raspberry") && pl.icon.equals("\uD83C\uDF47")) mismatch = true;
            if (pl.id.equals("grape") && !pl.icon.equals("\uD83C\uDF47")) mismatch = true;
        }
        for (String op : new String[]{Operation.SPRAY, Operation.HARVEST, Operation.PLANT, Operation.REPAIR}) {
            for (char c : Operation.icon(op).toCharArray()) {
                if ((c >= 0x0370 && c <= 0x03FF) || (c >= 0x1F00 && c <= 0x1FFF)) artifacts = true;
            }
        }
        check("нет битых пиктограмм-артефактов", !artifacts, "");
        check("пиктограммы соответствуют названиям (малина ≠ виноград)", !mismatch, "");
        boolean vectors = true;
        for (String id : new String[]{"raspberry", "beet", "quince", "currant_red", "rowan",
                "radish", "currant_black", "chokeberry", "blackberry", "gooseberry",
                "pea", "plum", "irga", "blueberry", "honeysuckle", "sea_buckthorn"}) {
            Plant vp = Plant.byId(id);
            if (vp == null || vp.iconRes == 0) { vectors = false; System.out.println("  нет вектора: " + id); }
        }
        check("у культур без подходящего эмодзи — свои векторные пиктограммы", vectors, "");
        int thin = 0;
        for (Plant pl : Plant.all()) {
            if (pl.id.equals("lawn") || pl.id.equals("greenhouse")) continue;
            CropInfo ci = CropInfo.byId(pl.id);
            if (ci == null) { thin++; continue; }
            int cnt = 0;
            for (String grp : new String[]{ci.early, ci.mid, ci.late}) {
                for (String v : grp.split(",")) if (!v.trim().isEmpty()) cnt++;
            }
            if (cnt < 5) { System.out.println("  мало сортов: " + pl.name + " = " + cnt); thin++; }
        }
        check("списки сортов расширены (≥5 у каждой культуры)", thin == 0, "тонких: " + thin);

        System.out.println("\n=== 16. Размер шрифта ===");
        check("4 ступени масштаба шрифта с подписями",
            Fonts.SCALES.length == 4 && Fonts.LABELS.length == 4,
            "масштабов: " + Fonts.SCALES.length + ", подписей: " + Fonts.LABELS.length);
        boolean monoFont = Fonts.SCALES[0] < Fonts.SCALES[1]
            && Fonts.SCALES[1] < Fonts.SCALES[2] && Fonts.SCALES[2] < Fonts.SCALES[3];
        check("ступени возрастают, обычный = 100%", monoFont && Fonts.SCALES[1] == 1.0f,
            java.util.Arrays.toString(Fonts.SCALES));
        Context ctxFont = new Context();
        check("размер шрифта по умолчанию — обычный", new Storage(ctxFont).fontSize() == 1, "");
        new Storage(ctxFont).setFontSize(3);
        check("размер шрифта сохраняется в настройках", new Storage(ctxFont).fontSize() == 3, "");
        check("масштаб «огромный» = ×1.4", Fonts.scale(ctxFont) == 1.4f, String.valueOf(Fonts.scale(ctxFont)));
        new Storage(ctxFont).setFontSize(9);
        check("некорректное значение в настройках → обычный шрифт", Fonts.scale(ctxFont) == 1.0f, "");
        new Storage(ctxFont).setFontSize(3);
        Context scaledFont = Fonts.applyFont(ctxFont);
        check("масштаб шрифта применяется к контексту (attachBaseContext)",
            scaledFont.getResources().getConfiguration().fontScale == 1.4f,
            String.valueOf(scaledFont.getResources().getConfiguration().fontScale));
        new Storage(ctxFont).setFontSize(1);
        check("обычный шрифт — контекст без обёртки", Fonts.applyFont(ctxFont) == ctxFont, "");
        check("настройки общие для обёрнутого контекста", new Storage(scaledFont).fontSize() == 1, "");

        // Настройки должны быть доступны с главного экрана (меню без тулбара недоступно на современных телефонах)
        String layoutMain = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_main.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        String srcMain = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("на главном экране есть видимая кнопка настроек", layoutMain.contains("@+id/btn_settings"), "");
        check("кнопка настроек открывает SettingsActivity",
                srcMain.contains("R.id.btn_settings") && srcMain.contains("SettingsActivity.class"), "");
        String layoutSettings = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_settings.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        int fg = layoutSettings.indexOf("@+id/font_group");
        String fontBlock = fg < 0 ? "" : layoutSettings.substring(fg, Math.min(fg + 400, layoutSettings.length()));
        check("список размеров шрифта вертикальный (влезает при любом масштабе)",
                fg >= 0 && fontBlock.contains("android:orientation=\"vertical\""), "");

        System.out.println("\n" + (failures == 0 ? "ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ" : "ПРОВАЛЕНО ПРОВЕРОК: " + failures));
        if (failures > 0) System.exit(1);
    }

    private static String Ui_money(double v) {
        return String.format(Locale.US, "%.2f BYN", v);
    }

    private static int firstHarvestDay(List<Task> tasks) {
        int best = -1;
        for (Task t : tasks) {
            if (!Operation.HARVEST.equals(t.op)) continue;
            int doy = Dates.at(t.year, t.month, t.day).get(Calendar.DAY_OF_YEAR);
            if (best < 0 || doy < best) best = doy;
        }
        return best;
    }

    private static boolean allMaterialsExist() {
        for (Rule r : Rules.all()) {
            for (MatRef ref : r.mats) {
                if (Material.byId(ref.materialId) == null) {
                    System.out.println("  нет материала: " + ref.materialId + " в правиле " + r.title);
                    return false;
                }
            }
        }
        return true;
    }
}
