package by.csl.gardener;

import android.app.IntentService;
import android.content.Intent;

public class RescheduleService extends IntentService {
    public RescheduleService() {
        super("RescheduleService");
    }

    protected void onHandleIntent(Intent intent) {
        Notifications.scheduleAll(this);
    }
}
