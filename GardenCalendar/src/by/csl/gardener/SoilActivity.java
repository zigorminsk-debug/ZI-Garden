package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Почва: восстановление плодородия — диагностика, сидераты, органика, мульча, ротация, микрофлора. */
public class SoilActivity extends Activity {
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    public static void show(Context ctx) {
        ctx.startActivity(new android.content.Intent(ctx, SoilActivity.class));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("Почва и плодородие");

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);

        TextView intro = Ui.text(this, SoilGuide.INTRO, 13.0f, cSub, false);
        intro.setPadding(0, Ui.dp(this, 2.0f), 0, Ui.dp(this, 10.0f));
        root.addView(intro);

        for (SoilGuide.Section sec : SoilGuide.all()) {
            LinearLayout card = Ui.card(this);

            int imgRes = DiseaseDb.imageRes(sec.image);
            if (imgRes != 0) {
                ImageView img = new ImageView(this);
                img.setImageResource(imgRes);
                img.setAdjustViewBounds(true);
                final int zoom = imgRes;
                img.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Ui.zoomPhoto(SoilActivity.this, zoom);
                    }
                });
                card.addView(img, new LinearLayout.LayoutParams(-1, -2));
            }

            card.addView(Ui.text(this, sec.title, 15.0f, cMain, true));
            card.addView(Ui.text(this, "🗓 " + sec.season, 12.0f, cSub, false));
            TextView body = Ui.text(this, sec.body, 13.0f, cMain, false);
            body.setPadding(0, Ui.dp(this, 4.0f), 0, Ui.dp(this, 2.0f));
            card.addView(body);
            for (String tip : sec.tips) {
                card.addView(Ui.text(this, "• " + tip, 13.0f, cMain, false));
            }
            root.addView(card);
        }
    }
}
