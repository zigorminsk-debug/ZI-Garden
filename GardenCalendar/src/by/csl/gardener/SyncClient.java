package by.csl.gardener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * Клиент сервера семейной синхронизации (sync-server).
 * Чистый HTTP + JSON, никакого Android — тестируется на JVM.
 */
final class SyncClient {
    /** Адрес сервера по умолчанию (демо-развёртывание; можно поменять в Настройках). */
    static final String DEFAULT_SERVER = "https://8635-ix6unzq4sg1bs0xnzky4f.e2b.app";

    static final String PATH_HEALTH = "/api/health";
    static final String PATH_CREATE = "/api/family/create";
    static final String PATH_JOIN = "/api/family/join";
    static final String PATH_PUSH = "/api/state/push";
    static final String PATH_PULL = "/api/state/pull";

    static final class ApiException extends Exception {
        ApiException(String message) { super(message); }
    }

    private SyncClient() {}

    // ── запросы ───────────────────────────────────────────────────────────

    static JSONObject familyCreate(String base, String family, String login, String password)
            throws ApiException {
        try {
            JSONObject body = new JSONObject();
            body.put("family", family);
            body.put("login", login);
            body.put("password", password);
            return api(base + PATH_CREATE, "POST", body);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    }

    static JSONObject familyJoin(String base, String family, String login, String password)
            throws ApiException {
        try {
            JSONObject body = new JSONObject();
            body.put("family", family);
            body.put("login", login);
            body.put("password", password);
            return api(base + PATH_JOIN, "POST", body);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    }

    static JSONObject push(String base, String token, long ts, String json) throws ApiException {
        try {
            JSONObject body = new JSONObject();
            body.put("token", token);
            body.put("ts", ts);
            body.put("json", json);
            return api(base + PATH_PUSH, "POST", body);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    }

    static JSONObject pull(String base, String token) throws ApiException {
        try {
            String url = base + PATH_PULL + "?token="
                    + java.net.URLEncoder.encode(token, "UTF-8");
            return api(url, "GET", null);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    }

    static boolean healthy(String base) {
        try {
            JSONObject r = api(base + PATH_HEALTH, "GET", null);
            return r.optBoolean("ok", false);
        } catch (Exception e) {
            return false;
        }
    }

    // ── транспорт ─────────────────────────────────────────────────────────

    private static JSONObject api(String url, String method, JSONObject body) throws ApiException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod(method);
            conn.setRequestProperty("User-Agent", "ZI-Garden-App");
            conn.setRequestProperty("Accept", "application/json");
            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                byte[] bytes = body.toString().getBytes("UTF-8");
                conn.setFixedLengthStreamingMode(bytes.length);
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.flush();
                out.close();
            }
            int code = conn.getResponseCode();
            String text = readBody(conn, code);
            JSONObject resp;
            try {
                resp = new JSONObject(text);
            } catch (Exception parse) {
                throw new ApiException("сервер ответил не JSON (HTTP " + code + ")");
            }
            if (code < 200 || code >= 300) {
                throw new ApiException(resp.optString("error", "ошибка сервера (HTTP " + code + ")"));
            }
            return resp;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(NetErrors.ru(e));
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String readBody(HttpURLConnection conn, int code) throws java.io.IOException {
        java.io.InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (in == null) {
            return "{}";
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[8192];
        int n;
        while ((n = br.read(buf)) > 0) {
            sb.append(buf, 0, n);
            if (sb.length() > 4194304) break;
        }
        br.close();
        return sb.toString();
    }
}
