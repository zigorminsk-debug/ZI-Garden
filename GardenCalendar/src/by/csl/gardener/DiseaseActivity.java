package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** Справочник болезней конкретной культуры: фото, симптомы, лечение, профилактика по сезонам. */
public class DiseaseActivity extends Activity {
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }


    private String plantId;
    private LinearLayout root;

    public static void show(Context ctx, String plantId) {
        Intent intent = new Intent(ctx, DiseaseActivity.class);
        intent.putExtra("plant", plantId);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.plantId = getIntent().getStringExtra("plant");
        Plant plant = Plant.byId(this.plantId);
        setTitle("Болезни: " + (plant != null ? plant.name : this.plantId));

        ScrollView scroll = new ScrollView(this);
        this.root = new LinearLayout(this);
        this.root.setOrientation(LinearLayout.VERTICAL);
        this.root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        scroll.addView(this.root);
        setContentView(scroll);
        render();
    }

    private void render() {
        this.root.removeAllViews();
        Plant plant = Plant.byId(this.plantId);
        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);

        Button refresh = new Button(this);
        refresh.setText("🔄 Обновить справочник из интернета");
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RemoteDiseases.refreshAsync(DiseaseActivity.this);
                Ui.toast(DiseaseActivity.this, "Обновление запущено в фоне — зайдите через минуту");
            }
        });
        this.root.addView(refresh);

        List<Disease> list = DiseaseDb.forPlant(this, this.plantId);
        int updated = RemoteDiseases.cachedCount(this);
        TextView header = Ui.text(this, "Найдено болезней: " + list.size()
                + (updated > 0 ? " (есть обновление с сервера: " + updated + " записей)" : " (встроенный справочник)")
                + ". Сравните симптомы с растением — по фото и описанию определите болезнь, затем действуйте по шагам.",
                13.0f, cSub, false);
        header.setPadding(0, Ui.dp(this, 4.0f), 0, Ui.dp(this, 8.0f));
        this.root.addView(header);

        for (Disease dz : list) {
            LinearLayout card = Ui.card(this);

            if (dz.imageRes() != 0) {
                ImageView img = new ImageView(this);
                img.setImageResource(dz.imageRes());
                img.setAdjustViewBounds(true);
                card.addView(img, new LinearLayout.LayoutParams(-1, -2));
                TextView cap = Ui.text(this, "На фото: типичные симптомы — «" + dz.name + "». Сравните со своим растением.", 12.0f, cSub, false);
                cap.setPadding(0, Ui.dp(this, 2.0f), 0, Ui.dp(this, 6.0f));
                card.addView(cap);
            }

            card.addView(Ui.text(this, dz.name, 16.0f, cMain, true));
            if (dz.kind.length() > 0) {
                card.addView(Ui.text(this, dz.kind, 12.0f, cSub, false));
            }

            TextView label = Ui.text(this, "🔎 Как распознать", 14.0f, cMain, true);
            label.setPadding(0, Ui.dp(this, 8.0f), 0, 0);
            card.addView(label);
            TextView sym = Ui.text(this, dz.symptoms, 13.0f, cMain, false);
            sym.setLineSpacing(Ui.dp(this, 2.0f), 1.0f);
            card.addView(sym);

            if (dz.cure != null && dz.cure.length > 0) {
                TextView cureLabel = Ui.text(this, "🛠️ Лечение — по шагам", 14.0f, cMain, true);
                cureLabel.setPadding(0, Ui.dp(this, 8.0f), 0, Ui.dp(this, 2.0f));
                card.addView(cureLabel);
                for (int i = 0; i < dz.cure.length; i++) {
                    card.addView(Ui.text(this, (i + 1) + ". " + dz.cure[i], 13.0f, cMain, false));
                }
            }

            TextView prevLabel = Ui.text(this, "📅 Профилактика по сезонам", 14.0f, cMain, true);
            prevLabel.setPadding(0, Ui.dp(this, 8.0f), 0, Ui.dp(this, 2.0f));
            card.addView(prevLabel);
            if (dz.spring != null && dz.spring.length() > 0) card.addView(Ui.text(this, "🌱 Весна: " + dz.spring, 13.0f, cMain, false));
            if (dz.summer != null && dz.summer.length() > 0) card.addView(Ui.text(this, "☀️ Лето: " + dz.summer, 13.0f, cMain, false));
            if (dz.autumn != null && dz.autumn.length() > 0) card.addView(Ui.text(this, "🍂 Осень: " + dz.autumn, 13.0f, cMain, false));

            if (dz.mats != null && dz.mats.length > 0) {
                StringBuilder names = new StringBuilder();
                for (String id : dz.mats) {
                    Material m = Material.byId(id);
                    if (m == null) continue;
                    if (names.length() > 0) names.append(", ");
                    names.append(m.name);
                }
                if (names.length() > 0) {
                    TextView matsLabel = Ui.text(this, "💊 Препараты и материалы", 14.0f, cMain, true);
                    matsLabel.setPadding(0, Ui.dp(this, 8.0f), 0, Ui.dp(this, 2.0f));
                    card.addView(matsLabel);
                    card.addView(Ui.text(this, names.toString(), 13.0f, cSub, false));
                }
            }

            final Disease shareDz = dz;
            final Plant sharePlant = plant;
            Button share = new Button(this);
            share.setText("📤 Поделиться советом");
            share.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    DiseaseActivity.this.shareAdvice(sharePlant, shareDz);
                }
            });
            LinearLayout.LayoutParams shareLp = new LinearLayout.LayoutParams(-1, -2);
            shareLp.setMargins(0, Ui.dp(this, 8.0f), 0, 0);
            card.addView(share, shareLp);

            this.root.addView(card);
        }

        TextView sign = Ui.text(this, "© Zakharevich Igor · +375 29 337-14-12 · csl.by", 12.0f, cSub, false);
        sign.setPadding(0, Ui.dp(this, 12.0f), 0, 0);
        this.root.addView(sign);
    }

    /** Отправить совет по болезни/вредителю в любой мессенджер через стандартный системный выбор. */
    private void shareAdvice(Plant plant, Disease dz) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT,
                "Совет садоводу: " + (plant != null ? plant.name + " — " : "") + dz.name);
        send.putExtra(Intent.EXTRA_TEXT, ShareText.diseaseAdvice(plant, dz));
        startActivity(Intent.createChooser(send, "Поделиться советом через…"));
    }
}
