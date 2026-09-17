package by.csl.gardener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Сезонные подсказки календаря: «что сейчас активно» из болезней и вредителей
 * у культур пользователя. Месячные пики собраны по фазам развития для умеренного
 * климата (Беларусь); записи всегда ссылаются на справочник DiseaseDb.
 */
public final class SeasonHints {

    private SeasonHints() {
    }

    /** Идентификаторы записей справочника по месяцам (0 = январь). */
    private static final String[][] MONTH_HINT_IDS = {
        /* январь */   {},
        /* февраль */  {},
        /* март */     {"gh_aphid", "gh_spider_mite", "greenhouse_grayrot", "greenhouse_rootrot",
                        "currant_clearwing", "lawn_snowmold", "onion_neckrot", "potato_rhizoctonia"},
        /* апрель */   {"apple_blossom_weevil", "currant_budmite", "currant_budmite_red", "raspberry_weevil",
                        "gooseberry_sawfly", "apple_scab", "peach_curl", "plum_aphid",
                        "cabbage_flea_beetle", "radish_flea_beetle", "conifer_schutte", "lawn_snowmold",
                        "carrot_fly", "cherry_coccomyces", "greens_blackleg", "gh_aphid", "gh_whitefly"},
        /* май */      {"apple_codling_moth", "apple_scab", "apple_mildew", "pear_psylla", "currant_clearwing",
                        "currant_gall_aphid", "gooseberry_shoot_aphid", "gooseberry_sawfly", "raspberry_stemfly",
                        "strawberry_grayrot", "plum_sawfly", "cherry_fly", "potato_medvedka", "tomato_cutworm",
                        "potato_colorado", "cabbage_white", "cabbage_fly", "onion_fly", "pea_weevil",
                        "beet_mining_fly", "rose_aphid", "sweet_cherry_aphid", "peach_aphid",
                        "apricot_monilia", "sweet_cherry_monilia", "grape_mildew", "cabbage_kila"},
        /* июнь */     {"apple_codling_moth", "potato_colorado", "cabbage_white", "currant_clearwing",
                        "raspberry_gall_midge", "currant_gall_aphid", "strawberry_crown_weevil", "grape_leafroller",
                        "blueberry_geometer", "cucumber_downy", "apple_mildew", "gooseberry_goblet",
                        "cherry_coccomyces", "strawberry_grayrot", "raspberry_grayrot", "onion_thrips",
                        "onion_downy", "rose_blackspot", "rose_chafer", "conifer_sawfly", "greens_slugs",
                        "gh_thrips", "hydrangea_mildew"},
        /* июль */     {"tomato_lateblight", "potato_lateblight", "potato_alternaria", "cucumber_downy",
                        "zucchini_downy", "pumpkin_downy", "onion_downy", "garlic_downy", "tomato_cladosporium",
                        "greenhouse_cladosporium", "cucumber_spider_mite", "gh_spider_mite", "gh_whitefly",
                        "onion_thrips", "walnut_codling", "rowan_apple_moth", "sea_buckthorn_fly",
                        "apple_codling_moth", "raspberry_grayrot", "strawberry_grayrot", "plum_rust",
                        "apple_scab", "pear_rust", "clematis_grayrot", "rose_rust", "lawn_rust", "greens_slugs"},
        /* август */   {"tomato_lateblight", "potato_lateblight", "potato_blackleg", "cucumber_downy",
                        "onion_downy", "garlic_downy", "onion_rust", "garlic_rust", "pea_rust",
                        "zucchini_mildew", "pumpkin_mildew", "cucumber_mildew", "zucchini_grayrot",
                        "beet_cercospora", "carrot_alternaria", "apple_scab", "grape_oidium", "grape_grayrot",
                        "lawn_chafer_grub", "potato_medvedka", "potato_wireworm", "tomato_cutworm",
                        "greens_slugs", "greenhouse_slug", "gh_spider_mite", "gh_whitefly",
                        "hydrangea_grayrot", "rose_rust", "lawn_redthread", "onion_neckrot"},
        /* сентябрь */ {"apple_scab", "pear_scab", "cherry_coccomyces", "plum_rust", "potato_rhizoctonia",
                        "potato_scab", "garlic_nematode", "beet_phomosis", "carrot_phomosis", "onion_neckrot",
                        "zucchini_anthracnose", "pumpkin_anthracnose", "pumpkin_whiterot", "greenhouse_rootrot",
                        "gh_whitefly", "gh_aphid", "greens_grayrot", "greens_blackleg", "conifer_necrosis",
                        "lawn_redthread", "walnut_anthracnose", "chokeberry_septoria", "greenhouse_grayrot"},
        /* октябрь */  {"apple_scab", "pear_scab", "cherry_monilia", "plum_monilia", "peach_monilia",
                        "cherry_coccomyces", "currant_black_anthracnose", "currant_red_anthracnose",
                        "gooseberry_anthracnose", "grape_mildew", "lawn_snowmold", "greenhouse_grayrot",
                        "greenhouse_rootrot", "greens_grayrot", "garlic_fusarium", "onion_fusarium",
                        "beet_phomosis", "currant_clearwing", "potato_medvedka", "tomato_cutworm"},
        /* ноябрь */   {},
        /* декабрь */  {}
    };

    /**
     * Подсказки месяца: записи справочника, которые активны в этом месяце
     * И относятся к культурам пользователя (порядок таблицы сохраняется).
     */
    public static List<Disease> forMonth(int month, Set<String> plants, List<Disease> all) {
        List<Disease> out = new ArrayList<>();
        if (month < 0 || month >= MONTH_HINT_IDS.length || all == null || plants == null) {
            return out;
        }
        Set<String> wanted = new LinkedHashSet<>();
        for (String id : MONTH_HINT_IDS[month]) {
            wanted.add(id);
        }
        for (Disease dz : all) {
            if (wanted.contains(dz.id) && plants.contains(dz.plantId)) {
                out.add(dz);
            }
        }
        return out;
    }

    /** Сезонный текст записи для месяца: март–май весна, июнь–август лето, сентябрь–ноябрь осень, зима → весна. */
    public static String seasonText(Disease dz, int month) {
        if (month >= 2 && month <= 4) {
            return dz.spring;
        }
        if (month >= 5 && month <= 7) {
            return dz.summer;
        }
        if (month >= 8 && month <= 10) {
            return dz.autumn;
        }
        return dz.spring;
    }
}
