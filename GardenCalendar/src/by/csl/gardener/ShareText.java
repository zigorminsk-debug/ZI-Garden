package by.csl.gardener;

/** Текст совета по болезни/вредителю для отправки через мессенджер (Viber, Telegram, WhatsApp и др.). */
final class ShareText {
    private ShareText() {}

    /** Полный совет по записи справочника: распознавание, лечение по шагам, сезонная профилактика, препараты. */
    static String diseaseAdvice(Plant plant, Disease dz) {
        StringBuilder sb = new StringBuilder(900);
        sb.append("🩺 Совет садоводу");
        if (plant != null) {
            sb.append(": ").append(plant.name).append(" — ").append(dz.name);
        } else {
            sb.append(": ").append(dz.name);
        }
        sb.append('\n');
        if (dz.kind != null && dz.kind.length() > 0) {
            sb.append(dz.kind).append('\n');
        }
        sb.append('\n');

        if (dz.symptoms != null && dz.symptoms.length() > 0) {
            sb.append("🔎 Как распознать:\n").append(dz.symptoms).append("\n\n");
        }

        if (dz.cure != null && dz.cure.length > 0) {
            sb.append("🛠️ Лечение по шагам:\n");
            for (int i = 0; i < dz.cure.length; i++) {
                if (dz.cure[i] == null || dz.cure[i].length() == 0) continue;
                sb.append(i + 1).append(". ").append(dz.cure[i]).append('\n');
            }
            sb.append('\n');
        }

        boolean hasSeason = (dz.spring != null && dz.spring.length() > 0)
                || (dz.summer != null && dz.summer.length() > 0)
                || (dz.autumn != null && dz.autumn.length() > 0);
        if (hasSeason) {
            sb.append("📅 Профилактика по сезонам:\n");
            if (dz.spring != null && dz.spring.length() > 0) sb.append("🌱 Весна: ").append(dz.spring).append('\n');
            if (dz.summer != null && dz.summer.length() > 0) sb.append("☀️ Лето: ").append(dz.summer).append('\n');
            if (dz.autumn != null && dz.autumn.length() > 0) sb.append("🍂 Осень: ").append(dz.autumn).append('\n');
            sb.append('\n');
        }

        if (dz.mats != null && dz.mats.length > 0) {
            StringBuilder names = new StringBuilder();
            for (String id : dz.mats) {
                Material m = Material.byId(id);
                if (m == null) continue;
                if (names.length() > 0) names.append(", ");
                names.append(m.name);
            }
            if (names.length() > 0) {
                sb.append("💊 Препараты и материалы: ").append(names).append('\n');
            }
        }

        sb.append("\n— из справочника приложения ZI Garden");
        return sb.toString();
    }

    /** План работ на неделю текстом — для отправки семье в мессенджер (без выполненных). */
    static String weekPlan(java.util.List<Task> tasks, String fromLabel) {
        StringBuilder sb = new StringBuilder("🗓️ План работ на неделю");
        if (fromLabel != null && fromLabel.length() > 0) {
            sb.append(" (от ").append(fromLabel).append(')');
        }
        int shown = 0;
        int pending = 0;
        if (tasks != null) {
            for (Task t : tasks) {
                if (t == null || t.done) continue;
                pending++;
                if (sb.length() < 1400) {
                    sb.append('\n').append("• ").append(Dates.fmt(t.year, t.month, t.day))
                            .append(" (").append(Dates.weekday(t.year, t.month, t.day)).append(") ")
                            .append(Operation.icon(t.op)).append(' ')
                            .append(t.plantName).append(" — ").append(t.title);
                    shown++;
                }
            }
        }
        if (shown == 0) {
            sb.append("\nРабот на ближайшую неделю не запланировано 🌿");
        } else if (pending > shown) {
            sb.append("\n…ещё ").append(pending - shown).append(" — смотрите в приложении");
        }
        sb.append("\n\n— из приложения ZI Garden");
        return sb.toString();
    }
}
