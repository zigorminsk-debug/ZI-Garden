package by.csl.gardener;

import java.util.Calendar;

/**
 * Лунный календарь: фаза Луны на дату и короткий совет по агрономическим лунным традициям
 * (растущая — надземные культуры, убывающая — корнеплоды и обрезка, ново-/полнолуние — без посева).
 * Расчёт — по синодическому месяцу от опорного новолуния 06.01.2000 18:14 UTC (JD 2451550.26).
 * Точности ±несколько часов достаточно: фаза меняется раз в ~3.7 суток.
 */
final class Moon {
    static final double SYNODIC = 29.530588853; // длинa лунного месяца, сутки
    private static final double JD_NEW_MOON = 2451550.26;

    static final String[] EMOJI = {"🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘"};
    static final String[] NAMES = {
            "Новолуние", "Растущая луна", "Первая четверть", "Растущая луна",
            "Полнолуние", "Убывающая луна", "Последняя четверть", "Убывающая луна"};

    private Moon() {}

    /** Возраст луны 0..29.53 суток на локальный полдень даты (полдень — устойчив к часовым поясам). */
    static double age(int year, int month, int day) {
        double days = julianDay(year, month, day, 12.0) - JD_NEW_MOON;
        double age = days % SYNODIC;
        return age < 0 ? age + SYNODIC : age;
    }

    /** Юлианский день по григорианской дате и часу (Meeus). */
    static double julianDay(int y, int m, int d, double hour) {
        if (m <= 2) {
            y -= 1;
            m += 12;
        }
        int a = y / 100;
        int b = 2 - a + a / 4;
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + b - 1524.5 + hour / 24.0;
    }

    /** Фаза 0..7: 0 новолуние, 2 первая четверть, 4 полнолуние, 6 последняя четверть. */
    static int phaseIndex(int year, int month, int day) {
        int idx = (int) Math.floor(age(year, month, day) / SYNODIC * 8.0 + 0.5) % 8;
        return idx < 0 ? idx + 8 : idx;
    }

    static String emoji(int year, int month, int day) {
        return EMOJI[phaseIndex(year, month, day)];
    }

    /** Совет дня по лунным традициям (для idx из phaseIndex). */
    static String advice(int idx) {
        switch (idx) {
            case 0:
                return "без посева и пересадки — день ухода за почвой и планирования";
            case 1:
                return "сейте и подкармливайте надземные культуры: зелень, капусту, томаты";
            case 2:
                return "лучшие дни для посадки, прививки и стратификации семян";
            case 3:
                return "полив и подкормки усваиваются лучше всего";
            case 4:
                return "полнолуние: собирайте надземный урожай, посев нежелателен";
            case 5:
                return "сажайте корнеплоды и луковичные, обрезайте деревья";
            case 6:
                return "обрезка, прополка и борьба с сорняками и вредителями";
            default:
                return "заготовки, консервация, уборка корнеплодов на хранение";
        }
    }

    /** Одна строка для календаря: «🌔 Растущая луна — полив и подкормки усваиваются лучше всего». */
    static String guide(Calendar c) {
        int idx = phaseIndex(c.get(1), c.get(2) + 1, c.get(5));
        return EMOJI[idx] + " " + NAMES[idx] + " — " + advice(idx);
    }
}
