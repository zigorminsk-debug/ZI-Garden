package by.csl.gardener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Перекрёстные ссылки: болезни и вредители → календарь обработок по фенофазам.
 * Связываются только карточки, чей график лечения реально расписан в приёмах
 * группы «Обработки по фенофазам»: парша и монилиоз плодовых, цветоед,
 * плодожорка, мучнистая роса и почковый клещ смородины/крыжовника,
 * милдью и оидиум винограда.
 */
public final class SprayLinks {

    private static final Map<String, String> BY_DISEASE = new LinkedHashMap<>();

    static {
        // ── Плодовые: календарь «зелёный конус → розовый бутон → после цветения» ──
        BY_DISEASE.put("apple_scab", "spray_fruit");            // парша яблони
        BY_DISEASE.put("pear_scab", "spray_fruit");             // парша груши
        BY_DISEASE.put("apple_blossom_weevil", "spray_fruit");  // цветоед — розовый бутон
        BY_DISEASE.put("apple_codling_moth", "spray_fruit");    // плодожорка — рост плода
        BY_DISEASE.put("apple_monilia", "spray_fruit");
        BY_DISEASE.put("pear_monilia", "spray_fruit");
        BY_DISEASE.put("quince_monilia", "spray_fruit");
        BY_DISEASE.put("cherry_monilia", "spray_fruit");
        BY_DISEASE.put("sweet_cherry_monilia", "spray_fruit");
        BY_DISEASE.put("plum_monilia", "spray_fruit");
        BY_DISEASE.put("cherry_plum_monilia", "spray_fruit");
        BY_DISEASE.put("apricot_monilia", "spray_fruit");
        BY_DISEASE.put("peach_monilia", "spray_fruit");
        BY_DISEASE.put("rowan_monilia", "spray_fruit");
        BY_DISEASE.put("chokeberry_monilia", "spray_fruit");
        BY_DISEASE.put("irga_monilia", "spray_fruit");
        // ── Ягодные: горячий душ по спящим почкам и окно после сбора ──
        BY_DISEASE.put("gooseberry_mildew", "spray_berries");   // мучнистая роса (смородина, крыжовник)
        BY_DISEASE.put("currant_budmite", "spray_berries");     // почковый клещ
        BY_DISEASE.put("currant_budmite_red", "spray_berries");
        // ── Виноград: «голая лоза → … → 30 дней до сбора» ──
        BY_DISEASE.put("grape_mildew", "spray_grape");          // милдью
        BY_DISEASE.put("grape_oidium", "spray_grape");          // оидиум
    }

    private SprayLinks() {
    }

    /** id приёма-календаря обработок для карточки болезни; null — прямого расписания нет. */
    public static String techniqueFor(String diseaseId) {
        return diseaseId == null ? null : BY_DISEASE.get(diseaseId);
    }

    /** Число связей (для автопроверок). */
    public static int size() {
        return BY_DISEASE.size();
    }

    /** Все пары «болезнь → приём» (для автопроверок). */
    public static Map<String, String> all() {
        return new LinkedHashMap<>(BY_DISEASE);
    }
}
