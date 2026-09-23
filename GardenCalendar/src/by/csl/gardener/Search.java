package by.csl.gardener;

/** Поиск по справочникам: все слова запроса должны где-то встретиться. */
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
        return allWordsIn(hay.toString(), q);
    }

    /** Агроприём: ищем по названию, группе, срокам, инструменту, технике и ошибкам. */
    static boolean matchesTechnique(TechniqueGuide.Item it, String query) {
        if (it == null) return false;
        if (query == null) return true;
        String q = query.trim().toLowerCase();
        if (q.length() == 0) return true;
        StringBuilder hay = new StringBuilder();
        append(hay, it.title);
        append(hay, it.group);
        append(hay, it.whenText);
        append(hay, it.tools);
        append(hay, it.how);
        append(hay, it.mistakes);
        return allWordsIn(hay.toString(), q);
    }

    /** Фенологический ориентир: ищем по сезону, названию, сроку, сигналу и работам. */
    static boolean matchesSign(PhenologyGuide.Sign s, String query) {
        if (s == null) return false;
        if (query == null) return true;
        String q = query.trim().toLowerCase();
        if (q.length() == 0) return true;
        StringBuilder hay = new StringBuilder();
        append(hay, s.season);
        append(hay, s.title);
        append(hay, s.period);
        append(hay, s.signal);
        append(hay, s.works);
        return allWordsIn(hay.toString(), q);
    }

    /** Каждое слово запроса — подстрока текста (без учёта регистра). */
    private static boolean allWordsIn(String text, String q) {
        String t = text.toLowerCase();
        for (String word : q.split("\\s+")) {
            if (word.length() > 0 && !t.contains(word)) {
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
