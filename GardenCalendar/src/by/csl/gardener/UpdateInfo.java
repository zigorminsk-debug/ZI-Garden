package by.csl.gardener;

import org.json.JSONArray;
import org.json.JSONObject;

/** Информация о последнем релизе из GitHub Releases API (чистая логика, проверяется LogicTest). */
final class UpdateInfo {
    /** Репозиторий и имя ассета фиксированы воркфлоу .github/workflows/build-apk.yml. */
    private static final String REPO_PATH = "zigorminsk-debug/ZI-Garden";
    private static final String ASSET_NAME = "ZI-Garden-csl.by-v2.5.apk";

    final String tag;         // "v2.6"
    final String name;        // заголовок релиза
    final String apkUrl;      // первая ссылка на .apk (главная)
    final java.util.List<String> apkUrls; // все ссылки на .apk (главная + зеркала)
    final String notes;       // текст релиза (body)
    final long versionCode;   // та же формула, что в CI: MAJ*10000 + MIN*1000

    private UpdateInfo(String tag, String name, java.util.List<String> apkUrls, String notes, long versionCode) {
        this.tag = tag;
        this.name = name;
        this.apkUrls = apkUrls;
        this.apkUrl = apkUrls.isEmpty() ? null : apkUrls.get(0);
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

    /**
     * Детерминированная ссылка на APK релиза: GitHub отдаёт ассеты по предсказуемому
     * адресу /releases/download/{тег}/{имя}, а имя ассета фиксировано CI.
     * Нужна, потому что современная страница релизов рендерит список ассетов
     * на клиенте — прямых ссылок в статическом HTML больше нет.
     */
    static String assetUrlForTag(String tag) {
        return "https://github.com/" + REPO_PATH + "/releases/download/" + tag + "/" + ASSET_NAME;
    }

    /** Примечания без служебных хвостов коммитов («Co-authored-by: …»). */
    private static String cleanNotes(String notes) {
        if (notes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String line : notes.split("\n", -1)) {
            if (line.trim().startsWith("Co-authored-by:")) continue;
            if (sb.length() > 0) sb.append('\n');
            sb.append(line);
        }
        // схлопываем пустые концы, оставляя текст аккуратным для диалога «Что нового»
        return sb.toString().replaceAll("\n{3,}", "\n\n").trim();
    }

    /** Парсинг ответа /releases/latest. Возвращает null, если релиз без APK или JSON битый. */
    static UpdateInfo fromReleaseJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            String tag = obj.optString("tag_name", "");
            long code = versionCodeFromTag(tag);
            if (code <= 0) return null;
            String name = obj.optString("name", tag);
            String notes = cleanNotes(obj.optString("body", ""));
            JSONArray assets = obj.optJSONArray("assets");
            java.util.List<String> apks = new java.util.ArrayList<>();
            if (assets != null) {
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject a = assets.optJSONObject(i);
                    if (a == null) continue;
                    String url = a.optString("browser_download_url", "");
                    if (url.endsWith(".apk") && !apks.contains(url)) {
                        apks.add(url);
                    }
                }
            }
            if (apks.isEmpty()) return null;
            return new UpdateInfo(tag, name, apks, notes, code);
        } catch (Exception e) {
            return null;
        }
    }

    /** Сравнение с текущей версией приложения: есть ли что ставить. */
    static boolean isNewer(long remoteCode, long ownCode) {
        return remoteCode > 0 && remoteCode > ownCode;
    }

    /**
     * Запасной канал: парсинг HTML-страницы /releases (когда api.github.com недоступен,
     * а github.com — доступен). Берём первый тег vX.Y. Прямую ссылку на .apk ищем в HTML;
     * если её нет (GitHub рендерит ассеты на клиенте) — собираем из фиксированного имени
     * ассета CI: assetUrlForTag.
     */
    static UpdateInfo fromHtmlPage(String html) {
        try {
            if (html == null) return null;
            java.util.regex.Matcher mTag = java.util.regex.Pattern
                    .compile("/releases/tag/v([0-9]+\\.[0-9]+(?:\\.[0-9]+)?)").matcher(html);
            if (!mTag.find()) return null;
            String tag = "v" + mTag.group(1);
            long code = versionCodeFromTag(tag);
            if (code <= 0) return null;
            java.util.regex.Matcher mApk = java.util.regex.Pattern
                    .compile("(/[^\\s\"']*/releases/download/[^\\s\"']+\\.apk)").matcher(html);
            String url;
            if (mApk.find()) {
                url = "https://github.com" + mApk.group(1);
            } else {
                url = assetUrlForTag(tag);
            }
            return new UpdateInfo(tag, tag, java.util.Collections.singletonList(url), "", code);
        } catch (Exception e) {
            return null;
        }
    }
}
