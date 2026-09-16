package by.csl.gardener;

import java.util.Calendar;
import java.util.Date;

public final class Dates {
    public static final String[] MONTHS = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
    public static final String[] MONTHS_NOM = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
    public static final String[] WD = {"воскресенье", "понедельник", "вторник", "среда", "четверг", "пятница", "суббота"};
    public static final String[] WD_SHORT = {"вс", "пн", "вт", "ср", "чт", "пт", "сб"};

    private Dates() {
    }

    public static Calendar at(int i, int i2, int i3) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(i, i2 - 1, i3, 0, 0, 0);
        return calendar;
    }

    public static Calendar today() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar;
    }

    public static Calendar plusDays(Calendar calendar, int i) {
        Calendar calendar2 = (Calendar) calendar.clone();
        calendar2.add(6, i);
        return calendar2;
    }

    public static int diffDays(Calendar calendar, Calendar calendar2) {
        return (int) ((at(calendar2.get(1), calendar2.get(2) + 1, calendar2.get(5)).getTimeInMillis() - at(calendar.get(1), calendar.get(2) + 1, calendar.get(5)).getTimeInMillis()) / 86400000);
    }

    public static String fmt(int i, int i2, int i3) {
        return i3 + " " + MONTHS[i2 - 1] + " " + i;
    }

    public static String fmtShort(int i, int i2, int i3) {
        return i3 + " " + MONTHS[i2 - 1];
    }

    public static String weekday(int i, int i2, int i3) {
        return WD[at(i, i2, i3).get(7) - 1];
    }

    public static String weekdayShort(int i, int i2, int i3) {
        return WD_SHORT[at(i, i2, i3).get(7) - 1];
    }

    public static long atTime(int i, int i2, int i3, int i4, int i5) {
        Calendar at = at(i, i2, i3);
        at.set(11, i4);
        at.set(12, i5);
        return at.getTimeInMillis();
    }

    public static String time(long j) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(j));
        return String.format("%02d:%02d", Integer.valueOf(calendar.get(11)), Integer.valueOf(calendar.get(12)));
    }
}
