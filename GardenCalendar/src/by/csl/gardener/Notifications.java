package by.csl.gardener;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public final class Notifications {
    public static final String CHANNEL = "garden_works";
    public static final String EXTRA_COUNT = "task_count";
    public static final String EXTRA_PLANT = "task_plant";
    public static final String EXTRA_TASK = "task_id";
    public static final String EXTRA_TEXT = "task_text";
    public static final String EXTRA_TITLE = "task_title";
    private static final int MAX_ALARMS = 20;
    private static final int SCHEDULE_DAYS = 14;

    private Notifications() {
    }

    public static void ensureChannel(Context context) {
        NotificationManager notificationManager;
        if (Build.VERSION.SDK_INT < 26 || (notificationManager = (NotificationManager) context.getSystemService("notification")) == null) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL, "Напоминания о работах в саду", 4);
        notificationChannel.setDescription("Всплывающие уведомления о необходимых работах по уходу за садом");
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public static boolean permissionGranted(Context context) {
        return Build.VERSION.SDK_INT < 33 || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0;
    }

    public static int scheduleAll(Context context) {
        cancelAll(context);
        Storage storage = new Storage(context);
        if (!storage.notifyEnabled()) {
            return 0;
        }
        ensureChannel(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            return 0;
        }
        Planner planner = new Planner(storage, Weather.fromJson(storage.weatherCache()));
        Calendar today = Dates.today();
        Calendar horizon = Dates.plusDays(today, SCHEDULE_DAYS);
        int leadDays = storage.leadDays();
        long now = System.currentTimeMillis();
        int count = 0;
        for (int day = 0; day <= SCHEDULE_DAYS; day++) {
            Calendar d = Dates.plusDays(today, day);
            List<Task> due = new ArrayList<>();
            for (Task t : planner.tasks(21)) {
                if (t.done) continue;
                Calendar at = Dates.at(t.year, t.month, t.day);
                if (at.get(Calendar.YEAR) == d.get(Calendar.YEAR)
                        && at.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)) {
                    due.add(t);
                }
            }
            if (due.isEmpty()) continue;
            // напоминание — за leadDays до работы, но не раньше сегодняшнего дня
            Calendar remind = Dates.plusDays(d, -leadDays);
            if (remind.before(today)) {
                remind = (Calendar) today.clone();
            }
            long atMs = Dates.atTime(remind.get(Calendar.YEAR), remind.get(Calendar.MONTH) + 1,
                    remind.get(Calendar.DAY_OF_MONTH), storage.notifyHour(), storage.notifyMinute());
            if (atMs <= now) {
                atMs = now + 60000L; // уже прошло — напомнить через минуту
            }
            if (atMs > horizon.getTimeInMillis() + 86400000L) continue;
            setAlarm(context, alarmManager, d, due, atMs);
            count++;
        }
        return count;
    }

    private static void setAlarm(Context context, AlarmManager alarmManager, Calendar calendar, List<Task> list, long j) {
        Task task = list.get(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size() && i < 4; i++) {
            Task task2 = list.get(i);
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(Operation.icon(task2.op));
            sb.append(' ');
            sb.append(task2.plantName);
            sb.append(": ");
            sb.append(task2.title);
        }
        if (list.size() > 4) {
            sb.append("\n…и ещё ");
            sb.append(list.size() - 4);
            sb.append(" раб.");
        }
        Intent intent = new Intent(context, (Class<?>) AlarmReceiver.class);
        intent.putExtra(EXTRA_TASK, task.id);
        intent.putExtra(EXTRA_TITLE, task.title);
        intent.putExtra(EXTRA_PLANT, task.plantName);
        intent.putExtra(EXTRA_TEXT, sb.toString());
        intent.putExtra(EXTRA_COUNT, list.size());
        intent.putExtra("year", calendar.get(1));
        intent.putExtra("window", Dates.fmtShort(calendar.get(1), calendar.get(2) + 1, calendar.get(5)));
        PendingIntent broadcast = PendingIntent.getBroadcast(context, dayRequestCode(calendar), intent, 201326592);
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                alarmManager.setWindow(0, j, 600000L, broadcast);
            } else {
                alarmManager.setExact(0, j, broadcast);
            }
        } catch (SecurityException unused) {
            alarmManager.set(0, j, broadcast);
        }
    }

    private static int dayRequestCode(Calendar calendar) {
        return ("day" + calendar.get(1) + calendar.get(6)).hashCode();
    }

    public static void cancelAll(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            return;
        }
        Storage storage = new Storage(context);
        Planner planner = new Planner(storage, Weather.fromJson(storage.weatherCache()));
        Calendar calendar = Dates.today();
        for (int i = 0; i <= 17; i++) {
            alarmManager.cancel(PendingIntent.getBroadcast(context, dayRequestCode(Dates.plusDays(calendar, i)), new Intent(context, (Class<?>) AlarmReceiver.class), 201326592));
        }
        Iterator<Task> it = planner.tasks(21).iterator();
        while (it.hasNext()) {
            alarmManager.cancel(PendingIntent.getBroadcast(context, taskRequestCode(it.next()), new Intent(context, (Class<?>) AlarmReceiver.class), 201326592));
        }
    }

    private static int taskRequestCode(Task task) {
        return (task.id + task.year + task.window).hashCode();
    }
}
