package by.csl.gardener;

import java.util.Calendar;

/**
 * Акварельные сцены сезона для главного экрана: по циклу работ на каждое время
 * года. Смена сцен — медленное растворение (наплыв) с постоянной альфой
 * в ритме советской анимации: долгий статичный план и туманный переход.
 */
public final class SeasonArt {

    private SeasonArt() {
    }

    /** Ключевые акварельные сцены сезона (по месяцу 1–12). */
    public static int[] keyFrames(int month1) {
        String season = PhenologyGuide.seasonFor(month1);
        if ("Зима".equals(season)) {
            return new int[]{R.drawable.season_winter_01, R.drawable.season_winter_02,
                    R.drawable.season_winter_03};
        }
        if ("Весна".equals(season)) {
            return new int[]{R.drawable.season_spring_01, R.drawable.season_spring_02,
                    R.drawable.season_spring_03};
        }
        if ("Лето".equals(season)) {
            return new int[]{R.drawable.season_summer_01, R.drawable.season_summer_02,
                    R.drawable.season_summer_03};
        }
        return new int[]{R.drawable.season_autumn_01, R.drawable.season_autumn_02,
                R.drawable.season_autumn_03, R.drawable.season_autumn_04,
                R.drawable.season_autumn_05, R.drawable.season_autumn_06};
    }

    /** Первый кадр сезона — для полноэкранного просмотра с увеличением. */
    public static int frameFor(int month1) {
        return keyFrames(month1)[0];
    }

    /** Текущий месяц 1–12. */
    public static int currentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }
}
