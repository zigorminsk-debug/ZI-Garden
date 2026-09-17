package by.csl.gardener;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import java.util.Calendar;
import java.util.List;

/** Виджет «Активно в этом месяце»: болезни и вредители по сезону для культур пользователя. */
public class SeasonWidgetProvider extends AppWidgetProvider {
    private static final int[] LINE_IDS = {R.id.widget_s_line1, R.id.widget_s_line2, R.id.widget_s_line3};

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
            int[] ids = awm.getAppWidgetIds(new ComponentName(context, SeasonWidgetProvider.class));
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
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_season);
        Storage store = new Storage(context);
        Calendar today = Dates.today();
        int month = today.get(Calendar.MONTH);
        rv.setTextViewText(R.id.widget_s_title, "⚠️ Активно в " + WidgetTexts.MONTHS_GEN[month]);
        List<String> lines = WidgetTexts.seasonLines(month, store.plants(), DiseaseDb.all(context), LINE_IDS.length);
        if (lines.isEmpty()) {
            rv.setTextViewText(R.id.widget_s_line1, WidgetTexts.seasonQuiet(month, store.plants()));
            rv.setViewVisibility(R.id.widget_s_line1, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_s_line2, View.GONE);
            rv.setViewVisibility(R.id.widget_s_line3, View.GONE);
        } else {
            for (int i = 0; i < LINE_IDS.length; i++) {
                if (i < lines.size()) {
                    rv.setTextViewText(LINE_IDS[i], lines.get(i));
                    rv.setViewVisibility(LINE_IDS[i], View.VISIBLE);
                } else {
                    rv.setViewVisibility(LINE_IDS[i], View.GONE);
                }
            }
        }
        PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(context, CalendarActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_s_root, pi);
        return rv;
    }
}
