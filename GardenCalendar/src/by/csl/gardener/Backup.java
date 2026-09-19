package by.csl.gardener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Резервная копия данных приложения одним JSON-файлом:
 * {"fmt":"zi-garden-backup","ver":<код версии>,"ts":<мс>,"p":{…},"done":{…},"j":{…}}.
 * Типы значений сохраняются: строки, boolean, int, множества строк; long — служебной
 * строкой-приставкой, чтобы после импорта int остался int, а long — long.
 */
final class Backup {
    static final String FORMAT = "zi-garden-backup";
    private static final String LONG_PREFIX = "\u0001L"; // служебная приставка для long-значений

    private Backup() {}

    /** Собирает JSON-копию из трёх срезов хранилища: настройки, отметки «выполнено», журнал. */
    static String encode(Map<String, ?> p, Map<String, ?> done, Map<String, ?> j, long versionCode) {
        try {
            JSONObject root = new JSONObject();
            root.put("fmt", FORMAT);
            root.put("ver", versionCode);
            root.put("ts", System.currentTimeMillis());
            root.put("p", encodeSection(p));
            root.put("done", encodeSection(done));
            root.put("j", encodeSection(j));
            return root.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("backup encode", e);
        }
    }

    private static JSONObject encodeSection(Map<String, ?> map) throws JSONException {
        JSONObject o = new JSONObject();
        for (Map.Entry<String, ?> e : map.entrySet()) {
            Object v = e.getValue();
            if (v == null) {
                continue;
            }
            if (v instanceof Set) {
                JSONArray arr = new JSONArray();
                for (Object s : (Set<?>) v) {
                    arr.put(String.valueOf(s));
                }
                o.put(e.getKey(), arr);
            } else if (v instanceof Long) {
                o.put(e.getKey(), LONG_PREFIX + v.toString()); // long кодируем приставкой — тип не теряется
            } else if (v instanceof Boolean || v instanceof Integer) {
                o.put(e.getKey(), v);
            } else {
                o.put(e.getKey(), String.valueOf(v));
            }
        }
        return o;
    }

    /**
     * Разбирает JSON-копию. Возвращает ровно три карты:
     * [0] = настройки, [1] = отметки «выполнено», [2] = журнал.
     * null — если это не файл резервной копии ZI Garden (или файл испорчен).
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object>[] decode(String json) {
        try {
            JSONObject root = new JSONObject(json);
            if (!FORMAT.equals(root.optString("fmt"))) {
                return null;
            }
            Map<String, Object> p = decodeSection(root.getJSONObject("p"));
            Map<String, Object> done = decodeSection(root.getJSONObject("done"));
            Map<String, Object> j = decodeSection(root.getJSONObject("j"));
            return new Map[] { p, done, j };
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Object> decodeSection(JSONObject o) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        java.util.Iterator<String> keys = o.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            Object v = o.opt(k);
            if (v instanceof JSONArray) {
                JSONArray arr = (JSONArray) v;
                Set<String> set = new HashSet<>();
                for (int i = 0; i < arr.length(); i++) {
                    set.add(arr.optString(i));
                }
                map.put(k, set);
            } else if (v instanceof Boolean || v instanceof Integer) {
                map.put(k, v);
            } else if (v instanceof Long) {
                map.put(k, v); // org.json иногда сам даёт Long — ок
            } else if (v instanceof String && ((String) v).startsWith(LONG_PREFIX)) {
                try {
                    map.put(k, Long.valueOf(((String) v).substring(LONG_PREFIX.length())));
                } catch (Exception badNum) {
                    map.put(k, v);
                }
            } else if (v != null && v != JSONObject.NULL) {
                map.put(k, String.valueOf(v));
            }
        }
        return map;
    }
}
