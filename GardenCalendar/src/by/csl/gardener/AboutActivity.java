package by.csl.gardener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_about);
        // баннер — тоже фото: тап открывает полноэкранный просмотр с жестами
        findViewById(R.id.banner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ui.zoomPhoto(AboutActivity.this, R.drawable.banner_garden);
            }
        });
        ((TextView) findViewById(R.id.version)).setText(
                "Версия " + AppUpdate.ownVersionName(this) + " · обновления — с GitHub");
        findViewById(R.id.check_update).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                AppUpdate.checkNow(AboutActivity.this);
            }
        });
        findViewById(R.id.open_releases).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                AppUpdate.openReleasePage(AboutActivity.this);
            }
        });
        findViewById(R.id.show_onboarding).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                OnboardingActivity.show(AboutActivity.this);
            }
        });
        ((TextView) findViewById(R.id.phone)).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                AboutActivity.this.m0lambda$onCreate$0$bycslgardenerAboutActivity(view);
            }
        });
        ((TextView) findViewById(R.id.site)).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                AboutActivity.this.m1lambda$onCreate$1$bycslgardenerAboutActivity(view);
            }
        });
    }

    void m0lambda$onCreate$0$bycslgardenerAboutActivity(View view) {
        try {
            startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:+375293371412")));
        } catch (Exception unused) {
            Ui.toast(this, "+375 29 337-14-12");
        }
    }

    void m1lambda$onCreate$1$bycslgardenerAboutActivity(View view) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://csl.by")));
        } catch (Exception unused) {
            Ui.toast(this, "csl.by");
        }
    }
}
