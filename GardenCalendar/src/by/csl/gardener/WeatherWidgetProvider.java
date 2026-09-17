package by.csl.gardener;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Calendar;

/** Виджет «Погода для сада»: текущая погода, сегодняшний прогноз и совет (полив/обработки/заморозки). */
public class WeatherWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager awm, int[] ids) {
        RemoteViews rv = build(context);
        for (int id : ids) {
            awm.updateAppWidget(id, rv);
        }
    }

    public static void refresh(Context context) {
        try {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            if (awm == null) return;
            int[] ids = awm.getAppWidgetIds(new ComponentName(context, WeatherWidgetProvider.class));
            if (ids == null || ids.length == 0) return;
            RemoteViews rv = build(context);
            for (int id : ids) {
                awm.updateAppWidget(id, rv);
            }
        } catch (Exception ignored) {
            // виджет не должен ронять приложение
        }
    }

    static RemoteViews build(Context context) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        Storage store = new Storage(context);
        Weather w = Weather.fromJson(store.weatherCache());
        Calendar today = Dates.today();
        int y = today.get(Calendar.YEAR);
        int m = today.get(Calendar.MONTH) + 1;
        int d = today.get(Calendar.DAY_OF_MONTH);
        rv.setTextViewText(R.id.widget_w_title, WidgetTexts.weatherTitle(w));
        rv.setTextViewText(R.id.widget_w_date,
                Dates.fmt(y, m, d) + ", " + Dates.weekday(y, m, d));
        rv.setTextViewText(R.id.widget_w_now, WidgetTexts.weatherNow(w));
        String todayLine = WidgetTexts.weatherToday(w, y, m, d);
        rv.setTextViewText(R.id.widget_w_today, todayLine.length() > 0 ? todayLine : "Прогноз появится после обновления в приложении");
        rv.setTextViewText(R.id.widget_w_advice, WidgetTexts.weatherAdvice(w, y, m, d));
        PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_w_root, pi);
        return rv;
    }
}
