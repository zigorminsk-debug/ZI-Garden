package by.csl.gardener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotifyTapReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra(Notifications.EXTRA_TASK);
        int intExtra = intent.getIntExtra("year", Dates.today().get(1));
        if (stringExtra == null) {
            return;
        }
        new Storage(context).setDone(stringExtra, intExtra, true);
        Notifications.scheduleAll(context);
    }
}
