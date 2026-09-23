package by.csl.gardener;

/** Причина сбоя сети по-русски (чистая логика, без android.* — проверяется LogicTest). */
final class NetErrors {
    private NetErrors() {}

    static String ru(Exception e) {
        if (e instanceof java.net.UnknownHostException) {
            return "нет доступа в интернет (DNS не разрешил GitHub)";
        }
        if (e instanceof java.net.SocketTimeoutException) {
            return "GitHub не отвечает (таймаут) — повторите позже";
        }
        if (e instanceof javax.net.ssl.SSLException) {
            return "не установилось защищённое соединение с GitHub";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("403")) {
            return "GitHub временно ограничил запросы (403) — повторите через час";
        }
        if (msg.contains("404")) {
            return "адрес не найден (404) — неверный адрес либо доступ закрыт";
        }
        if (msg.contains("HTTP")) {
            return "GitHub ответил ошибкой " + msg;
        }
        return "сбой сети (" + e.getClass().getSimpleName() + ")" + (msg.length() > 0 ? ": " + msg : "");
    }
}
