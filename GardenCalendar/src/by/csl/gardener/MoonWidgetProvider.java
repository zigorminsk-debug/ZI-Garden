package by.csl.gardener;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Calendar;

/** Виджет «Лунный календарь»: фаза Луны и совет дня по лунным традициям. */
public class MoonWidgetProvider extends AppWidgetProvider {

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
            int[] ids = awm.getAppWidgetIds(new ComponentName(context, MoonWidgetProvider.class));
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
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_moon);
        Calendar today = Dates.today();
        int y = today.get(Calendar.YEAR);
        int m = today.get(Calendar.MONTH) + 1;
        int d = today.get(Calendar.DAY_OF_MONTH);
        rv.setTextViewText(R.id.widget_m_title, "🌙 Лунный календарь");
        rv.setTextViewText(R.id.widget_m_date,
                Dates.fmt(y, m, d) + ", " + Dates.weekday(y, m, d));
        rv.setTextViewText(R.id.widget_m_phase, WidgetTexts.moonPhase(y, m, d));
        rv.setTextViewText(R.id.widget_m_advice, WidgetTexts.moonAdvice(y, m, d));
        PendingIntent pi = PendingIntent.getActivity(context, 1,
                new Intent(context, LunarActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_m_root, pi);
        return rv;
    }
}
