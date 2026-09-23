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
        if (!ok) {
            failures++;
            // Аннотация GitHub Actions: провал виден прямо в проверках, даже без чтения логов
            System.out.println("::error::FAIL — " + what + (detail.isEmpty() ? "" : " — " + detail));
        }
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
        int photoDebtMax = 0;
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
        // фиксированный старт окна 1 сентября — до первых уборок любой группы сортов;
        // проверка не зависит от текущей даты запуска тестов
        Calendar fixedStart = Dates.at(2027, 9, 1);
        int dE = firstHarvestDay(new Planner(sE, w).tasks(45, fixedStart));
        Context cL = new Context();
        Storage sL = new Storage(cL);
        sL.setPlants(oneCarrot);
        Set<String> gL = new HashSet<>();
        gL.add("late");
        sL.setVarietyGroups("carrot", gL);
        int dL = firstHarvestDay(new Planner(sL, w).tasks(45, fixedStart));
        // инвариант: поздние сорта убираются СТРОГО позже ранних
        // (от старта 1 сентября: ранние — с 1 сентября, поздние — с 21 сентября)
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
        List<Task> tm = new Planner(sM, w).tasks(45, fixedStart);
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
        check("6 ступеней масштаба шрифта с подписями",
            Fonts.SCALES.length == 6 && Fonts.LABELS.length == 6,
            "масштабов: " + Fonts.SCALES.length + ", подписей: " + Fonts.LABELS.length);
        boolean monoFont = Fonts.SCALES[0] < Fonts.SCALES[1]
            && Fonts.SCALES[1] < Fonts.SCALES[2] && Fonts.SCALES[2] < Fonts.SCALES[3]
            && Fonts.SCALES[3] < Fonts.SCALES[4] && Fonts.SCALES[4] < Fonts.SCALES[5];
        check("ступени возрастают, обычный = 100%", monoFont && Fonts.SCALES[1] == 1.0f,
            java.util.Arrays.toString(Fonts.SCALES));
        check("две новые ступени крупнее ×1.4", Fonts.SCALES[4] == 1.6f && Fonts.SCALES[5] == 1.85f,
            "×" + Fonts.SCALES[4] + " и ×" + Fonts.SCALES[5]);
        Context ctxFont = new Context();
        check("размер шрифта по умолчанию — обычный", new Storage(ctxFont).fontSize() == 1, "");
        new Storage(ctxFont).setFontSize(3);
        check("размер шрифта сохраняется в настройках", new Storage(ctxFont).fontSize() == 3, "");
        check("масштаб «очень крупный» = ×1.4", Fonts.scale(ctxFont) == 1.4f, String.valueOf(Fonts.scale(ctxFont)));
        new Storage(ctxFont).setFontSize(5);
        check("масштаб «гигантский» = ×1.85", Fonts.scale(ctxFont) == 1.85f, String.valueOf(Fonts.scale(ctxFont)));
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

        System.out.println("\n=== 17. Сезонные подсказки календаря ===");
        Set<String> dzIds = new HashSet<>();
        for (Disease dz : dzAll) dzIds.add(dz.id);
        Set<String> plantIds = new HashSet<>();
        for (Plant pl : Plant.all()) plantIds.add(pl.id);
        int emptyActive = 0, badHint = 0, dupHint = 0;
        for (int m = 0; m < 12; m++) {
            List<Disease> monthHints = SeasonHints.forMonth(m, plantIds, dzAll);
            if (m >= 2 && m <= 9 && monthHints.isEmpty()) emptyActive++;
            if ((m < 2 || m > 9) && !monthHints.isEmpty()) emptyActive++;
            Set<String> seen = new HashSet<>();
            for (Disease dz : monthHints) {
                if (!dzIds.contains(dz.id) || !plantIds.contains(dz.plantId)) badHint++;
                if (!seen.add(dz.id)) dupHint++;
            }
        }
        check("подсказки есть с марта по октябрь, зимой скрыты", emptyActive == 0, "нарушений: " + emptyActive);
        check("все подсказки ведут на существующие записи и культуры", badHint == 0, "битых: " + badHint);
        check("в месяце нет повторных подсказок", dupHint == 0, "дублей: " + dupHint);
        // фильтрация по культурам пользователя
        Set<String> onlyApple = new HashSet<>(java.util.Arrays.asList("apple"));
        List<Disease> appleJune = SeasonHints.forMonth(5, onlyApple, dzAll);
        boolean allApple = !appleJune.isEmpty();
        for (Disease dz : appleJune) {
            if (!"apple".equals(dz.plantId)) allApple = false;
        }
        check("подсказки фильтруются по культурам пользователя", allApple,
            "июнь, только яблоня: " + appleJune.size() + " шт.");
        check("у пользователя без яблони плодожорка не подсвечивается",
            SeasonHints.forMonth(5, new HashSet<>(java.util.Arrays.asList("cabbage")), dzAll).size()
                < SeasonHints.forMonth(5, plantIds, dzAll).size(), "");
        // сезонный текст соответствует месяцу
        Disease scabDz = null;
        for (Disease dz : dzAll) {
            if (dz.id.equals("apple_scab")) scabDz = dz;
        }
        boolean seasons = scabDz != null
            && SeasonHints.seasonText(scabDz, 4).equals(scabDz.spring)
            && SeasonHints.seasonText(scabDz, 6).equals(scabDz.summer)
            && SeasonHints.seasonText(scabDz, 9).equals(scabDz.autumn)
            && SeasonHints.seasonText(scabDz, 0).equals(scabDz.spring);
        check("сезонный текст подсказки соответствует месяцу", seasons, "");

        System.out.println("\n=== 18. Поделиться советом ===");
        Plant appleForShare = Plant.byId("apple");
        Disease scabForShare = null, pestForShare = null;
        for (Disease dz : dzAll) {
            if (dz.id.equals("apple_scab")) scabForShare = dz;
            if (dz.id.equals("apple_codling_moth")) pestForShare = dz;
        }
        String shareScab = ShareText.diseaseAdvice(appleForShare, scabForShare);
        check("шаринг: болезнь — название и культура в тексте",
                shareScab.contains(scabForShare.name) && shareScab.contains("Яблоня"), "");
        check("шаринг: болезнь — есть распознавание, лечение, профилактика и препараты",
                shareScab.contains("Как распознать") && shareScab.contains("Лечение по шагам")
                && shareScab.contains("Профилактика") && shareScab.contains("Препараты"), "");
        check("шаринг: болезнь — сезонные блоки с пометками",
                shareScab.contains("Весна") && (shareScab.contains("Лето") || shareScab.contains("Осень")), "");
        check("шаринг: болезнь — подпись приложения и разумная длина",
                shareScab.contains("ZI Garden") && shareScab.length() > 200 && shareScab.length() < 4000,
                "длина: " + shareScab.length());
        String sharePest = ShareText.diseaseAdvice(appleForShare, pestForShare);
        check("шаринг: вредитель — текст без «null» и с шагами",
                !sharePest.contains("null") && sharePest.contains(pestForShare.name)
                && sharePest.contains("1. "), "");
        String shareNoPlant = ShareText.diseaseAdvice(null, scabForShare);
        check("шаринг: без культуры текст всё равно собирается",
                shareNoPlant.contains(scabForShare.name) && !shareNoPlant.contains("null"), "");
        String srcDisease = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/DiseaseActivity.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("шаринг: кнопка «Поделиться советом» есть на экране болезней",
                srcDisease.contains("📤 Поделиться советом"), "");
        check("шаринг: отправка через системный выбор приложения (ACTION_SEND + createChooser)",
                srcDisease.contains("ACTION_SEND") && srcDisease.contains("createChooser"), "");

        System.out.println("\n=== 19. Совместимость со старым Android (min API 21) ===");
        String buildSh = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("build.sh").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("минимальная версия: Android 5.0 (API 21) в aapt2 link",
                buildSh.contains("--min-sdk-version 21"), "");
        check("минимальная версия: API 21 в d8",
                buildSh.contains("--min-api 21"), "");
        check("байткод Java 8 (поддерживается d8 на 21+)",
                buildSh.contains("--release 8"), "");
        check("целевая версия — Android 14 (API 34)",
                buildSh.contains("--target-sdk-version 34"), "");
        // Новые API (23+) допустимы только под guard'ом Build.VERSION.SDK_INT в том же файле.
        String[] risky = {"NotificationChannel", "createNotificationChannel", "POST_NOTIFICATIONS",
                "startForegroundService", "canScheduleExactAlarms", "checkSelfPermission",
                "requestPermissions", "Notification.Builder(", "canRequestPackageInstalls",
                "getLongVersionCode"};
        Set<String> unguarded = new HashSet<>();
        java.io.File srcDir = new java.io.File("src/by/csl/gardener");
        java.io.File[] javaFiles = srcDir.listFiles(new java.io.FilenameFilter() {
            public boolean accept(java.io.File d, String n) { return n.endsWith(".java"); }
        });
        if (javaFiles == null) javaFiles = new java.io.File[0];
        for (java.io.File f : javaFiles) {
            String body = new String(java.nio.file.Files.readAllBytes(f.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            for (String tok : risky) {
                if (body.contains(tok) && !body.contains("SDK_INT")) {
                    unguarded.add(f.getName() + ": " + tok);
                }
            }
        }
        check("все вызовы API 23+ защищены проверкой Build.VERSION",
                unguarded.isEmpty(), unguarded.toString());
        // Java-библиотеки, недоступные на 21 без десугара — запрещены.
        String[] bannedLib = {"java.time", "java.nio.file", "java.util.stream",
                "java.util.Base64", "java.util.Optional"};
        Set<String> foundLib = new HashSet<>();
        for (java.io.File f : javaFiles) {
            String body = new String(java.nio.file.Files.readAllBytes(f.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            for (String tok : bannedLib) {
                if (body.contains(tok)) foundLib.add(f.getName() + ": " + tok);
            }
        }
        check("нет вызовов библиотек новее Android 5 (java.time/Stream/nio)",
                foundLib.isEmpty(), foundLib.toString());
        // Тема Material требует API 21 — lower порога уже нельзя.
        String stylesXml = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values/styles.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        boolean themeOk = stylesXml.contains("Theme.Material"); // 21+
        check("тема Material согласована с порогом API 21", themeOk, "");
        // Каждый компонент с intent-filter обязан иметь android:exported (требование 31+/aapt2).
        String manifest = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        int filters = 0, exportedTagged = 0;
        java.util.regex.Matcher mBlock = java.util.regex.Pattern
                .compile("<(activity|service|receiver)\\b[^>]*>(.*?)</\\1>|<(activity|service|receiver)\\b[^>]*/>",
                        java.util.regex.Pattern.DOTALL).matcher(manifest);
        while (mBlock.find()) {
            String whole = mBlock.group(0);
            if (whole.contains("intent-filter")) {
                filters++;
                if (whole.contains("android:exported")) exportedTagged++;
            }
        }
        check("у всех компонентов с intent-filter указан android:exported",
                filters > 0 && filters == exportedTagged, filters + "/" + exportedTagged);
        // Строковые новые разрешения в манифесте на старых версиях игнорируются — это безопасно,
        // но контролируем, что не появился auto-permission без обработки.
        check("разрешения манифеста известны и все из списка",
                manifest.contains("POST_NOTIFICATIONS") && manifest.contains("SCHEDULE_EXACT_ALARM")
                && manifest.contains("ACCESS_FINE_LOCATION"), "");
        // Метод эталона: Geo.hasPermission должен считать API<23 «разрешено при установке».
        String srcGeo = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/Geo.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("Geo: на Android 5 разрешение геолокации считается выданным",
                srcGeo.contains("SDK_INT < 23") && srcGeo.contains("return true;"), "");

        System.out.println("\n=== 20. Виджеты рабочего стола ===");
        check("виджеты: месяцы в предложном падеже — 12 шт.", WidgetTexts.MONTHS_GEN.length == 12, "");
        check("виджет погоды: без данных — вежливая подсказка",
                WidgetTexts.weatherNow(null).contains("Нет данных")
                && WidgetTexts.weatherAdvice(null, 2026, 9, 17).contains("Обновите"), "");
        // погодные ветки совета
        Weather wf = new Weather();
        Weather.Day frost = new Weather.Day();
        frost.year = 2026; frost.month = 5; frost.day = 3; frost.tMin = -2; frost.tMax = 9;
        frost.code = 0; frost.windMax = 5; frost.precipMm = 0; frost.precipProb = 0;
        wf.days.add(frost);
        check("совет виджета: заморозок — укрыть",
                WidgetTexts.weatherAdvice(wf, 2026, 5, 3).contains("замороз"), "");
        Weather wr = new Weather();
        Weather.Day rain = new Weather.Day();
        rain.year = 2026; rain.month = 6; rain.day = 10; rain.tMin = 12; rain.tMax = 18;
        rain.code = 61; rain.windMax = 5; rain.precipMm = 5; rain.precipProb = 80;
        wr.days.add(rain);
        check("совет виджета: дождь — полив не нужен",
                WidgetTexts.weatherAdvice(wr, 2026, 6, 10).contains("полив не нужен"), "");
        Weather wd = new Weather();
        for (int i = 0; i < 3; i++) {
            Weather.Day dry = new Weather.Day();
            dry.year = 2026; dry.month = 7; dry.day = 20 + i; dry.tMin = 15; dry.tMax = 26;
            dry.code = 1; dry.windMax = 3; dry.precipMm = 0; dry.precipProb = 0;
            wd.days.add(dry);
        }
        check("совет виджета: засуха 3 дня — полейте",
                WidgetTexts.weatherAdvice(wd, 2026, 7, 20).contains("полейте"), "");
        Weather wn = new Weather();
        Weather.Day nice = new Weather.Day();
        nice.year = 2026; nice.month = 9; nice.day = 17; nice.tMin = 7; nice.tMax = 18;
        nice.code = 2; nice.windMax = 4; nice.precipMm = 0.5; nice.precipProb = 30;
        wn.days.add(nice);
        wn.currentCode = 2; wn.currentTemp = 14; wn.cityName = "Полоцк";
        check("совет виджета: нормальная погода — можно работать",
                WidgetTexts.weatherAdvice(wn, 2026, 9, 17).contains("благоприятна"), "");
        check("виджет погоды: строка «Сегодня» и город в заголовке",
                WidgetTexts.weatherToday(wn, 2026, 9, 17).contains("Сегодня")
                && WidgetTexts.weatherTitle(wn).contains("Полоцк"), "");
        check("виджет сезона: июньские подсказки с культурами",
                WidgetTexts.seasonLines(5, plantIds, dzAll, 3).size() == 4
                && WidgetTexts.seasonLines(5, plantIds, dzAll, 3).get(0).contains("—"), "");
        check("виджет сезона: январь — тихая подпись",
                WidgetTexts.seasonLines(0, plantIds, dzAll, 3).isEmpty()
                && WidgetTexts.seasonQuiet(0, plantIds).contains("Спокойный"), "");
        check("виджет сезона: без культур — приглашение отметить",
                WidgetTexts.seasonQuiet(5, new HashSet<String>()).contains("Мои растения"), "");
        check("памятка: детерминирована и зависит от дня",
                WidgetTexts.tipOfDay(61).equals(WidgetTexts.tipOfDay(61))
                && !WidgetTexts.tipOfDay(61).equals(WidgetTexts.tipOfDay(62))
                && WidgetTexts.tipOfDay(1).equals(WidgetTexts.tipOfDay(1 + WidgetTexts.TIPS.length)), "");
        boolean tipsOk = WidgetTexts.TIPS.length >= 30;
        for (String tip : WidgetTexts.TIPS) {
            if (tip == null || tip.length() < 15 || tip.contains("null")) tipsOk = false;
        }
        check("памятки: ≥30 осмысленных советов без пустых", tipsOk,
                "советов: " + WidgetTexts.TIPS.length);
        for (String cls : new String[]{"WeatherWidgetProvider", "SeasonWidgetProvider", "TipWidgetProvider"}) {
            check("виджет: провайдер " + cls + " есть и зарегистрирован",
                    new java.io.File("src/by/csl/gardener/" + cls + ".java").exists()
                    && manifest.contains("." + cls + "\""), cls);
        }
        for (String lay : new String[]{"widget_weather", "widget_season", "widget_tip"}) {
            check("виджет: layout и appwidget-info " + lay,
                    new java.io.File("res/layout/" + lay + ".xml").exists()
                    && new java.io.File("res/xml/" + lay + "_info.xml").exists(), lay);
        }
        check("виджеты: единое обновление Widgets.refreshAll вызывается приложением",
                new java.io.File("src/by/csl/gardener/Widgets.java").exists()
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("Widgets.refreshAll")
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/BootReceiver.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("Widgets.refreshAll"), "");

        System.out.println("\n=== 21. Самообновление с GitHub ===");
        check("тег v2.6 → код 26000 (как в CI)",
                UpdateInfo.versionCodeFromTag("v2.6") == 26000
                && UpdateInfo.versionCodeFromTag("v2.10") == 30000, "");
        check("битые теги не дают кода", UpdateInfo.versionCodeFromTag("v2") < 0
                && UpdateInfo.versionCodeFromTag("abc") < 0
                && UpdateInfo.versionCodeFromTag(null) < 0, "");
        String relJson = "{\"tag_name\":\"v2.7\",\"name\":\"2.7\",\"body\":\"Новое: виджеты\\nИсправления\","
                + "\"assets\":[{\"name\":\"app.apk\",\"browser_download_url\":\"https://github.com/x/app.apk\"}]}";
        UpdateInfo parsed = UpdateInfo.fromReleaseJson(relJson);
        check("парсинг релиза: тег, код, ссылка на APK",
                parsed != null && parsed.versionCode == 27000
                && parsed.apkUrl.endsWith(".apk") && parsed.notes.contains("Новое"), "");
        check("релиз без APK → null", UpdateInfo.fromReleaseJson(
                "{\"tag_name\":\"v2.7\",\"assets\":[]}") == null, "");
        check("битый JSON → null, без падения", UpdateInfo.fromReleaseJson("{oops") == null, "");
        check("сравнение версий: релиз 2.7 новее CI-сборки 17036",
                UpdateInfo.isNewer(27000, 17036)
                && !UpdateInfo.isNewer(27000, 27000) && !UpdateInfo.isNewer(26000, 27000), "");
        String srcManifest = manifest;
        check("манифест: разрешение REQUEST_INSTALL_PACKAGES",
                srcManifest.contains("REQUEST_INSTALL_PACKAGES"), "");
        check("манифест: провайдер APK не экспортирован, выдачи по разрешению",
                srcManifest.contains("by.csl.gardener.apk")
                && srcManifest.contains("android:grantUriPermissions=\"true\""), "");
        String srcApk = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/ApkProvider.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("ApkProvider: корректный MIME и открытие только на чтение",
                srcApk.contains("application/vnd.android.package-archive")
                && srcApk.contains("MODE_READ_ONLY"), "");
        String srcUpd = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/AppUpdate.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("обновление: проверка по GitHub releases/latest с User-Agent",
                srcUpd.contains("repos/zigorminsk-debug/ZI-Garden/releases/latest")
                && srcUpd.contains("User-Agent"), "");
        check("обновление: троттлинг проверки (6 часов)",
                srcUpd.contains("CHECK_EVERY_MS"), "");
        check("обновление: установка через системный установщик из content://",
                srcUpd.contains("ACTION_INSTALL_PACKAGE")
                && srcUpd.contains("FLAG_GRANT_READ_URI_PERMISSION"), "");
        check("обновление: авто-проверка при запуске и ручная из «О приложении»",
                new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("AppUpdate.autoCheck")
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/AboutActivity.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("AppUpdate.checkNow"), "");

        System.out.println("\n=== 22. План на неделю, поиск, журнал сада ===");
        // План работ на неделю (шаринг)
        java.util.List<Task> wk = new java.util.ArrayList<>();
        Task tA = new Task(); tA.year = 2026; tA.month = 9; tA.day = 21; tA.op = "spray";
        tA.plantName = "Яблоня"; tA.title = "Обработка от парши"; tA.done = false;
        Task tB = new Task(); tB.year = 2026; tB.month = 9; tB.day = 23; tB.op = "water";
        tB.plantName = "Виноград"; tB.title = "Полив"; tB.done = true; // не попадёт в план
        wk.add(tA); wk.add(tB);
        String plan = ShareText.weekPlan(wk, "19.09");
        check("план недели: задача есть, выполненная пропущена",
                plan.contains("Обработка от парши") && !plan.contains("Полив")
                && plan.contains("ZI Garden"), "");
        check("план недели: пустой список — дружелюбный текст",
                ShareText.weekPlan(new java.util.ArrayList<Task>(), "").contains("не запланировано"), "");
        java.util.List<Task> many = new java.util.ArrayList<>();
        for (int i = 0; i < 40; i++) {
            Task t = new Task(); t.year = 2026; t.month = 9; t.day = 20 + (i % 7); t.op = "monitor";
            t.plantName = "Культура"; t.title = "Осмотр номер " + i + " с длинным длинным описанием работы";
            many.add(t);
        }
        check("план недели: длинный список ужимается с «ещё N»",
                ShareText.weekPlan(many, "").contains("ещё"), "");
        check("план недели: кнопка есть в календаре",
                new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/CalendarActivity.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("Отправить план на неделю"), "");
        // Поиск по справочнику
        Disease scabForSearch = null;
        for (Disease dz : dzAll) {
            if (dz.id.equals("apple_scab")) scabForSearch = dz;
        }
        check("поиск: находит по названию (регистр не важен)",
                Search.matches(scabForSearch, "ПАРША") && Search.matches(scabForSearch, "парша"), "");
        check("поиск: несколько слов — все должны встретиться",
                Search.matches(scabForSearch, "парша яблон")
                && !Search.matches(scabForSearch, "парша банановый экватор"), "");
        check("поиск: пустой запрос показывает всё", Search.matches(scabForSearch, "   "), "");
        check("поиск: экран болезней содержит строку фильтра",
                srcDisease.contains("Search.matches") && srcDisease.contains("EditText"), "");
        check("фото болезни открывается на весь экран",
                srcDisease.contains("zoomPhoto"), "");
        // Журнал сада + сроки ожидания
        check("сроки ожидания: известные препараты",
                WaitDays.forName("Актара ВДГ") == 21 && WaitDays.forName("Фитоверм КЭ") == 2
                && WaitDays.forName("Поливочный шланг") == 0, "");
        long nowMs = System.currentTimeMillis();
        check("статус ожидания: свежая обработка → «ещё N дн.»",
                WaitDays.statusLine("spray", "Актара", nowMs, nowMs).contains("ещё 21 дн"), "");
        check("статус ожидания: срок вышел → разрешение сборов",
                WaitDays.statusLine("spray", "Актара", nowMs - 22L * 86400000L, nowMs)
                .contains("вышел"), "");
        check("статус ожидания: только для обработок",
                WaitDays.statusLine("feed_root", "Актара", nowMs, nowMs).length() == 0, "");
        Task tMats = new Task();
        Task.Item i1 = new Task.Item(); i1.name = "Актара ВДГ"; i1.alternative = false;
        Task.Item i2 = new Task.Item(); i2.name = "Фитоверм КЭ (альтернатива)"; i2.alternative = true;
        tMats.items.add(i1); tMats.items.add(i2);
        check("журнал: материалы записываются без альтернатив",
                WaitDays.matsCsv(tMats).equals("Актара ВДГ"), "");
        String enc = Journal.encode("spray", "Яблоня", "Обработка, повторно", "Актара", 123456789L);
        String[] dec = Journal.decode(enc);
        check("журнал: кодирование/декодирование с запятыми",
                dec != null && dec[Journal.F_PLANT].equals("Яблоня")
                && dec[Journal.F_TITLE].equals("Обработка, повторно")
                && Journal.when(dec) == 123456789L, "");
        java.util.List<String> rawJ = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rawJ.add(Journal.encode("water", "Виноград", "Полив " + i, "", 1000L + i));
        }
        java.util.List<String> newest = Journal.newest(rawJ, 3);
        check("журнал: сортировка новые-сверху и лимит",
                newest.size() == 3 && Journal.when(Journal.decode(newest.get(0))) == 1004L, "");
        Storage stJ = new Storage(new Context());
        stJ.addJournal("spray", "Томат", "Обработка от фитофторы", "Ридомил Голд");
        stJ.addJournal("harvest", "Клубника", "Сбор урожая", "");
        java.util.List<String> jourRows = stJ.journal(10);
        check("журнал: запись сохраняется и читается",
                jourRows.size() == 2
                && Journal.decode(jourRows.get(0))[Journal.F_PLANT].equals("Клубника"), "");
        check("журнал: экран и пункт меню зарегистрированы",
                manifest.contains(".JournalActivity")
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("JournalActivity.class"), "");
        check("журнал: отметки «выполнено» пишутся из задач/диалога/уведомления",
                new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/Ui.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("addJournal")
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/TaskDialog.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("addJournal")
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/NotifyTapReceiver.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("addJournal"), "");

        System.out.println("\n=== 23. Диагностика самообновления ===");
        String html = "<html><body><a href=\"/zigorminsk-debug/ZI-Garden/releases/tag/v2.9\">v2.9</a>"
                + "<a href=\"/zigorminsk-debug/ZI-Garden/releases/download/v2.9/ZI-Garden-csl.apk\">apk</a></body></html>";
        UpdateInfo htmlInfo = UpdateInfo.fromHtmlPage(html);
        check("резервный канал: тег и код из HTML-страницы релизов",
                htmlInfo != null && htmlInfo.tag.equals("v2.9") && htmlInfo.versionCode == 29000, "");
        check("резервный канал: прямая ссылка на APK абсолютная",
                htmlInfo != null && htmlInfo.apkUrl.startsWith("https://github.com/")
                && htmlInfo.apkUrl.endsWith(".apk"), "");
        check("резервный канал: страница без тега/APK → null",
                UpdateInfo.fromHtmlPage("<a href=\"/foo\">no</a>") == null
                && UpdateInfo.fromHtmlPage("<a href=\"/x/releases/tag/v1.2\">t</a>") == null, "");
        check("ошибки по-русски: DNS / таймаут / SSL / 403",
                NetErrors.ru(new java.net.UnknownHostException()).contains("интернет")
                && NetErrors.ru(new java.net.SocketTimeoutException()).contains("таймаут")
                && NetErrors.ru(new javax.net.ssl.SSLException("x")).contains("защищённое")
                && NetErrors.ru(new java.io.IOException("HTTP 403")).contains("403"), "");
        check("обновление: запасной канал и предпроверка сети подключены",
                srcUpd.contains("github.com/zigorminsk-debug/ZI-Garden/releases")
                && srcUpd.contains("isOnline"), "");
        check("обновление: разрешение доступа к состоянию сети в манифесте",
                manifest.contains("ACCESS_NETWORK_STATE"), "");
        String pagesJson = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("../pages/latest.json").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        UpdateInfo pagesInfo = UpdateInfo.fromReleaseJson(pagesJson);
        check("зеркало: pages/latest.json — валидный релиз с APK",
                pagesInfo != null && pagesInfo.versionCode > 0 && pagesInfo.apkUrl != null,
                "");
        check("обновление: третий канал — raw-зеркало (raw.githubusercontent.com)",
                srcUpd.contains("raw.githubusercontent.com/zigorminsk-debug/ZI-Garden"), "");
        check("обновление: четвёртый канал — зеркало на GitHub Pages (github.io)",
                srcUpd.contains("github.io/ZI-Garden/latest.json"), "");
        check("обновление: без сведений о статусе сети просто пробуем",
                srcUpd.contains("cm == null) {\n                return true;"), "");
        check("обновление: скачивание перебирает все зеркала APK",
                srcUpd.contains("info.apkUrls"), "");
        check("обновление: полный диалог сбоя + кнопка «в браузере»",
                srcUpd.contains("showErrorDialog") && srcUpd.contains("openReleasePage")
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/AboutActivity.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("open_releases"), "");
        String[] relTwo = {"{\"tag_name\":\"v3.0\",\"assets\":[",
                "{\"browser_download_url\":\"https://a.io/app.apk\"},",
                "{\"browser_download_url\":\"https://github.com/x.apk\"}]}"};
        UpdateInfo rel2 = UpdateInfo.fromReleaseJson(String.join("", relTwo));
        check("релиз с двумя ссылками: обе собраны, главная — первая",
                rel2 != null && rel2.apkUrls.size() == 2
                && rel2.apkUrl.equals("https://a.io/app.apk"), "");
        String workflow = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("../.github/workflows/build-apk.yml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("CI: релиз по тегу публикует зеркало на Pages и не зацикливается",
                workflow.contains("pages/latest.json") && workflow.contains("pages/app.apk")
                && workflow.contains("pages/**") && workflow.contains("[skip ci]"), "");

        // ───────────────────────── 24. РЕЗЕРВНАЯ КОПИЯ ─────────────────────────
        HashMap<String, Object> srcP = new HashMap<>();
        srcP.put("city", "Полоцк");
        srcP.put("font", 3);
        srcP.put("notify", Boolean.TRUE);
        srcP.put("weather_ts", 1700000000000L);
        srcP.put("bought", new HashSet<>(java.util.Arrays.asList("Фитоспорин", "Агат-25")));
        srcP.put("quote", "с «кавычками» и символами %");
        HashMap<String, Object> emptyDone = new HashMap<>();
        HashMap<String, Object> srcJ = new HashMap<>();
        srcJ.put("j#1700000000000#ab", "{\"plant\":\"Яблоня\",\"name\":\"опрыскивание\"}");
        String encodedBackup = Backup.encode(srcP, emptyDone, srcJ, 2010000);
        Map<String, Object>[] decodedBackup = Backup.decode(encodedBackup);
        check("резервная копия: типы и кириллица переживают экспорт-импорт",
                decodedBackup != null
                && "Полоцк".equals(decodedBackup[0].get("city"))
                && Integer.valueOf(3).equals(decodedBackup[0].get("font"))
                && Boolean.TRUE.equals(decodedBackup[0].get("notify"))
                && Long.valueOf(1700000000000L).equals(decodedBackup[0].get("weather_ts"))
                && ((Set<?>) decodedBackup[0].get("bought")).size() == 2
                && ((String) decodedBackup[0].get("quote")).contains("«кавычками»")
                && decodedBackup[2].size() == 1 && decodedBackup[1].isEmpty(), "");
        check("резервная копия: чужой JSON и мусор отвергаются",
                Backup.decode("{\"a\":1}") == null && Backup.decode("не json") == null, "");
        Storage stBackup = new Storage(new Context());
        stBackup.setLeadDays(2);
        java.util.Map<String, ?>[] snapB = stBackup.exportAll();
        String dumpB = Backup.encode(snapB[0], snapB[1], snapB[2], 2010000);
        Map<String, Object>[] backB = Backup.decode(dumpB);
        Storage stRestore = new Storage(new Context());
        stRestore.setLeadDays(1); // отличается от копии — должен перезаписаться значением из копии
        int restored = stRestore.importAll(backB[0], backB[1], backB[2]);
        check("резервная копия: полный цикл через хранилище возвращает значения",
                restored == snapB[0].size() + snapB[1].size() + snapB[2].size()
                && stRestore.leadDays() == 2 && stRestore.city().equals(stBackup.city()), "");
        String srcSet = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/SettingsActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcLay = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_settings.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcBuiTmp = new String(java.nio.file.Files.readAllBytes(new java.io.File("src/by/csl/gardener/BackupUi.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("резервная копия: кнопки и системный диалог файлов подключены",
                srcLay.contains("backup_export") && srcLay.contains("backup_import")
                && srcSet.contains("ACTION_CREATE_DOCUMENT") && srcSet.contains("ACTION_OPEN_DOCUMENT")
                && srcSet.contains("BackupUi.confirm") && srcBuiTmp.contains("importAll"), "");

        // ───────────────────────── 26. СЕМЕЙНАЯ СИНХРОНИЗАЦИЯ (e-mail) ─────────────────────────
        check("семья: адрес хранится в настройках",
                srcLay.contains("family_email") && srcSet.contains("familyEmail") && srcSet.contains("setFamilyEmail"), "");
        check("семья: отправка письма с полной копией и инструкцией",
                srcSet.contains("onFamilyShare") && srcSet.contains("ACTION_SEND")
                && srcSet.contains("EXTRA_EMAIL") && srcSet.contains("Backup.encode")
                && srcSet.contains("скопируйте всё ниже линии"), "");
        check("семья: импорт из текста письма (вставка)",
                srcSet.contains("Вставить текст") && srcSet.contains("showPasteImportDialog"), "");
        String manifest2 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        String srcImp = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/ImportBackupActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("семья: приложение зарегистрировано приёмником .json (Gmail-вложения)",
                manifest2.contains("ImportBackupActivity") && manifest2.contains("application/json")
                && manifest2.contains("android.intent.action.VIEW")
                && srcImp.contains("BackupUi.confirm") && srcImp.contains("MainActivity.class"), "");

        // ───────────────────────── 27. СЕРВЕРНАЯ СИНХРОНИЗАЦИЯ СЕМЬИ ─────────────────────────
        Storage stSync = new Storage(new Context());
        check("синхронизация: по умолчанию не привязана, после входа — семья/логин/токен",
                !stSync.syncLinked() && stSync.syncServer().startsWith("https://"), "");
        stSync.setSyncAccount("ивановы", "papa", "token123");
        stSync.setSyncLastTs(1726760000000L);
        check("синхронизация: учётка и метка времени сохраняются",
                stSync.syncLinked() && "ивановы".equals(stSync.syncFamily())
                && "papa".equals(stSync.syncLogin()) && "token123".equals(stSync.syncToken())
                && stSync.syncLastTs() == 1726760000000L, "");
        stSync.clearSyncAccount();
        check("синхронизация: выход очищает учётку", !stSync.syncLinked(), "");
        String srcSync = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/SyncClient.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("синхронизация: клиент знает все маршруты сервера",
                srcSync.contains("/api/family/create") && srcSync.contains("/api/family/join")
                && srcSync.contains("/api/state/push") && srcSync.contains("/api/state/pull")
                && srcSync.contains("/api/health") && srcSync.contains("NetErrors.ru"), "");
        check("синхронизация: настройки — создание/вход/кнопка обмена",
                srcSet.contains("showFamilyAccountDialog") && srcSet.contains("onSyncNow")
                && srcSet.contains("doSyncPush") && srcLay.contains("sync_create")
                && srcLay.contains("sync_join") && srcLay.contains("sync_now")
                && srcLay.contains("sync_server"), "");
        String srcMainSync = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("синхронизация: главный экран подсказывает об обновлениях от семьи",
                srcMainSync.contains("checkFamilyUpdates") && srcMainSync.contains("SyncClient.pull"), "");
        String serverPy = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("../sync-server/server.py").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("сервер: логин/пароль, PBKDF2, последний-новее-побеждает, health",
                serverPy.contains("pbkdf2_hmac") && serverPy.contains("token_hex")
                && serverPy.contains(">= ts") && serverPy.contains("/api/health")
                && serverPy.contains("FAMILY_RE"), "");
        check("сервер: автотест полного цикла семьи лежит рядом",
                new java.io.File("../sync-server/test_server.py").isFile(), "");

        // ───────────────────────── 28. СВОИ ФОТО КУЛЬТУР ─────────────────────────
        String srcPhotos = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PlantPhotos.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcSheet = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/CropInfoSheet.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("фото культур: хранилище ужимает и держит приватно",
                srcPhotos.contains("plant_photos") && srcPhotos.contains("MAX_SIDE")
                && srcPhotos.contains("saveFromUri") && srcPhotos.contains("inSampleSize"), "");
        check("фото культур: карточка показывает фото и кнопки",
                srcSheet.contains("PlantPhotos.has") && srcSheet.contains("btnPhoto")
                && srcSheet.contains("pendingPlantId") && srcSheet.contains("PlantPhotos.remove")
                && srcSheet.contains("Резервную копию не входит".replace("Резервную", "резервную")), "");
        check("фото культур: приёмник выбора зарегистрирован",
                manifest2.contains("PhotoPickActivity") && srcPhotos.contains("pendingPlantId")
                && manifest2.contains("android:theme=\"@android:style/Theme.Translucent.NoTitleBar\""), "");
        check("копия: метка времени извлекается из файла",
                Backup.timestamp(encodedBackup) > 0 && Backup.timestamp("мусор") == 0, "");
        String srcBui = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/BackupUi.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("восстановление: общий диалог показывает дату копии и число записей",
                srcBui.contains("Копия от") && srcBui.contains("записей") && srcBui.contains("importAll"), "");

        // ───────────────────────── 29. ЕЖЕНЕДЕЛЬНЫЙ ДАЙДЖЕСТ ─────────────────────────
        check("дайджест: склонения «работа/работы/работ»",
                DigestText.plural(1).equals("работа") && DigestText.plural(2).equals("работы")
                && DigestText.plural(4).equals("работы") && DigestText.plural(5).equals("работ")
                && DigestText.plural(11).equals("работ") && DigestText.plural(21).equals("работа"), "");
        Storage stDigest = new Storage(new Context());
        String[] emptyDigest = DigestText.weekly(stDigest);
        check("дайджест: структура всегда «заголовок + список»",
                emptyDigest.length == 2 && emptyDigest[0].startsWith("🌿 ")
                && emptyDigest[1].length() > 10, "");
        check("дайджест: без работ — «спокойная неделя» с советом; с работами — число и строки по дням",
                emptyDigest[0].contains("Спокойная неделя")
                        ? emptyDigest[1].contains("справочник")
                        : (emptyDigest[0].contains("работ") && emptyDigest[1].contains("▪")), "");
        stDigest.setPlants(plants);
        String[] fullDigest = DigestText.weekly(stDigest);
        check("дайджест: полный сад → структура сохраняется",
                fullDigest.length == 2 && fullDigest[0].startsWith("🌿 ")
                && fullDigest[1].length() > 10, "");
        String srcNt = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/Notifications.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcAl = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/AlarmReceiver.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcSt29 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/Storage.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("дайджест: планируется на воскресенье 10:00, обрабатывается и перезапускается",
                srcNt.contains("scheduleWeekly") && srcNt.contains("EXTRA_WEEKLY")
                && srcNt.contains("10, 0") && srcNt.contains("cancelWeekly")
                && srcAl.contains("showWeeklyDigest") && srcAl.contains("DigestText.weekly")
                && srcAl.contains("scheduleAll"), "");
        check("дайджест: переключатель в настройках",
                srcLay.contains("weekly_digest") && srcSet.contains("setWeeklyDigest")
                && srcSt29.contains("weeklyDigestEnabled"), "");
        check("дайджест: тихие часы соблюдаются",
                srcNt.contains("clampQuiet(atMs)"), "");

        // ── 30. Релизный pipeline: версия APK всегда берётся из тега ──
        String srcBuild = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("build.sh").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("CI: сборка читает версию из имени тега даже без env",
                srcBuild.contains("GITHUB_REF_NAME") && srcBuild.contains("MAJ * 10000")
                && srcBuild.contains("APP_VERSION_CODE=$(( MAJ"), "");
        check("CI: версия надёжно попадает в APK (sed-манифест + --replace-version)",
                srcBuild.contains("AndroidManifest.versioned.xml") && srcBuild.contains("--replace-version")
                && srcBuild.contains("android:versionCode=\\\"$APP_VERSION_CODE\\\""), "");

        // ───────────────────────── 25. ЛУННЫЙ КАЛЕНДАРЬ ─────────────────────────
        check("луна: опорное новолуние 06.01.2000 → возраст почти ноль (обёрнутый)",
                Moon.age(2000, 1, 6) > 28.9 && Moon.age(2000, 1, 6) < Moon.SYNODIC, "");
        check("луна: новолуние 15.09.2023 распознано",
                Moon.age(2023, 9, 15) < 1.5 && Moon.phaseIndex(2023, 9, 15) == 0, "");
        check("луна: полнолуние 29.09.2023 распознано",
                Moon.phaseIndex(2023, 9, 29) == 4, "");
        check("луна: возраст растёт день ото дня и корректно заворачивается в месяце",
                Moon.age(2023, 9, 16) > Moon.age(2023, 9, 15)
                && Moon.age(2023, 9, 14) > 27.0 && Moon.age(2023, 9, 14) < Moon.SYNODIC, "");
        check("луна: 8 фаз с эмодзи, названиями и советами",
                Moon.EMOJI.length == 8 && Moon.NAMES.length == 8
                && Moon.advice(0).length() > 10 && Moon.advice(4).length() > 10
                && Moon.emoji(2023, 9, 29).equals("🌕"), "");
        Calendar moonDay = Calendar.getInstance();
        moonDay.set(2023, 8, 29, 10, 0, 0);
        check("луна: строка-подсказка на день содержит фазу и совет",
                Moon.guide(moonDay).contains("🌕") && Moon.guide(moonDay).contains("урожай"), "");
        String srcCal = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/CalendarActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("луна: календарь показывает фазы в днях и совет на неделю",
                srcCal.contains("Moon.emoji") && srcCal.contains("Moon.guide"), "");

        // ── 31. Вредители: стадии развития с фото + Справочник почвы ──
        boolean stagesOk = PestStages.ids().size() == 58;
        for (String pid : PestStages.ids()) {
            PestStage[] ss = PestStages.forPest(pid);
            if (ss == null || ss.length != 4) { stagesOk = false; break; }
            for (PestStage st : ss) {
                if (st.title == null || st.title.length() < 3
                        || st.where == null || st.where.length() < 30
                        || st.harm == null || st.harm.length() < 30
                        || st.image == null || !st.image.startsWith("dzs_")
                        || !new java.io.File("res/drawable-nodpi/" + st.image + ".jpg").exists()) { stagesOk = false; break; }
            }
        }
        check("стадии: у 58 вредителей по 4 стадии «где развивается/какой вред», у каждой своё фото", stagesOk, "");

        boolean imgsOk = true;
        for (String img : new String[]{"soil_siderat", "soil_compost", "soil_mulch", "soil_ph", "soil_min", "soil_bio", "soil_diag", "soil_rotation", "soil_errors"}) {
            if (!new java.io.File("res/drawable-nodpi/" + img + ".jpg").exists()) { imgsOk = false; break; }
        }
        check("почва: фото разделов справочника лежат в drawable-nodpi", imgsOk, "");

        boolean soilOk = SoilGuide.INTRO.length() > 50 && SoilGuide.all().size() >= 8;
        for (SoilGuide.Section sec : SoilGuide.all()) {
            if (sec.tips == null || sec.tips.length < 4 || sec.body == null || sec.body.length() < 50
                    || sec.season == null || sec.season.length() < 3) { soilOk = false; break; }
        }
        check("почва: справочник восстановления плодородия (сидераты, органика, мульча, ротация)", soilOk, "");

        String srcDis31 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/DiseaseActivity.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        String srcMain31 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        String srcMan31 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        String srcLay31 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_main.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("вредители: в карточке показывается блок стадий развития", srcDis31.contains("PestStages.forPest"), "");
        check("почва: кнопка в главном меню, экран в манифесте",
                srcMain31.contains("btn_soil") && srcMan31.contains(".SoilActivity") && srcLay31.contains("btn_soil"), "");

        // ── 32. Быстрый переход: кнопки Вредители/Болезни ──
        boolean kindSplitOk = true;
        java.util.List<Disease> pests32 = new java.util.ArrayList<>();
        DiseaseDataPests.fill(pests32);
        for (Disease dz : pests32) {
            if (!dz.kind.startsWith("Вредитель")) { kindSplitOk = false; break; }
        }
        java.util.List<Disease> dis32 = new java.util.ArrayList<>();
        DiseaseDataA.fill(dis32); DiseaseDataB.fill(dis32); DiseaseDataC.fill(dis32);
        if (kindSplitOk) {
            for (Disease dz : dis32) {
                if (dz.kind.startsWith("Вредитель")) { kindSplitOk = false; break; }
            }
        }
        check("переходы: признак «Вредитель» в kind надёжно разделяет вредителей и болезни", kindSplitOk, "");

        check("переходы: кнопки Вредители/Болезни на главном экране и выбор культуры",
                srcLay31.contains("btn_pests") && srcLay31.contains("btn_diseases")
                && srcMain31.contains("btn_pests") && srcMain31.contains("btn_diseases")
                && srcMain31.contains("pickCulture"), "");
        check("переходы: фильтр кинда и переключатель разделов в карточках справочника",
                srcDis31.contains("kindFilter") && srcDis31.contains("matchesKind")
                && srcDis31.contains("Вредители") && srcDis31.contains("putExtra(\"kind\", kind)"), "");
        String srcPlants32 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PlantsActivity.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("переходы: на каждом растении кнопки своих вредителей и болезней",
                srcPlants32.contains("🐛") && srcPlants32.contains("🍂")
                && srcPlants32.contains("pest") && srcPlants32.contains("disease")
                && srcPlants32.contains("pestBtn") && srcPlants32.contains("dzBtn"), "");

        // ── 33. Экран «О приложении»: без хардкода версии + водяной знак ──
        String srcStr33 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values/strings.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        String srcAbout33 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_about.xml").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("о приложении: в заголовке нет захардкоженной версии («v2.5» удалено)",
                !srcStr33.contains("v2.5"), "");
        check("о приложении: баннер оставлен в карточке (без водяного знака)",
                srcAbout33.contains("fitCenter") && !srcAbout33.contains("android:alpha"), "");

        // ── 34. Водяной знак на остальных экранах ──
        String srcUi34 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/Ui.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("водяной знак: хелпер Ui.setContent с баннером и альфой 0.12",
                srcUi34.contains("WM_ALPHA") && srcUi34.contains("banner_garden")
                && srcUi34.contains("setContent(android.app.Activity activity, View content)"), "");
        String[] act34 = {"MainActivity", "SettingsActivity", "PlantsActivity", "TasksActivity",
                "CalendarActivity", "MaterialsActivity", "DiseaseActivity", "SoilActivity", "JournalActivity"};
        boolean wmOk = true;
        for (String a : act34) {
            String s34 = new String(java.nio.file.Files.readAllBytes(
                    new java.io.File("src/by/csl/gardener/" + a + ".java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
            if (!s34.contains("Ui.setContent")) { wmOk = false; break; }
        }
        check("водяной знак: все 9 основных экранов через Ui.setContent", wmOk, "");
        String srcAbout34 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/AboutActivity.java").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        check("о приложении: свой setContentView без водяного знака (там баннер)", !srcAbout34.contains("Ui.setContent"), "");

        // ── 35. Архив выполненных работ на главном экране ──
        check("архив: выполненные работы уходят из общего списка в отдельный блок",
                srcMain31.contains("archived.add(task)") && srcMain31.contains("task.done")
                && srcMain31.contains("Архив выполненных работ"), "");
        check("архив: состояние раскрытия сохраняется в настройках",
                srcMain31.contains("setArchiveOpen") && srcMain31.contains("archiveOpen()")
                && new String(java.nio.file.Files.readAllBytes(
                        new java.io.File("src/by/csl/gardener/Storage.java").toPath()),
                        java.nio.charset.StandardCharsets.UTF_8).contains("archive_open"), "");

        // ── 36. Единое верхнее меню главного экрана ──
        String xmlMain36 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_main.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        int statsPos36 = xmlMain36.indexOf("@+id/stats");
        String head36 = xmlMain36.substring(0, statsPos36);
        String tail36 = xmlMain36.substring(statsPos36);
        boolean allTop36 = true;
        int tiles36 = 0;
        String[] parts36 = head36.split("<Button");
        for (int k36 = 1; k36 < parts36.length; k36++) {
            String seg36 = parts36[k36];
            if (!seg36.contains("@drawable/card_bg\"") || !seg36.contains("72dp")
                    || !seg36.contains("\\n")) { allTop36 = false; }
            tiles36++;
        }
        check("меню: все 15 кнопок-плиток наверху, одного размера и стиля, с пиктограммой и подписью",
                tiles36 == 15 && allTop36, "");
        check("меню: ниже блока работ кнопок нет (только статистика и подвал-контакты)",
                !tail36.contains("<Button"), "");

        // ── 37. Режим участка и календарь посещения ──
        Storage rs37 = new Storage(new Context());
        check("режим участка: по умолчанию — постоянное проживание",
                rs37.residenceMode().equals("permanent"), "");
        rs37.setResidenceMode("dacha");
        check("режим участка: дачный режим сохраняется", rs37.residenceMode().equals("dacha"), "");
        check("календарь посещения: пуст по умолчанию", rs37.visitDays().isEmpty(), "");
        rs37.setVisitDay(2027, 9, 10, true);
        rs37.setVisitDay(2027, 9, 20, true);
        rs37.setVisitDay(2027, 9, 10, false);
        check("календарь посещения: отметки включаются и снимаются",
                rs37.visitDays().size() == 1 && rs37.isVisitDay(2027, 9, 20) && !rs37.isVisitDay(2027, 9, 10), "");

        rs37.setVisitDay(2027, 9, 10, true);
        Calendar f37 = Dates.at(2027, 9, 6);   // понедельник
        Calendar h37 = Dates.at(2027, 9, 19);  // воскресенье
        Calendar v37 = Planner.nextVisitDay(rs37, f37, h37);
        check("дачный режим: ближайший день присутствия — отмеченная пятница 10.09.2027",
                v37 != null && v37.get(Calendar.DAY_OF_MONTH) == 10, "");
        rs37.setVisitDay(2027, 9, 10, false);
        rs37.setVisitDay(2027, 9, 20, false);
        Calendar w37 = Planner.nextVisitDay(rs37, f37, h37);
        check("дачный режим: без отметок присутствие — выходные (суббота 11.09)",
                w37 != null && w37.get(Calendar.DAY_OF_MONTH) == 11, "");
        Calendar n37 = Planner.nextVisitDay(rs37, Dates.at(2027, 9, 13), Dates.at(2027, 9, 17));
        check("дачный режим: будни без отметок — дня присутствия нет",
                n37 == null, "");

        Context cV37 = new Context();
        Storage sV37 = new Storage(cV37);
        sV37.setPlants(oneCarrot);
        sV37.setResidenceMode("dacha");
        sV37.setVisitDay(2027, 9, 10, true);
        sV37.setVisitDay(2027, 9, 20, true);
        List<Task> vt37 = new Planner(sV37, w).tasks(25, Dates.at(2027, 9, 1));
        boolean visitOk37 = !vt37.isEmpty();
        for (Task t37 : vt37) {
            boolean okDay37 = (t37.month == 9 && t37.day == 10)
                    || (t37.month == 9 && t37.day == 20)
                    || (t37.month == 9 && t37.day >= 21);
            if (!okDay37) { visitOk37 = false; break; }
        }
        check("дачный режим: работы собираются к отмеченным визитам (10.09 и 20.09.2027)",
                visitOk37, "задач: " + vt37.size());

        String srcSet37 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/SettingsActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcVis37 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/VisitCalendarActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcMan37 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("настройки: выбор режима участка и кнопка календаря посещения",
                srcSet37.contains("residence_group") && srcSet37.contains("btn_visit_calendar")
                && srcSet37.contains("setResidenceMode"), "");
        check("календарь посещения: отметка дней и быстрые действия по месяцу",
                srcVis37.contains("setVisitDay") && srcVis37.contains("Отметить выходные")
                && srcVis37.contains("Снять отметки"), "");
        check("календарь посещения: экран зарегистрирован в манифесте",
                srcMan37.contains(".VisitCalendarActivity"), "");

        // ── 38. Дефициты минералов и микроэлементов ──
        check("дефициты: интро учит правилу «старые/молодые листья»",
                DeficiencyGuide.INTRO.contains("нижних") && DeficiencyGuide.INTRO.contains("верхних"), "");
        boolean defOk38 = DeficiencyGuide.all().size() >= 12;
        boolean hasOld38 = false, hasYoung38 = false;
        for (DeficiencyGuide.Item it38 : DeficiencyGuide.all()) {
            if (it38.symbol == null || it38.symbol.length() < 1 || it38.name.length() < 3
                    || it38.signs.length() < 40 || it38.mimic.length() < 20
                    || it38.fix.length() < 30 || it38.prevent.length() < 20) { defOk38 = false; break; }
            if (it38.youngLeaves) hasYoung38 = true; else hasOld38 = true;
        }
        check("дефициты: 12 элементов с признаками, помощью и профилактикой",
                defOk38 && hasOld38 && hasYoung38, "элементов: " + DeficiencyGuide.all().size());
        boolean markerOk38 = true;
        for (DeficiencyGuide.Item it38 : DeficiencyGuide.all()) {
            if (("N".equals(it38.symbol) || "P".equals(it38.symbol) || "K".equals(it38.symbol)
                    || "Mg".equals(it38.symbol)) && it38.youngLeaves) markerOk38 = false;
            if (("Fe".equals(it38.symbol) || "B".equals(it38.symbol) || "Ca".equals(it38.symbol))
                    && !it38.youngLeaves) markerOk38 = false;
        }
        check("дефициты: подвижные (N, P, K, Mg) — на старых листьях, малоподвижные (Ca, Fe, B) — на молодых",
                markerOk38, "");
        boolean excessOk38 = true;
        for (DeficiencyGuide.Item it38 : DeficiencyGuide.all()) {
            if (it38.excess == null || it38.excess.length() < 30) { excessOk38 = false; break; }
        }
        check("дефициты: у всех 12 элементов описан и переизбыток",
                excessOk38 && DeficiencyGuide.all().size() == 12, "");

        int withImg38 = 0;
        boolean imgsOk38 = true;
        for (DeficiencyGuide.Item it38 : DeficiencyGuide.all()) {
            if (it38.image == null) continue;
            withImg38++;
            if (!new java.io.File("res/drawable-nodpi/" + it38.image + ".jpg").exists()) { imgsOk38 = false; break; }
        }
        check("дефициты: фото всех 12 карточек лежат в drawable-nodpi",
                imgsOk38 && withImg38 == 12, "с фото: " + withImg38);
        String srcSoil38 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/SoilActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcDef38 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/DeficiencyActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcMan38 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("дефициты: блок открыт с экрана «Почва», экран в манифесте",
                srcSoil38.contains("DeficiencyActivity") && srcMan38.contains(".DeficiencyActivity")
                && srcDef38.contains("DeficiencyGuide.all"), "");

        // ── 39. Ручной перенос сроков ──
        Storage sh39 = new Storage(new Context());
        check("перенос: по умолчанию переносов нет", sh39.shiftedDate("t1") == null, "");
        sh39.setShifted("t1", 2027, 9, 12);
        int[] got39 = sh39.shiftedDate("t1");
        check("перенос: дата хранится и читается",
                got39 != null && got39[0] == 2027 && got39[1] == 9 && got39[2] == 12, "");
        sh39.clearShifted("t1");
        check("перенос: снятие одного переноса", sh39.shiftedDate("t1") == null, "");
        sh39.setShifted("t2", 2027, 10, 1);
        sh39.setShifted("t3", 2027, 10, 2);
        sh39.clearShifts();
        check("перенос: очистка всех переносов (новый сезон)",
                sh39.shiftedDate("t2") == null && sh39.shiftedDate("t3") == null, "");

        Context cS39 = new Context();
        Storage sS39 = new Storage(cS39);
        sS39.setPlants(oneCarrot);
        List<Task> base39 = new Planner(sS39, w).tasks(45, fixedStart);
        check("перенос: базовые задачи планируются", !base39.isEmpty(), "задач: " + base39.size());
        Task pick39 = base39.get(0);
        Calendar nd39 = Dates.plusDays(Dates.at(pick39.year, pick39.month, pick39.day), 5);
        sS39.setShifted(pick39.id, nd39.get(Calendar.YEAR), nd39.get(Calendar.MONTH) + 1, nd39.get(Calendar.DAY_OF_MONTH));
        List<Task> moved39 = new Planner(sS39, w).tasks(45, fixedStart);
        boolean movedOk39 = false;
        for (Task t39 : moved39) {
            if (t39.id.equals(pick39.id) && t39.year == nd39.get(Calendar.YEAR)
                    && t39.month == nd39.get(Calendar.MONTH) + 1
                    && t39.day == nd39.get(Calendar.DAY_OF_MONTH)) { movedOk39 = true; break; }
        }
        check("перенос: планировщик применяет ручной сдвиг (+5 дней)", movedOk39, "");
        String srcDlg39 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/TaskDialog.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcMain39 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_main.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("перенос: кнопки +1/+3/+7 и возврат в диалоге задачи",
                srcDlg39.contains("+7 дней") && srcDlg39.contains("Вернуть исходный срок"), "");
        check("минералы: отдельная плитка в главном меню",
                srcMain39.contains("btn_minerals"), "");

        // ── 40. Тёмная тема ──
        Storage th40 = new Storage(new Context());
        check("тема: по умолчанию — как в системе", th40.themeMode() == 0, "");
        th40.setThemeMode(2);
        check("тема: сохранение тёмного режима", th40.themeMode() == 2, "");
        th40.setThemeMode(1);
        check("тема: сохранение светлого режима", th40.themeMode() == 1, "");
        String colsDay40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values/colors.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String colsNight40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values-night/colors.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        java.util.Set<String> namesDay40 = new java.util.TreeSet<String>();
        java.util.regex.Matcher mA40 = java.util.regex.Pattern.compile("name=\"([a-z_0-9]+)\"").matcher(colsDay40);
        while (mA40.find()) { namesDay40.add(mA40.group(1)); }
        java.util.Set<String> namesNight40 = new java.util.TreeSet<String>();
        java.util.regex.Matcher mB40 = java.util.regex.Pattern.compile("name=\"([a-z_0-9]+)\"").matcher(colsNight40);
        while (mB40.find()) { namesNight40.add(mB40.group(1)); }
        check("тема: ночная палитра покрывает все цвета дневной",
                namesDay40.size() >= 15 && namesDay40.equals(namesNight40),
                "день: " + namesDay40.size() + ", ночь: " + namesNight40.size());
        String stylesDay40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values/styles.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String stylesNight40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values-night/styles.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("тема: тёмная тема — Material без Light, фон и текст из палитры",
                stylesNight40.contains("Theme.Material.NoActionBar") && !stylesNight40.contains("Light")
                && stylesNight40.contains("@color/bg") && stylesNight40.contains("@color/text_main"), "");
        check("тема: статус-бар в обеих темах через @color/bar",
                stylesDay40.contains("<item name=\"android:statusBarColor\">@color/bar</item>")
                && stylesNight40.contains("<item name=\"android:statusBarColor\">@color/bar</item>"), "");
        String card40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/drawable/card_bg.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String ok40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/drawable/ok_bg.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String warn40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/drawable/warn_bg.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("тема: карточки и плашки берут цвета из ресурсов (тёмные варианты)",
                card40.contains("@color/card_fill") && card40.contains("@color/card_stroke")
                && ok40.contains("@color/ok_bg") && warn40.contains("@color/warn_bg"), "");
        String srcUi40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/Ui.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("тема: обёртка контекста в Ui.applyTheme (до масштаба шрифта)",
                srcUi40.contains("UI_MODE_NIGHT_YES") && srcUi40.contains("UI_MODE_NIGHT_NO")
                && srcUi40.contains("createConfigurationContext")
                && srcUi40.contains("Fonts.applyFont(applyTheme(context))"), "");
        check("тема: программные цвета без «магических чисел»",
                !srcUi40.contains("-2300968") && srcUi40.contains("R.color.card_stroke"), "");
        String srcSet40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/SettingsActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String layoutSet40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_settings.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("тема: выбор в настройках — система / светлая / тёмная",
                layoutSet40.contains("theme_group") && layoutSet40.contains("theme_dark")
                && srcSet40.contains("setThemeMode") && srcSet40.contains("recreate()"), "");
        String srcMain40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("тема: главный экран перерисовывается при смене темы",
                srcMain40.contains("themeMode()") && srcMain40.contains("recreate()"), "");
        String man40 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher acts40 = java.util.regex.Pattern
                .compile("<activity\\b[^>]*?android:name=\"\\.([A-Za-z0-9_]+)\"").matcher(man40);
        int actsCount40 = 0, actsThemed40 = 0;
        while (acts40.find()) {
            actsCount40++;
            String srcAct40 = new String(java.nio.file.Files.readAllBytes(
                    new java.io.File("src/by/csl/gardener/" + acts40.group(1) + ".java").toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            if (srcAct40.contains("attachBaseContext") && srcAct40.contains("Ui.applyFont")) actsThemed40++;
        }
        check("тема: все экраны приложения применяют тему и шрифт",
                actsCount40 >= 12 && actsCount40 == actsThemed40,
                "экранов: " + actsCount40 + ", с темой: " + actsThemed40);

        // ── 41. Бюджет покупок и итоги сезона ──
        Context cS41 = new Context();
        Storage sS41 = new Storage(cS41);
        sS41.setPlants(oneCarrot);
        int year41 = fixedStart.get(Calendar.YEAR);
        Planner.Budget b41 = new Planner(sS41, w).seasonBudget(year41);
        check("бюджет: план покупок сезона посчитан",
                b41.planned > 0.0d && b41.positions >= 1 && b41.packs >= 1,
                "план: " + String.format(Locale.US, "%.2f", b41.planned) + " " + b41.currency
                + ", позиций: " + b41.positions);
        double sum41 = 0.0d;
        boolean monthsOk41 = b41.byMonth.length == 12;
        for (double v41 : b41.byMonth) {
            sum41 += v41;
            if (v41 < 0.0d) monthsOk41 = false;
        }
        check("бюджет: месячная разбивка сходится в план сезона",
                monthsOk41 && Math.abs(sum41 - b41.planned) < 0.01d,
                "сумма месяцев: " + String.format(Locale.US, "%.2f", sum41));
        check("бюджет: без отметок купленное равно нулю", b41.boughtCost == 0.0d, "");
        List<Task> year41tasks = new Planner(sS41, w).tasks(366, Dates.at(year41, 1, 1));
        String buyId41 = null;
        for (Task t41 : year41tasks) {
            for (Task.Item it41 : t41.items) {
                if (it41.price > 0.0d && !it41.alternative) {
                    for (Material m41 : Material.all()) {
                        if (m41.name.equals(it41.name)) { buyId41 = m41.id; break; }
                    }
                }
                if (buyId41 != null) break;
            }
            if (buyId41 != null) break;
        }
        check("бюджет: в сезоне есть покупаемые материалы", buyId41 != null, "");
        if (buyId41 != null) {
            java.util.List<String> one41 = new java.util.ArrayList<String>();
            one41.add(buyId41);
            sS41.setBought(one41);
            Planner.Budget bought41 = new Planner(sS41, w).seasonBudget(year41);
            check("бюджет: купленное учтено в итогах сезона",
                    bought41.boughtCost > 0.0d && bought41.boughtCost <= bought41.planned,
                    "куплено: " + String.format(Locale.US, "%.2f", bought41.boughtCost));
            sS41.setBought(new java.util.ArrayList<String>());
        }
        String srcPlan41 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/Planner.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcMat41 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MaterialsActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("бюджет: карточка итогов сезона на экране покупок",
                srcMat41.contains("Бюджет сезона") && srcMat41.contains("MONTHS_NOM")
                && srcPlan41.contains("seasonBudget"), "");
        boolean magic41 = true;
        String[] files41 = {"CalendarActivity", "MaterialsActivity", "TaskDialog", "CropInfoSheet", "Ui"};
        for (String fn41 : files41) {
            String src41 = new String(java.nio.file.Files.readAllBytes(
                    new java.io.File("src/by/csl/gardener/" + fn41 + ".java").toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            if (src41.contains("-14670049") || src41.contains("-10721696") || src41.contains("-14983648")
                    || src41.contains("-15374912") || src41.contains("-5091328") || src41.contains("-7695732")) {
                magic41 = false;
            }
        }
        check("тема: магические цвета заменены ресурсами (тёмная тема читаема)", magic41, "");
        String colsDay41 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values/colors.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String colsNight41 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/values-night/colors.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("тема: цвета warn_text и text_note в обеих палитрах",
                colsDay41.contains("warn_text") && colsNight41.contains("warn_text")
                && colsDay41.contains("text_note") && colsNight41.contains("text_note"), "");

        // ── 42. Итоги сезона: отчёт с отправкой ──
        Context cJ42 = new Context();
        Storage sJ42 = new Storage(cJ42);
        int year42 = Dates.today().get(Calendar.YEAR);
        String empty42 = ShareText.seasonSummary(sJ42.journal(Journal.MAX_ENTRIES),
                new Planner(sJ42, w).seasonBudget(year42), year42, "Минск и окрестности");
        check("итоги: пустой сезон — дружелюбный отчёт",
                empty42.contains("Итоги сезона") && empty42.contains("Выполнено работ: 0")
                && empty42.contains("пока ничего не отмечено") && empty42.contains("ZI Garden"), "");
        sJ42.addJournal("feed_root", "Морковь", "Подкормка моркови", "Удобрение для овощей");
        sJ42.addJournal("feed_root", "Морковь", "Вторая подкормка моркови", "");
        sJ42.addJournal("spray", "Яблоня", "Обработка от парши", "Хорус");
        String rep42 = ShareText.seasonSummary(sJ42.journal(Journal.MAX_ENTRIES),
                new Planner(sJ42, w).seasonBudget(year42), year42, "Минск и окрестности");
        check("итоги: работы посчитаны и сгруппированы по культурам",
                rep42.contains("Выполнено работ: 3") && rep42.contains("Морковь — 2")
                && rep42.contains("Яблоня — 1") && rep42.contains("Обработка от парши"), "");
        check("итоги: бюджет закупок в отчёте",
                rep42.contains("Закупки сезона") && rep42.contains("план:")
                && rep42.contains("куплено:"), "");
        String srcJrn42 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/JournalActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcShare42 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/ShareText.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("итоги: кнопка отправки в журнале сада",
                srcJrn42.contains("Отправить итоги сезона") && srcJrn42.contains("ACTION_SEND"), "");
        check("итоги: отчёт собирается в ShareText.seasonSummary",
                srcShare42.contains("seasonSummary") && srcShare42.contains("Закупки сезона"), "");

        // ── 43. Погода с графиком на главной ──
        Weather wF43 = new Weather();
        wF43.fetchedAt = System.currentTimeMillis();
        Calendar base43 = Dates.today();
        for (int k43 = 0; k43 < 10; k43++) {
            Calendar c43 = Dates.plusDays(base43, k43);
            Weather.Day d43 = new Weather.Day();
            d43.year = c43.get(Calendar.YEAR);
            d43.month = c43.get(Calendar.MONTH) + 1;
            d43.day = c43.get(Calendar.DAY_OF_MONTH);
            d43.tMax = 15.0d + k43;
            d43.tMin = 5.0d + k43;
            d43.precipMm = k43 % 3 == 0 ? 3.0d : 0.0d;
            d43.precipProb = 40.0d;
            d43.code = 1;
            wF43.days.add(d43);
        }
        List<ForecastModel.Cell> cells43 = ForecastModel.build(wF43, new java.util.ArrayList<Task>(), 7);
        check("погода-полоса: 7 дней из 10-дневного прогноза",
                cells43.size() == 7, "ячеек: " + cells43.size());
        check("погода-полоса: в ячейках день недели, дата и иконка",
                cells43.get(0).weekday.length() >= 2 && cells43.get(0).icon.length() > 0
                && cells43.get(0).day == base43.get(Calendar.DAY_OF_MONTH), "");
        check("погода-полоса: температура и осадки прокинуты в ячейки",
                cells43.get(0).tMax == 15.0d && cells43.get(0).tMin == 5.0d
                && cells43.get(0).precipMm == 3.0d, "");
        double[] range43 = ForecastModel.tempRange(cells43);
        check("погода-полоса: диапазон температур недели для столбиков",
                range43[0] == 5.0d && range43[1] == 21.0d,
                String.format(Locale.US, "%.1f…%.1f", Double.valueOf(range43[0]), Double.valueOf(range43[1])));
        check("погода-полоса: масштаб осадков не меньше 5 мм",
                ForecastModel.precipScale(cells43) >= 5.0d, "");
        Task wTask43 = new Task();
        wTask43.year = base43.get(Calendar.YEAR);
        wTask43.month = base43.get(Calendar.MONTH) + 1;
        wTask43.day = base43.get(Calendar.DAY_OF_MONTH);
        wTask43.weatherState = 1;
        java.util.List<Task> wTasks43 = new java.util.ArrayList<Task>();
        wTasks43.add(wTask43);
        List<ForecastModel.Cell> cells43b = ForecastModel.build(wF43, wTasks43, 7);
        check("погода-полоса: день с погодным сдвигом работы помечен точкой",
                cells43b.get(0).warn && !cells43b.get(1).warn, "");
        check("погода-полоса: без прогноза ячеек нет",
                ForecastModel.build(null, null, 7).isEmpty(), "");
        List<ForecastModel.Cell> cells43c = ForecastModel.build(wF43, null, 15);
        check("погода-полоса: полоса обрезается по длине прогноза",
                cells43c.size() == 10, "ячеек: " + cells43c.size());
        String srcMain43 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String layoutMain43 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_main.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcView43 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/ForecastView.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("погода-полоса: нарисована на главной (кастомный вид в разметке и коде)",
                layoutMain43.contains("by.csl.gardener.ForecastView")
                && srcMain43.contains("forecast_chart") && srcMain43.contains("ForecastModel.build")
                && srcView43.contains("onDraw") && srcView43.contains("setCells"), "");

        // ── 44. Учёт урожая ──
        Context cH44 = new Context();
        Storage sH44 = new Storage(cH44);
        check("урожай: изначально записей нет", sH44.harvestSummary(2027, "carrot").isEmpty(), "");
        sH44.addHarvest(2027, "carrot", 12.5d, "кг");
        sH44.addHarvest(2027, "carrot", 7.5d, "кг");
        check("урожай: сумма по культуре за год",
                "20.0 кг".equals(sH44.harvestSummary(2027, "carrot")), sH44.harvestSummary(2027, "carrot"));
        sH44.addHarvest(2027, "carrot", 5.0d, "шт");
        check("урожай: единицы считаются раздельно",
                "20.0 кг, 5.0 шт".equals(sH44.harvestSummary(2027, "carrot")), sH44.harvestSummary(2027, "carrot"));
        check("урожай: годы не смешиваются", sH44.harvestSummary(2026, "carrot").isEmpty(), "");
        sH44.addHarvest(2026, "carrot", 3.0d, "кг");
        check("урожай: история прошлого года хранится отдельно",
                "3.0 кг".equals(sH44.harvestSummary(2026, "carrot")), "");
        java.util.Map<String, String> h44 = new java.util.LinkedHashMap<String, String>();
        h44.put("Морковь", "20.0 кг");
        h44.put("Яблоня", "96.5 кг");
        String rep44 = ShareText.seasonSummary(new java.util.ArrayList<String>(), null, 2027,
                "Минск и окрестности", h44);
        check("итоги: урожай в отчёте сезона",
                rep44.contains("Урожай года") && rep44.contains("Морковь — 20.0 кг")
                && rep44.contains("Яблоня — 96.5 кг"), "");
        String rep44b = ShareText.seasonSummary(new java.util.ArrayList<String>(), null, 2027,
                "Минск и окрестности", null);
        check("итоги: без урожая блок не выводится", !rep44b.contains("Урожай года"), "");
        String srcUi44 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/Ui.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcDlg44 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/TaskDialog.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcHarv44 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/HarvestDialog.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcCrop44 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/CropInfoSheet.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcJrn44 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/JournalActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("урожай: вопрос при отметке «Сбор урожая» (карточка и диалог задачи)",
                srcUi44.contains("HarvestDialog.ask") && srcDlg44.contains("HarvestDialog.ask"), "");
        check("урожай: в карточке культуры итог, сравнение с прошлым годом и запись вручную",
                srcCrop44.contains("Записать урожай") && srcCrop44.contains("harvestSummary(yearNow - 1, str)"), "");
        check("урожай: диалог с единицами кг/шт/л и пропуском",
                srcHarv44.contains("кг") && srcHarv44.contains("шт") && srcHarv44.contains("л")
                && srcHarv44.contains("Пропустить"), "");
        check("урожай: итоги сезона собираются по всем культурам",
                srcJrn44.contains("harvestSummary(year, plant.id)"), "");


        // ── 45. Посадка растений: справочник с картинками ──
        String srcGuide45 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PlantingGuide.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcAct45 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PlantingActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String man45 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcMain45 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("посадка: в справочнике не меньше 20 культур",
                PlantingGuide.all().size() >= 20, "сейчас " + PlantingGuide.all().size());
        boolean fields45 = true;
        java.util.List<String> diagramIds45 = new java.util.ArrayList<>();
        for (PlantingGuide.Entry e45 : PlantingGuide.all()) {
            if (e45.dates == null || e45.dates.trim().isEmpty()) fields45 = false;
            if (e45.soil == null || e45.soil.trim().isEmpty()) fields45 = false;
            if (e45.pit == null || e45.pit.trim().isEmpty()) fields45 = false;
            if (e45.spacing == null || e45.spacing.trim().isEmpty()) fields45 = false;
            if (e45.care == null || e45.care.trim().isEmpty()) fields45 = false;
            if (e45.friends == null || e45.friends.trim().isEmpty()) fields45 = false;
            if (e45.foes == null || e45.foes.trim().isEmpty()) fields45 = false;
            if (e45.diagram == null || e45.diagram.trim().isEmpty()) fields45 = false;
        }
        check("посадка: у каждой культуры заполнены все разделы инструкции", fields45, "");
        boolean plants45 = true;
        for (PlantingGuide.Entry e45 : PlantingGuide.all()) {
            boolean ok45 = false;
            for (Plant p45 : Plant.all()) {
                if (p45.id.equals(e45.plantId)) { ok45 = true; break; }
            }
            if (!ok45) plants45 = false;
            diagramIds45.add(e45.diagram);
        }
        check("посадка: каждая запись соответствует культуре из справочника растений", plants45, "");
        boolean diagrams45 = true;
        java.io.File nodpi45 = new java.io.File("res/drawable-nodpi");
        for (String d45 : diagramIds45) {
            java.io.File f45 = new java.io.File(nodpi45, d45 + ".jpg");
            if (!f45.exists()) {
                f45 = new java.io.File(nodpi45, d45 + ".png");
            }
            if (!f45.exists()) { diagrams45 = false; System.out.println("  нет схемы: " + d45); }
        }
        check("посадка: у каждой культуры есть картинка-схема в ресурсах", diagrams45, "");
        check("посадка: у каждой культуры индивидуальная схема — без переиспользования",
                new java.util.HashSet<>(diagramIds45).size() == PlantingGuide.all().size(), "");
        check("посадка: поиск по культуре (apple есть, zzz нет)",
                PlantingGuide.byPlant("apple") != null && PlantingGuide.byPlant("zzz") == null, "");
        check("посадка: карточка с зумом картинок и всеми секциями",
                srcAct45.contains("Ui.zoomPhoto") && srcAct45.contains("Сроки посадки")
                && srcAct45.contains("Яма / лунка") && srcAct45.contains("соседи"), "");
        check("посадка: 13-я плитка меню зарегистрирована и подключена",
                xmlMain36.contains("@+id/btn_planting") && srcMain45.contains("R.id.btn_planting")
                && srcMain45.contains("PlantingActivity.class") && man45.contains(".PlantingActivity"), "");
        java.util.List<String> plantIds45 = new java.util.ArrayList<>();
        for (PlantingGuide.Entry e45 : PlantingGuide.all()) {
            plantIds45.add(e45.plantId);
        }
        check("посадка: в справочнике нет дублей культур",
                plantIds45.size() == new java.util.HashSet<>(plantIds45).size(), "");
        java.util.HashSet<String> covered45 = new java.util.HashSet<>(plantIds45);
        boolean fullCover45 = true;
        for (Plant p45 : Plant.all()) {
            if (!covered45.contains(p45.id)) fullCover45 = false;
        }
        check("посадка: каждая культура справочника растений покрыта инструкцией",
                fullCover45, "");


        // ── 46. Посадка: интерактивный выбор культуры ──
        String srcAct46 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PlantingActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("посадка: список культур с переходом к рекомендациям (tap по строке)",
                srcAct46.contains("putExtra(\"plantId\"") && srcAct46.contains("static void showFor"), "");
        check("посадка: рекомендации открываются по plantId и есть возврат к списку",
                srcAct46.contains("getStringExtra(\"plantId\"") && srcAct46.contains("Все культуры"), "");
        check("посадка: в строке выбора — иконка и название культуры",
                srcAct46.contains("plant.icon") && srcAct46.contains("plant.name"), "");
        check("посадка: в списке только культуры с инструкцией (byPlant != null)",
                srcAct46.contains("PlantingGuide.byPlant(p.id) != null"), "");


        // ── 47. Поиск по всем справочникам ──
        String srcSearch47 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/SearchActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("поиск: живой ввод (TextWatcher) и порог минимум 2 буквы",
                srcSearch47.contains("TextWatcher") && srcSearch47.contains("addTextChangedListener")
                && srcSearch47.contains("length() < 2"), "");
        check("поиск: ищет по всем справочникам (посадка, культуры, болезни, вредители, дефициты)",
                srcSearch47.contains("PlantingActivity.showFor") && srcSearch47.contains("CropInfoSheet.show")
                && srcSearch47.contains("DiseaseActivity.show") && srcSearch47.contains("DeficiencyActivity.show")
                && srcSearch47.contains("PlantingGuide.all()") && srcSearch47.contains("DiseaseDb.all(")
                && srcSearch47.contains("DeficiencyGuide.all()"), "");
        check("поиск: вредители отличаются от болезней по полю kind",
                srcSearch47.contains("startsWith(\"Вредитель\")") && srcSearch47.contains("\"pest\"")
                && srcSearch47.contains("\"disease\""), "");
        check("поиск: есть пустое состояние и стартовые примеры запросов",
                srcSearch47.contains("Ничего не нашлось") && srcSearch47.contains("Попробуйте, например:"), "");
        check("поиск: 14-я плитка меню зарегистрирована и подключена",
                xmlMain36.contains("@+id/btn_search") && srcMain45.contains("R.id.btn_search")
                && srcMain45.contains("SearchActivity.class") && man45.contains(".SearchActivity"), "");


        // ── 48. Экспорт данных в CSV ──
        Storage st48 = new Storage(new Context());
        st48.addJournal("spray", "apple", "Обработка «Актарой», 1.2 мл", "aktara");
        st48.addJournal(Operation.PRUNE, "pear", "Обрезка кроны \"по весне\"", "");
        st48.addHarvest(2026, "apple", 12.5, "кг");
        st48.addHarvest(2026, "apple", 3.0, "шт");
        st48.addHarvest(2025, "apple", 8.0, "кг");
        check("csv: экранирование кавычек и разделителей в ячейках",
                CsvExport.cell("a,b").equals("\"a,b\"")
                && CsvExport.cell("он сказал \"да\"").equals("\"он сказал \"\"да\"\"\"")
                && CsvExport.cell("просто").equals("просто"), "");
        String csvJ48 = CsvExport.journal(st48, 200);
        check("csv: журнал с заголовком, культурами и операциями по-русски",
                csvJ48.startsWith("Дата;Культура;Операция;Работа;Материалы")
                && csvJ48.contains("Яблоня") && csvJ48.contains("Груша")
                && csvJ48.contains("Обработка (опрыскивание)") && csvJ48.contains("Обрезка"), "");
        check("csv: ячейки с кавычками и запятыми обёрнуты и удвоены",
                csvJ48.contains("\"Обработка «Актарой», 1.2 мл\"")
                && csvJ48.contains("\"Обрезка кроны \"\"по весне\"\"\""), "");
        check("csv: годы урожая перечисляются по возрастанию",
                st48.harvestYears().size() == 2 && st48.harvestYears().get(0).intValue() == 2025
                && st48.harvestYears().get(1).intValue() == 2026, "");
        String csvH48 = CsvExport.harvest(st48);
        check("csv: урожай по годам, культурам и единицам",
                csvH48.startsWith("Год;Культура;Единица;Количество")
                && csvH48.contains("2026;Яблоня;кг;12.5")
                && csvH48.contains("2026;Яблоня;шт;3.0")
                && csvH48.contains("2025;Яблоня;кг;8.0"), "");
        String srcJrn48 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/JournalActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("csv: кнопка экспорта в журнале с выбором набора данных",
                srcJrn48.contains("Экспорт в CSV") && srcJrn48.contains("CsvExport.journal(")
                && srcJrn48.contains("CsvExport.harvest(") && srcJrn48.contains("createChooser"), "");


        // ── 49. Фото-дневник культуры ──
        java.io.File root49 = new java.io.File("out-test/photo49");
        if (root49.isDirectory()) {
            java.io.File[] lvl1 = root49.listFiles();
            if (lvl1 != null) {
                for (java.io.File f : lvl1) {
                    if (f.isDirectory()) {
                        java.io.File[] lvl2 = f.listFiles();
                        if (lvl2 != null) {
                            for (java.io.File g : lvl2) {
                                if (g.isDirectory()) {
                                    java.io.File[] lvl3 = g.listFiles();
                                    if (lvl3 != null) {
                                        for (java.io.File h : lvl3) {
                                            h.delete();
                                        }
                                    }
                                }
                                g.delete();
                            }
                        }
                    }
                    f.delete();
                }
            }
            root49.delete();
        }
        java.io.File ph1 = PhotoDiary.add(root49, "apple",
                new java.io.ByteArrayInputStream(new byte[]{1, 2, 3}), 1727000000000L);
        PhotoDiary.add(root49, "apple",
                new java.io.ByteArrayInputStream(new byte[]{4, 5}), 1727000001000L);
        check("фото: сохраняется в photos/<культура>/<время>.jpg",
                ph1 != null && ph1.exists()
                && ph1.getParentFile().getName().equals("apple")
                && ph1.getParentFile().getParentFile().getName().equals("photos"), "");
        java.util.List<java.io.File> phList49 = PhotoDiary.photos(root49, "apple");
        check("фото: список по возрастанию времени",
                phList49.size() == 2
                && phList49.get(0).getName().compareTo(phList49.get(1).getName()) < 0, "");
        check("фото: метка времени читается из имени",
                PhotoDiary.takenAt(ph1) == 1727000000000L, "");
        check("фото: чужая культура не видит фото",
                PhotoDiary.photos(root49, "pear").isEmpty(), "");
        check("фото: удаление файла",
                PhotoDiary.delete(phList49.get(0)) && PhotoDiary.photos(root49, "apple").size() == 1, "");
        java.io.File phEvil = PhotoDiary.add(root49, "../evil",
                new java.io.ByteArrayInputStream(new byte[]{6}), 5L);
        check("фото: id культуры экранируется в безопасное имя каталога",
                phEvil != null && phEvil.getParentFile().getName().equals("___evil")
                && !phEvil.getParentFile().getAbsolutePath().contains("..evil"), "");
        String srcPhotos49 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PhotosActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcCrop49 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/CropInfoSheet.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("фото: экран с выбором из галереи и сохранением результата",
                srcPhotos49.contains("ACTION_GET_CONTENT") && srcPhotos49.contains("onActivityResult")
                && srcPhotos49.contains("PhotoDiary.add(") && srcPhotos49.contains("image/*"), "");
        check("фото: просмотр, удаление с подтверждением и масштабирование",
                srcPhotos49.contains("Удалить это фото?") && srcPhotos49.contains("setOnLongClickListener")
                && srcPhotos49.contains("inSampleSize"), "");
        check("фото: вход из карточки культуры со счётчиком",
                srcCrop49.contains("Фото-дневник") && srcCrop49.contains("PhotoDiary.count(")
                && srcCrop49.contains("PhotosActivity.show("), "");
        check("фото: PhotosActivity зарегистрирована в манифесте",
                man45.contains(".PhotosActivity"), "");


        // ── 50. Агроприёмы: обрезка, рассада, размножение ──
        String srcTech50 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/TechniqueGuide.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("приёмы: в справочнике не меньше 13 агроприёмов",
                TechniqueGuide.all().size() >= 13, "сейчас " + TechniqueGuide.all().size());
        boolean techFields50 = true;
        java.util.List<String> techDiagrams50 = new java.util.ArrayList<>();
        java.util.List<String> techGroups50 = new java.util.ArrayList<>();
        for (TechniqueGuide.Item it50 : TechniqueGuide.all()) {
            if (it50.title == null || it50.title.trim().isEmpty()) techFields50 = false;
            if (it50.whenText == null || it50.whenText.trim().isEmpty()) techFields50 = false;
            if (it50.tools == null || it50.tools.trim().isEmpty()) techFields50 = false;
            if (it50.how == null || it50.how.trim().isEmpty()) techFields50 = false;
            if (it50.mistakes == null || it50.mistakes.trim().isEmpty()) techFields50 = false;
            if (it50.diagram == null || it50.diagram.trim().isEmpty()) techFields50 = false;
            techDiagrams50.add(it50.diagram);
            if (!techGroups50.contains(it50.group)) techGroups50.add(it50.group);
        }
        check("приёмы: у каждого приёма заполнены все разделы и схема", techFields50, "");
        boolean techDiagFiles50 = true;
        java.io.File nodpiTech50 = new java.io.File("res/drawable-nodpi");
        for (String d50 : techDiagrams50) {
            if (!new java.io.File(nodpiTech50, d50 + ".jpg").exists()) {
                techDiagFiles50 = false;
                System.out.println("  нет схемы приёма: " + d50);
            }
        }
        check("приёмы: файл каждой схемы существует в ресурсах", techDiagFiles50, "");
        check("приёмы: схемы уникальны — по одной на приём",
                new java.util.HashSet<>(techDiagrams50).size() == TechniqueGuide.all().size(), "");
        check("приёмы: три раздела — обрезка, рассада, размножение",
                techGroups50.size() == 3 && techGroups50.contains("Обрезка и формировка")
                && techGroups50.contains("Рассада") && techGroups50.contains("Размножение"), "");
        check("приёмы: поиск по id (pruning_spring есть, zzz нет)",
                TechniqueGuide.byId("pruning_spring") != null && TechniqueGuide.byId("zzz") == null, "");
        check("приёмы: партия 2 — деление куста, усы земляники, семена",
                TechniqueGuide.byId("prop_division") != null
                && TechniqueGuide.byId("prop_runners") != null
                && TechniqueGuide.byId("prop_seeds") != null
                && TechniqueGuide.byId("prop_division").diagram.equals("prop_division")
                && TechniqueGuide.byId("prop_runners").diagram.equals("prop_runners")
                && TechniqueGuide.byId("prop_seeds").diagram.equals("prop_seeds")
                && "Размножение".equals(TechniqueGuide.byId("prop_division").group)
                && "Размножение".equals(TechniqueGuide.byId("prop_runners").group)
                && "Размножение".equals(TechniqueGuide.byId("prop_seeds").group), "");
        check("приёмы: экран посадки показывает приёмы и открывает по technique",
                srcAct45.contains("Агроприёмы") && srcAct45.contains("putExtra(\"technique\"")
                && srcAct45.contains("TechniqueGuide.byId(technique)")
                && srcAct45.contains("Когда проводить") && srcAct45.contains("Техника шаг за шагом")
                && srcAct45.contains("Частые ошибки"), "");


        // ── 51. Онбординг: знакомство при первом запуске ──
        Storage stOb51 = new Storage(new Context());
        check("онбординг: до прохождения флаг не выставлен",
                !stOb51.onboardingDone(), "");
        stOb51.setOnboardingDone(true);
        check("онбординг: после прохождения флаг выставлен",
                stOb51.onboardingDone(), "");
        String srcOb51 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/OnboardingActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher obM51 = java.util.regex.Pattern.compile(
                "new Slide\\(\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"\\s*\\)",
                java.util.regex.Pattern.DOTALL).matcher(srcOb51);
        int obCount51 = 0;
        boolean obFields51 = true;
        while (obM51.find()) {
            obCount51++;
            if (obM51.group(2).trim().length() < 5) obFields51 = false;
            if (obM51.group(3).trim().length() < 50) obFields51 = false;
            if (obM51.group(4).trim().length() < 10) obFields51 = false;
        }
        check("онбординг: не меньше 5 экранов знакомства",
                obCount51 >= 5, "найдено " + obCount51);
        check("онбординг: у каждого экрана заполнены заголовок, текст и подсказка «где найти»",
                obFields51 && obCount51 >= 5, "");
        check("онбординг: первый показ — только пока флаг не выставлен",
                srcOb51.contains("showIfNeeded") && srcOb51.contains("!store.onboardingDone()")
                && srcOb51.contains("setOnboardingDone(true)"), "");
        String srcMain51 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcAbout51 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/AboutActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcMan51 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcLayout51 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_about.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("онбординг: главный экран показывает его при первом запуске",
                srcMain51.contains("OnboardingActivity.showIfNeeded(this)"), "");
        check("онбординг: повторный показ из «О программе» (кнопка + разметка)",
                srcAbout51.contains("OnboardingActivity.show(")
                && srcAbout51.contains("R.id.show_onboarding")
                && srcLayout51.contains("@+id/show_onboarding"), "");
        check("онбординг: активность зарегистрирована в манифесте",
                srcMan51.contains(".OnboardingActivity"), "");

        // ── 52. Фенология: природный календарь работ ──
        boolean phFields52 = true;
        java.util.List<String> phSeasons52 = new java.util.ArrayList<>();
        for (PhenologyGuide.Sign ph52 : PhenologyGuide.all()) {
            if (ph52.emoji == null || ph52.emoji.trim().isEmpty()) phFields52 = false;
            if (ph52.title == null || ph52.title.trim().length() < 5) phFields52 = false;
            if (ph52.period == null || ph52.period.trim().length() < 5) phFields52 = false;
            if (ph52.signal == null || ph52.signal.trim().length() < 15) phFields52 = false;
            if (ph52.works == null || ph52.works.trim().length() < 20) phFields52 = false;
            if (!phSeasons52.contains(ph52.season)) phSeasons52.add(ph52.season);
        }
        check("фенология: в справочнике не меньше 18 природных ориентиров",
                PhenologyGuide.all().size() >= 18, "сейчас " + PhenologyGuide.all().size());
        check("фенология: у каждого ориентира заполнены эмодзи, название, срок, сигнал и работы",
                phFields52, "");
        check("фенология: четыре сезона, в каждом не меньше 2 ориентиров",
                phSeasons52.size() == 4 && phSeasons52.contains("Весна") && phSeasons52.contains("Лето")
                && phSeasons52.contains("Осень") && phSeasons52.contains("Зима")
                && PhenologyGuide.bySeason("Весна").size() >= 6
                && PhenologyGuide.bySeason("Лето").size() >= 5
                && PhenologyGuide.bySeason("Осень").size() >= 5
                && PhenologyGuide.bySeason("Зима").size() >= 2, "");
        boolean phSeasonFor52 = true;
        for (int m52 = 1; m52 <= 12; m52++) {
            String expect52;
            if (m52 >= 3 && m52 <= 5) expect52 = "Весна";
            else if (m52 >= 6 && m52 <= 8) expect52 = "Лето";
            else if (m52 >= 9 && m52 <= 11) expect52 = "Осень";
            else expect52 = "Зима";
            if (!expect52.equals(PhenologyGuide.seasonFor(m52))) phSeasonFor52 = false;
        }
        check("фенология: сезон по месяцу считается верно для всех 12 месяцев", phSeasonFor52, "");
        boolean phSummary52 = true;
        for (String s52 : new String[]{"Весна", "Лето", "Осень", "Зима"}) {
            if (PhenologyGuide.seasonSummary(s52) == null
                    || PhenologyGuide.seasonSummary(s52).trim().length() < 40) phSummary52 = false;
        }
        check("фенология: сводка «что сейчас» есть для каждого сезона", phSummary52, "");
        check("фенология: честная оговорка о сверке с прогнозом",
                PhenologyGuide.INTRO != null && PhenologyGuide.INTRO.contains("прогноз"), "");
        String srcMain52 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcLayout52 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_main.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcMan52 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("AndroidManifest.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("фенология: кнопка на главном экране (разметка + проводка)",
                srcLayout52.contains("@+id/btn_phenology")
                && srcMain52.contains("R.id.btn_phenology")
                && srcMain52.contains("PhenologyActivity.class"), "");
        check("фенология: активность зарегистрирована в манифесте",
                srcMan52.contains(".PhenologyActivity"), "");

        // ── 53. Поиск по агроприёмам и фенологии ──
        TechniqueGuide.Item graft53 = TechniqueGuide.byId("prop_grafting");
        check("поиск приёмов: прививка находится по слову из названия",
                graft53 != null && Search.matchesTechnique(graft53, "прививк"), "");
        check("поиск приёмов: находится по слову из содержания (копулировка)",
                graft53 != null && Search.matchesTechnique(graft53, "копулировка"), "");
        check("поиск приёмов: несколько слов — все должны встретиться",
                graft53 != null && Search.matchesTechnique(graft53, "прививка весна")
                && !Search.matchesTechnique(graft53, "прививка борщевик"), "");
        check("поиск приёмов: пустой запрос показывает всё, чужое слово — нет",
                graft53 != null && Search.matchesTechnique(graft53, "   ")
                && !Search.matchesTechnique(graft53, "азот"), "");
        PhenologyGuide.Sign cherry53 = null;
        for (PhenologyGuide.Sign s53 : PhenologyGuide.all()) {
            if (s53.title.toLowerCase().contains("черёмуха")) {
                cherry53 = s53;
            }
        }
        check("поиск примет: черёмуха находится по названию",
                cherry53 != null && Search.matchesSign(cherry53, "черёмух"), "");
        check("поиск примет: находится по слову из работ (картофель)",
                cherry53 != null && Search.matchesSign(cherry53, "картоф"), "");
        check("поиск примет: пустой запрос показывает всё, чужое слово — нет",
                cherry53 != null && Search.matchesSign(cherry53, "")
                && !Search.matchesSign(cherry53, "борщевик"), "");
        String srcSearch53 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/SearchActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcPlant53 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PlantingActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcOb53 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/OnboardingActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("поиск: разделы агроприёмов и фенологии на экране поиска",
                srcSearch53.contains("addTechniques") && srcSearch53.contains("addPhenology")
                && srcSearch53.contains("Агроприёмы") && srcSearch53.contains("Фенология")
                && srcSearch53.contains("Search.matchesTechnique")
                && srcSearch53.contains("Search.matchesSign"), "");
        check("поиск: переходы — приём открывает инструкцию, примета — фенологию",
                srcSearch53.contains("PlantingActivity.showTechnique")
                && srcSearch53.contains("PhenologyActivity.show")
                && srcPlant53.contains("showTechnique"), "");
        check("онбординг: слайд «Схемы, фенология, виджеты» упоминает фенологию",
                srcOb53.contains("Схемы, фенология, виджеты")
                && srcOb53.contains("черёмуховых холодов"), "");

        // ── 54. Фенология на главном экране и выбор культур из онбординга ──
        check("главный экран: эмодзи сезонов из справочника",
                "🌱".equals(PhenologyGuide.seasonEmoji("Весна"))
                && "☀️".equals(PhenologyGuide.seasonEmoji("Лето"))
                && "🍂".equals(PhenologyGuide.seasonEmoji("Осень"))
                && "❄️".equals(PhenologyGuide.seasonEmoji("Зима")), "");
        boolean phCard54 = true;
        for (int m54 = 1; m54 <= 12; m54++) {
            if (PhenologyGuide.bySeason(PhenologyGuide.seasonFor(m54)).size() < 2) phCard54 = false;
        }
        check("главный экран: у карточки «Сейчас в природе» есть приметы в любом месяце",
                phCard54, "");
        String srcMain54 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/MainActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcLayout54 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("res/layout/activity_main.xml").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("главный экран: карточка «Сейчас в природе» в разметке и заполняется кодом",
                srcLayout54.contains("@+id/phenology_card")
                && srcMain54.contains("R.id.phenology_card")
                && srcMain54.contains("setupPhenologyCard")
                && srcMain54.contains("PhenologyActivity.show(MainActivity.this)"), "");
        int statsPos54 = srcLayout54.indexOf("@+id/stats");
        check("главный экран: карточка фенологии — не кнопка, меню не тронуто (15 плиток до статистики)",
                statsPos54 > 0 && srcLayout54.substring(0, statsPos54).contains("@+id/phenology_card")
                && srcLayout54.split("<Button").length == 16, "");
        String srcOb54 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/OnboardingActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        String srcPlants54 = new String(java.nio.file.Files.readAllBytes(
                new java.io.File("src/by/csl/gardener/PlantsActivity.java").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        check("онбординг: на последнем экране — «Сразу выбрать свои культуры»",
                srcOb54.contains("Сразу выбрать свои культуры")
                && srcOb54.contains("PlantsActivity.show")
                && srcPlants54.contains("public static void show"), "");

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
