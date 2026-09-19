package by.csl.gardener;

import java.util.List;

/** Сроки ожидания препаратов до сбора урожая (дней) и статус по выполненной обработке. */
final class WaitDays {
    private WaitDays() {}

    /** Название-фрагмент → срок ожидания (дней). Соответствует листкам инструкций препаратов. */
    private static final String[][] TABLE = {
            {"Актара", "21"},
            {"Алатар", "20"},
            {"Кинмикс", "5"},
            {"Биотлин", "5"},
            {"Фитоверм", "2"},
            {"Лепидоцид", "5"},
            {"Битоксибациллин", "5"},
            {"Бордос", "14"},
            {"Медный купорос", "14"},
            {"Хлорокись", "14"},
            {"Сургут", "20"},
            {"Топаз", "14"},
            {"Тиовит", "3"},
            {"Сера", "3"},
            {"Метальдегид", "21"},
            {"Марганцовка", "0"},
            {"Зеленка", "0"},
            {"Превикур", "20"},
            {"Ридомил", "7"},
    };

    /** Срок ожидания по названию препарата (0 — неизвестен/нет ожидания). */
    static int forName(String name) {
        if (name == null) return 0;
        int best = 0;
        for (String[] row : TABLE) {
            if (name.contains(row[0])) {
                int days = Integer.parseInt(row[1]);
                if (days > best) best = days;
            }
        }
        return best;
    }

    /** CSV названий материалов задачи (для записи в журнал). */
    static String matsCsv(Task task) {
        if (task == null || task.items == null || task.items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Task.Item item : task.items) {
            if (item == null || item.alternative || item.name == null) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(item.name);
        }
        return sb.toString();
    }

    /** Максимальный срок ожидания среди материалов CSV-строки. */
    static int forMats(String matsCsv) {
        if (matsCsv == null || matsCsv.length() == 0) return 0;
        int best = 0;
        for (String name : matsCsv.split(", ")) {
            int days = forName(name);
            if (days > best) best = days;
        }
        return best;
    }

    static long dayMs() {
        return 86400000L;
    }

    /**
     * Статус срока ожидания для журнальной записи обработки.
     * «⏳ до сбора ещё N дн.» / «✅ срок ожидания вышел» / "" (не обработка или дней нет).
     */
    static String statusLine(String op, String matsCsv, long doneMs, long nowMs) {
        if (!Operation.SPRAY.equals(op)) return "";
        int wait = forMats(matsCsv);
        if (wait <= 0) return "";
        long endMs = doneMs + wait * dayMs();
        long leftMs = endMs - nowMs;
        if (leftMs <= 0) {
            return "✅ Срок ожидания вышел — урожай можно собирать";
        }
        long days = (leftMs + dayMs() - 1) / dayMs();
        String mats = matsCsv != null && matsCsv.length() > 0 ? " (" + matsCsv + ")" : "";
        return "⏳ Срок ожидания" + mats + ": до сбора ещё " + days + " дн.";
    }
}
