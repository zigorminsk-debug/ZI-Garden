package by.csl.gardener;

import java.util.Calendar;

/**
 * Акварельные анимации сезона для главного экрана: по одному циклу работ
 * на каждое время года (осень — листопад и компост, зима — снег и укрытия,
 * весна — обрезка и посев, лето — полив и урожай).
 */
public final class SeasonArt {

    private SeasonArt() {
    }

    /** Анимация (animation-list) для текущего месяца. */
    public static int animationFor(int month1) {
        String season = PhenologyGuide.seasonFor(month1);
        if ("Зима".equals(season)) {
            return R.drawable.anim_season_winter;
        }
        if ("Весна".equals(season)) {
            return R.drawable.anim_season_spring;
        }
        if ("Лето".equals(season)) {
            return R.drawable.anim_season_summer;
        }
        return R.drawable.anim_season_autumn;
    }

    /** Первый кадр сезона — для полноэкранного просмотра с увеличением. */
    public static int frameFor(int month1) {
        String season = PhenologyGuide.seasonFor(month1);
        if ("Зима".equals(season)) {
            return R.drawable.season_winter_1;
        }
        if ("Весна".equals(season)) {
            return R.drawable.season_spring_1;
        }
        if ("Лето".equals(season)) {
            return R.drawable.season_summer_1;
        }
        return R.drawable.season_autumn_1;
    }

    /** Текущий месяц 1–12. */
    public static int currentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }
}
