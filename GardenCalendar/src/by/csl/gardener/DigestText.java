package by.csl.gardener;

import java.util.Calendar;
import java.util.List;

/**
 * Текст еженедельного дайджеста «что предстоит в саду» (уведомление по воскресеньям).
 * Без android-классов — тестируется на JVM.
 */
final class DigestText {
    private DigestText() {}

    /** [0] = заголовок, [1] = тело дайджеста предстоящих 7 дней. */
    static String[] weekly(Storage store) {
        Planner planner = new Planner(store, Weather.fromJson(store.weatherCache()));
        List<Task> week = planner.tasks(7);
        int total = 0;
        int listed = 0;
        StringBuilder body = new StringBuilder();
        Calendar curDay = null;
        for (Task t : week) {
            if (t.done) {
                continue;
            }
            total++;
            if (listed >= 5) {
                continue;
            }
            Calendar day = Dates.at(t.year, t.month, t.day);
            boolean newDay = curDay == null
                    || curDay.get(Calendar.YEAR) != day.get(Calendar.YEAR)
                    || curDay.get(Calendar.DAY_OF_YEAR) != day.get(Calendar.DAY_OF_YEAR);
            if (newDay) {
                if (body.length() > 0) {
                    body.append('\n');
                }
                body.append("▪ ").append(Dates.weekdayShort(t.year, t.month, t.day))
                        .append(": ");
                curDay = (Calendar) day.clone();
            } else {
                body.append("; ");
            }
            body.append(Operation.icon(t.op)).append(' ')
                    .append(t.plantName).append(" — ").append(t.title);
            listed++;
        }
        if (total > listed) {
            body.append("\n…и ещё ").append(total - listed).append(' ').append(plural(total - listed)).append('.');
        }
        String title;
        if (total == 0) {
            title = "🌿 Спокойная неделя";
            body.append("Активных работ на неделю нет — загляните в справочник болезней, "
                    + "проверьте запасы препаратов или спланируйте посадки.");
        } else {
            title = "🌿 Неделя в саду: " + total + " " + plural(total);
        }
        return new String[]{title, body.toString()};
    }

    /** «1 работа», «3 работы», «7 работ». */
    static String plural(int n) {
        int mod10 = n % 10;
        int mod100 = n % 100;
        if (mod10 == 1 && mod100 != 11) {
            return "работа";
        }
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
            return "работы";
        }
        return "работ";
    }
}
