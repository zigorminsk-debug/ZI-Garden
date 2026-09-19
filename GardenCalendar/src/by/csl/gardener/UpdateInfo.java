package by.csl.gardener;

import org.json.JSONArray;
import org.json.JSONObject;

/** Информация о последнем релизе из GitHub Releases API (чистая логика, проверяется LogicTest). */
final class UpdateInfo {
    final String tag;         // "v2.6"
    final String name;        // заголовок релиза
    final String apkUrl;      // browser_download_url первого .apk
    final String notes;       // текст релиза (body)
    final long versionCode;   // та же формула, что в CI: MAJ*10000 + MIN*1000

    private UpdateInfo(String tag, String name, String apkUrl, String notes, long versionCode) {
        this.tag = tag;
        this.name = name;
        this.apkUrl = apkUrl;
        this.notes = notes;
        this.versionCode = versionCode;
    }

    /**
     * Тег «v2.6» → код 26000 (2*10000 + 6*1000) — соответствует .github/workflows/build-apk.yml.
     * Некорректный тег → -1.
     */
    static long versionCodeFromTag(String tag) {
        if (tag == null) return -1;
        String t = tag.trim();
        if (t.startsWith("v") || t.startsWith("V")) t = t.substring(1);
        int dot = t.indexOf('.');
        if (dot <= 0) return -1;
        int dot2 = t.indexOf('.', dot + 1); // v2.6.1 → берём «2» и «6.1»→6
        String majS = t.substring(0, dot);
        String minS = dot2 < 0 ? t.substring(dot + 1) : t.substring(dot + 1, dot2);
        try {
            long maj = Long.parseLong(majS.trim());
            long min = Long.parseLong(minS.trim());
            if (maj < 0 || min < 0) return -1;
            return maj * 10000 + min * 1000;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Парсинг ответа /releases/latest. Возвращает null, если релиз без APK или JSON битый. */
    static UpdateInfo fromReleaseJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            String tag = obj.optString("tag_name", "");
            long code = versionCodeFromTag(tag);
            if (code <= 0) return null;
            String name = obj.optString("name", tag);
            String notes = obj.optString("body", "");
            JSONArray assets = obj.optJSONArray("assets");
            String apk = null;
            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject a = assets.optJSONObject(i);
                    if (a == null) continue;
                    String url = a.optString("browser_download_url", "");
                    if (url.endsWith(".apk")) {
                        apk = url;
                        break;
                    }
                }
            }
            if (apk == null) return null;
            return new UpdateInfo(tag, name, apk, notes, code);
        } catch (Exception e) {
            return null;
        }
    }

    /** Сравнение с текущей версией приложения: есть ли что ставить. */
    static boolean isNewer(long remoteCode, long ownCode) {
        return remoteCode > 0 && remoteCode > ownCode;
    }
}
