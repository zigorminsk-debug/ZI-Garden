package by.csl.gardener;

/** Поиск по справочнику болезней/вредителей: все слова запроса должны где-то встретиться. */
final class Search {
    private Search() {}

    static boolean matches(Disease dz, String query) {
        if (dz == null) return false;
        if (query == null) return true;
        String q = query.trim().toLowerCase();
        if (q.length() == 0) return true;
        StringBuilder hay = new StringBuilder();
        append(hay, dz.name);
        append(hay, dz.kind);
        append(hay, dz.symptoms);
        append(hay, dz.spring);
        append(hay, dz.summer);
        append(hay, dz.autumn);
        if (dz.cure != null) {
            for (String step : dz.cure) {
                append(hay, step);
            }
        }
        String text = hay.toString().toLowerCase();
        for (String word : q.split("\\s+")) {
            if (word.length() > 0 && !text.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private static void append(StringBuilder sb, String field) {
        if (field != null) {
            sb.append(' ').append(field);
        }
    }
}
