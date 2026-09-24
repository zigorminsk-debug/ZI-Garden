package by.csl.gardener;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Calendar;

/**
 * Виджет «Минерал дня»: обучающая карточка по 12 элементам питания —
 * признак дефицита и скорая помощь, элемент меняется ежедневно по циклу.
 */
public class DeficitWidgetProvider extends AppWidgetProvider {

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
            int[] ids = awm.getAppWidgetIds(new ComponentName(context, DeficitWidgetProvider.class));
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
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_deficit);
        Calendar today = Dates.today();
        int y = today.get(Calendar.YEAR);
        int m = today.get(Calendar.MONTH) + 1;
        int d = today.get(Calendar.DAY_OF_MONTH);
        int doy = today.get(Calendar.DAY_OF_YEAR);
        rv.setTextViewText(R.id.widget_d_title, "🔬 Минерал дня");
        rv.setTextViewText(R.id.widget_d_date,
                Dates.fmt(y, m, d) + ", " + Dates.weekday(y, m, d));
        rv.setTextViewText(R.id.widget_d_signs, WidgetTexts.deficitHead(doy));
        rv.setTextViewText(R.id.widget_d_fix, WidgetTexts.deficitFix(doy));
        PendingIntent pi = PendingIntent.getActivity(context, 3,
                new Intent(context, DeficiencyActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_d_root, pi);
        return rv;
    }
}
