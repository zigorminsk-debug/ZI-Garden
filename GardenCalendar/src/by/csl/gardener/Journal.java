package by.csl.gardener;

/** Кодирование записей журнала сада (одна строка на выполненную работу). */
final class Journal {
    static final int MAX_ENTRIES = 200;

    /** Индексы полей в раскодированной записи. */
    static final int F_OP = 0;
    static final int F_PLANT = 1;
    static final int F_TITLE = 2;
    static final int F_MATS = 3;
    static final int F_WHEN = 4;

    private Journal() {}

    static String encode(String op, String plant, String title, String mats, long whenMs) {
        return safe(op) + "\u0001" + safe(plant) + "\u0001" + safe(title) + "\u0001"
                + safe(mats) + "\u0001" + whenMs;
    }

    /** Раскодировка; битая строка → null. */
    static String[] decode(String raw) {
        if (raw == null) return null;
        String[] parts = raw.split("\u0001", -1);
        if (parts.length != 5) return null;
        return parts;
    }

    static String safe(String value) {
        return value == null ? "" : value;
    }

    static long when(String[] fields) {
        try {
            return Long.parseLong(fields[F_WHEN]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Из массива сырых строк отбирает max новых (по времени, убывание). */
    static java.util.List<String> newest(java.util.List<String> raw, int max) {
        java.util.List<String[]> parsed = new java.util.ArrayList<>();
        for (String s : raw) {
            String[] f = decode(s);
            if (f != null) parsed.add(f);
        }
        java.util.Collections.sort(parsed, new java.util.Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                long wa = when(a), wb = when(b);
                if (wa == wb) return 0;
                return wa > wb ? -1 : 1;
            }
        });
        java.util.List<String> out = new java.util.ArrayList<>();
        int cap = Math.min(max, Math.min(MAX_ENTRIES, parsed.size()));
        for (int i = 0; i < cap; i++) {
            String[] f = parsed.get(i);
            out.add(encode(f[F_OP], f[F_PLANT], f[F_TITLE], f[F_MATS], when(f)));
        }
        return out;
    }
}
