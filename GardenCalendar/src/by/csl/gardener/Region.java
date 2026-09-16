package by.csl.gardener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Region {
    private static final Map<String, Region> ALL = build();
    public final String currencyCode;
    public final String displayName;
    public final String id;
    private final Set<String> notTypical;
    public final String note;
    public final int phenoShiftDays;
    private final double priceFactor;
    private final Map<String, Double> prices;
    private final Set<String> recommended;
    public final String symbol;

    private Region(String str, String str2, String str3, String str4, String str5, int i, Map<String, Double> map, double d, String[] strArr, String[] strArr2) {
        this.id = str;
        this.displayName = str2;
        this.currencyCode = str3;
        this.symbol = str4;
        this.note = str5;
        this.phenoShiftDays = i;
        this.prices = map == null ? new HashMap<>() : map;
        this.priceFactor = d;
        this.recommended = new HashSet();
        if (strArr != null) {
            for (String str6 : strArr) {
                this.recommended.add(str6);
            }
        }
        this.notTypical = new HashSet();
        if (strArr2 != null) {
            for (String str7 : strArr2) {
                this.notTypical.add(str7);
            }
        }
    }

    private static Map<String, Region> build() {
        HashMap hashMap = new HashMap();
        hashMap.put("by", new Region("by", "Беларусь (средняя полоса)", "BYN", "BYN", "Цены в BYN по рознице РБ; препараты из Госреестра СЗР Республики Беларусь.", 0, null, 1.0d, null, null));
        HashMap hashMap2 = new HashMap();
        Double valueOf = Double.valueOf(45.0d);
        hashMap2.put("urea", valueOf);
        Double valueOf2 = Double.valueOf(50.0d);
        hashMap2.put("ammonium_nitrate", valueOf2);
        Double valueOf3 = Double.valueOf(70.0d);
        hashMap2.put("azofoska", valueOf3);
        Double valueOf4 = Double.valueOf(65.0d);
        hashMap2.put("superphosphate", valueOf4);
        hashMap2.put("potassium_sulfate", Double.valueOf(140.0d));
        Double valueOf5 = Double.valueOf(90.0d);
        hashMap2.put("kalmag", valueOf5);
        hashMap2.put("ammonium_sulfate", Double.valueOf(55.0d));
        Double valueOf6 = Double.valueOf(120.0d);
        hashMap2.put("dolomite", valueOf6);
        Double valueOf7 = Double.valueOf(60.0d);
        hashMap2.put("ash", valueOf7);
        hashMap2.put("iron_sulfate", valueOf5);
        Double valueOf8 = Double.valueOf(150.0d);
        hashMap2.put("copper_sulfate", valueOf8);
        Double valueOf9 = Double.valueOf(100.0d);
        hashMap2.put("manure", valueOf9);
        Double valueOf10 = Double.valueOf(110.0d);
        hashMap2.put("chicken_manure", valueOf10);
        Double valueOf11 = Double.valueOf(250.0d);
        hashMap2.put("peat", valueOf11);
        Double valueOf12 = Double.valueOf(350.0d);
        hashMap2.put("bark", valueOf12);
        hashMap2.put("azofos", valueOf11);
        hashMap2.put("bordeaux", valueOf6);
        Double valueOf13 = Double.valueOf(200.0d);
        hashMap2.put("bordeaux_liquid", valueOf13);
        hashMap2.put("horus", Double.valueOf(180.0d));
        hashMap2.put("skor", valueOf5);
        Double valueOf14 = Double.valueOf(160.0d);
        hashMap2.put("rayok", valueOf14);
        hashMap2.put("topaz", Double.valueOf(80.0d));
        hashMap2.put("fitosporin", valueOf8);
        hashMap2.put("phytoverm", valueOf7);
        hashMap2.put("bitoxibacillin", valueOf3);
        hashMap2.put("lepidocide", valueOf7);
        hashMap2.put("aktara", Double.valueOf(130.0d));
        hashMap2.put("alatar", valueOf10);
        hashMap2.put("aliot", valueOf8);
        hashMap2.put("biotlin", valueOf2);
        hashMap2.put("sulfur", valueOf3);
        hashMap2.put("tornado", Double.valueOf(260.0d));
        hashMap2.put("garden_tar", valueOf14);
        hashMap2.put("zsp", valueOf6);
        hashMap2.put("whitewash", valueOf13);
        hashMap2.put("glue_belt", valueOf9);
        hashMap2.put("pheromone", valueOf8);
        hashMap2.put("spunbond", valueOf12);
        hashMap2.put("lapnik", valueOf9);
        hashMap2.put("humate", valueOf6);
        hashMap2.put("epin", valueOf7);
        hashMap2.put("blueberry_mix", valueOf11);
        Double valueOf15 = Double.valueOf(220.0d);
        hashMap2.put("rose_mix", valueOf15);
        hashMap2.put("lawn_spring", Double.valueOf(400.0d));
        hashMap2.put("lawn_autumn", Double.valueOf(420.0d));
        hashMap2.put("seedling_soil", valueOf12);
        hashMap2.put("peat_tablets", valueOf11);
        hashMap2.put("cassette", valueOf8);
        hashMap2.put("phytolamp", Double.valueOf(600.0d));
        Double valueOf16 = Double.valueOf(300.0d);
        hashMap2.put("agrofibre_17", valueOf16);
        hashMap2.put("agrofibre_40", Double.valueOf(450.0d));
        hashMap2.put("gh_film", Double.valueOf(1100.0d));
        Double valueOf17 = Double.valueOf(40.0d);
        hashMap2.put("seeds_veg", valueOf17);
        hashMap2.put("veg_fertilizer", valueOf13);
        hashMap2.put("potato_fertilizer", valueOf16);
        hashMap2.put("hom", valueOf3);
        hashMap2.put("previkur", valueOf12);
        hashMap2.put("confidor", valueOf3);
        hashMap2.put("actophyt", valueOf15);
        hashMap2.put("fitosporin_tomato", valueOf5);
        hashMap2.put("lime", valueOf9);
        hashMap2.put("potassium_permanganate", valueOf2);
        hashMap2.put("sulfur_check", valueOf13);
        hashMap2.put("metaldehyde", valueOf9);
        hashMap2.put("mustard_siderate", valueOf8);
        Double valueOf18 = Double.valueOf(30.0d);
        hashMap2.put("ash_husk", valueOf18);
        hashMap2.put("trellis", valueOf8);
        hashMap.put("ru", new Region("ru", "Россия (средняя полоса)", "RUB", "₽", "Цены ориентировочно в ₽ по рознице РФ; препараты из Госреестра СЗР России.", 0, hashMap2, 30.0d, null, null));
        hashMap.put("crimea", new Region("crimea", "Крым (южная зона)", "RUB", "₽", "Южный климат: весенние работы на 2–3 недели раньше, главное — полив; зимние укрытия обычно не нужны. Цены в ₽ по рынку РФ.", 20, hashMap2, 30.0d, new String[]{"peach", "apricot", "sweet_cherry", "cherry", "grape", "quince", "plum", "cherry_plum", "rose"}, new String[]{"blueberry", "sea_buckthorn", "honeysuckle"}));
        HashMap hashMap3 = new HashMap();
        hashMap3.put("urea", Double.valueOf(20.0d));
        hashMap3.put("superphosphate", valueOf18);
        hashMap3.put("potassium_sulfate", valueOf4);
        hashMap3.put("bordeaux", valueOf7);
        hashMap3.put("skor", valueOf);
        hashMap3.put("aktara", valueOf4);
        hashMap3.put("fitosporin", valueOf3);
        hashMap3.put("hom", Double.valueOf(35.0d));
        hashMap3.put("iron_sulfate", valueOf17);
        hashMap3.put("copper_sulfate", valueOf3);
        hashMap.put("ua", new Region("ua", "Украина", "UAH", "₴", "Цены ориентировочно в ₴; сроки работ на 7–10 дней раньше среднеросских.", 10, hashMap3, 12.5d, new String[]{"apricot", "peach", "grape", "sweet_cherry", "cherry"}, new String[]{"sea_buckthorn"}));
        hashMap.put("other", new Region("other", "регион не определён", "BYN", "BYN", "Для справочных расчётов используются данные Беларуси (BYN); сроки — как для средней полосы.", 0, null, 1.0d, null, null));
        return hashMap;
    }

    public static java.util.Collection<Region> allRegions() {
        return ALL.values();
    }

    public java.util.Map<String, Double> prices() {
        return prices;
    }

    public static Region byId(String str) {
        Map<String, Region> map = ALL;
        Region region = map.get(str);
        return region == null ? map.get("by") : region;
    }

    public static Region detect(double d, double d2) {
        if (d >= 44.2d && d <= 46.3d && d2 >= 32.3d && d2 <= 36.8d) {
            return byId("crimea");
        }
        if (d >= 51.2d && d <= 56.2d && d2 >= 23.1d && d2 <= 32.8d) {
            return byId("by");
        }
        if (d >= 44.2d && d <= 52.4d && d2 >= 22.1d && d2 <= 40.2d) {
            return byId("ua");
        }
        if (d >= 41.0d && d <= 70.0d && d2 >= 19.0d && d2 <= 60.0d) {
            return byId("ru");
        }
        return byId("other");
    }

    public double priceOf(Material material) {
        Double d = this.prices.get(material.id);
        if (d != null) {
            return d.doubleValue();
        }
        return this.priceFactor == 1.0d ? material.price : Math.round(material.price * this.priceFactor);
    }

    public boolean skips(Rule rule) {
        if ("crimea".equals(this.id) && Operation.PROTECT.equals(rule.op)) {
            return rule.title.contains("Укрытие") || rule.title.contains("укрытие") || rule.title.contains("Пригибание");
        }
        return false;
    }

    public boolean isRecommended(String str) {
        return this.recommended.contains(str);
    }

    public boolean isNotTypical(String str) {
        return this.notTypical.contains(str);
    }
}
