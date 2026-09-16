package by.csl.gardener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Notification.Builder builder;
        String sb;
        String str;
        Notifications.ensureChannel(context);
        String stringExtra = intent.getStringExtra(Notifications.EXTRA_TASK);
        String stringExtra2 = intent.getStringExtra(Notifications.EXTRA_TITLE);
        String stringExtra3 = intent.getStringExtra(Notifications.EXTRA_PLANT);
        String stringExtra4 = intent.getStringExtra(Notifications.EXTRA_TEXT);
        String stringExtra5 = intent.getStringExtra("window");
        int intExtra = intent.getIntExtra(Notifications.EXTRA_COUNT, 1);
        Intent intent2 = new Intent(context, (Class<?>) MainActivity.class);
        intent2.setFlags(335544320);
        intent2.putExtra(Notifications.EXTRA_TASK, stringExtra);
        PendingIntent activity = PendingIntent.getActivity(context, stringExtra == null ? 0 : stringExtra.hashCode(), intent2, 201326592);
        Intent intent3 = new Intent(context, (Class<?>) NotifyTapReceiver.class);
        intent3.setAction("by.csl.gardener.MARK_DONE");
        intent3.putExtra(Notifications.EXTRA_TASK, stringExtra);
        intent3.putExtra("year", intent.getIntExtra("year", Dates.today().get(1)));
        PendingIntent broadcast = PendingIntent.getBroadcast(context, (stringExtra == null ? 0 : stringExtra.hashCode()) + 7, intent3, 201326592);
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(context, Notifications.CHANNEL);
        } else {
            builder = new Notification.Builder(context);
        }
        if (intExtra > 1) {
            sb = "🌿 Работ в саду: " + intExtra + " (" + stringExtra5 + ")";
        } else {
            StringBuilder sb2 = new StringBuilder("🌿 ");
            if (stringExtra3 == null) {
                stringExtra3 = "Сад";
            }
            sb2.append(stringExtra3);
            sb2.append(": ");
            if (stringExtra2 == null) {
                stringExtra2 = "работа";
            }
            sb2.append(stringExtra2);
            sb = sb2.toString();
        }
        Notification.Builder contentText = builder.setContentTitle(sb).setContentText(stringExtra4 == null ? "" : stringExtra4);
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        StringBuilder sb3 = new StringBuilder();
        if (stringExtra5 == null) {
            str = "";
        } else {
            str = "Дата: " + stringExtra5 + "\n";
        }
        sb3.append(str);
        if (stringExtra4 == null) {
            stringExtra4 = "";
        }
        sb3.append(stringExtra4);
        contentText.setStyle(bigTextStyle.bigText(sb3.toString())).setSmallIcon(R.drawable.ic_leaf).setAutoCancel(true).setContentIntent(activity).setPriority(1).addAction(0, "Отметить выполненной", broadcast);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager != null && Notifications.permissionGranted(context)) {
            notificationManager.notify(stringExtra != null ? stringExtra.hashCode() : 1, builder.build());
        }
        try {
            context.startService(new Intent(context, (Class<?>) RescheduleService.class));
        } catch (Exception unused) {
        }
    }
}
