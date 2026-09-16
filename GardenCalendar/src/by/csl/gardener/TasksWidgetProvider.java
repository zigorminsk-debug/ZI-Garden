package by.csl.gardener;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Виджет «Задачи на сегодня» для рабочего стола: ближайшие 3 невыполненные работы
 * на 7 дней вперёд. Тап по виджету открывает приложение.
 */
public class TasksWidgetProvider extends AppWidgetProvider {

    private static final int[] LINE_IDS = {
            R.id.widget_line1, R.id.widget_line2, R.id.widget_line3};

    @Override
    public void onUpdate(Context context, AppWidgetManager awm, int[] ids) {
        RemoteViews rv = build(context);
        for (int id : ids) {
            awm.updateAppWidget(id, rv);
        }
    }

    /** Обновить все экземпляры виджета (вызывается после изменений в приложении). */
    public static void refresh(Context context) {
        try {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            if (awm == null) return;
            int[] ids = awm.getAppWidgetIds(
                    new ComponentName(context, TasksWidgetProvider.class));
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
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
        Storage store = new Storage(context);
        Calendar today = Dates.today();
        int y = today.get(Calendar.YEAR);
        int m = today.get(Calendar.MONTH) + 1;
        int d = today.get(Calendar.DAY_OF_MONTH);
        rv.setTextViewText(R.id.widget_date,
                Dates.fmt(y, m, d) + ", " + Dates.weekday(y, m, d));

        List<Task> shown = new ArrayList<>();
        for (Task t : new Planner(store, Weather.fromJson(store.weatherCache())).tasks(7)) {
            if (t.done) continue;
            shown.add(t);
            if (shown.size() >= LINE_IDS.length) break;
        }

        for (int i = 0; i < LINE_IDS.length; i++) {
            if (i < shown.size()) {
                Task t = shown.get(i);
                String when = Dates.at(t.year, t.month, t.day).get(Calendar.DAY_OF_YEAR)
                        == today.get(Calendar.DAY_OF_YEAR) ? "сегодня" : Dates.fmt(t.year, t.month, t.day);
                Plant pl = Plant.byId(t.plantId);
                rv.setTextViewText(LINE_IDS[i],
                        (pl != null && pl.iconRes != 0 ? "" : (pl != null ? pl.icon : "•") + " ") + t.plantName + " — " + t.title + " (" + when + ")");
                rv.setTextViewCompoundDrawables(LINE_IDS[i],
                        pl != null ? pl.iconRes : 0, 0, 0, 0);
                rv.setTextColor(LINE_IDS[i], 0xFF20271F);
                rv.setViewVisibility(LINE_IDS[i], View.VISIBLE);
            } else {
                rv.setViewVisibility(LINE_IDS[i], View.GONE);
            }
        }
        if (shown.isEmpty()) {
            rv.setTextViewText(R.id.widget_line1,
                    "🌿 На ближайшие 7 дней работ нет");
            rv.setTextColor(R.id.widget_line1, 0xFF5C6660);
            rv.setViewVisibility(R.id.widget_line1, View.VISIBLE);
        }

        PendingIntent pi = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_root, pi);
        return rv;
    }
}
