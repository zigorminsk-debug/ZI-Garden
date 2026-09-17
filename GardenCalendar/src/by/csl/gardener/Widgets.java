package by.csl.gardener;

import android.content.Context;

/** Обновление всех виджетов рабочего стола разом (после любых изменений в приложении). */
public final class Widgets {
    private Widgets() {}

    public static void refreshAll(Context context) {
        TasksWidgetProvider.refresh(context);
        WeatherWidgetProvider.refresh(context);
        SeasonWidgetProvider.refresh(context);
        TipWidgetProvider.refresh(context);
    }
}
