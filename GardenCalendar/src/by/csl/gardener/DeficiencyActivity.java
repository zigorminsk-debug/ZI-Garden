package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** 🔎 Дефициты минералов и микроэлементов: читаем растение и помогаем. */
public class DeficiencyActivity extends Activity {
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    public static void show(Context ctx) {
        ctx.startActivity(new android.content.Intent(ctx, DeficiencyActivity.class));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("Дефициты элементов");

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);

        LinearLayout intro = Ui.card(this);
        intro.addView(Ui.text(this, "🔎 Как читать растение", 15.0f, cMain, true));
        TextView introText = Ui.text(this, DeficiencyGuide.INTRO, 13.0f, cMain, false);
        introText.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
        intro.addView(introText);
        root.addView(intro);

        for (DeficiencyGuide.Item it : DeficiencyGuide.all()) {
            LinearLayout card = Ui.card(this);
            card.addView(Ui.text(this, it.symbol + " · " + it.name
                    + (it.youngLeaves ? " — смотрим на молодых верхних листьях"
                                      : " — смотрим на старых нижних листьях"),
                    15.0f, getResources().getColor(R.color.green_900), true));
            card.addView(label("Признаки"));
            card.addView(Ui.text(this, it.signs, 13.0f, cMain, false));
            card.addView(label("С чем путают"));
            card.addView(Ui.text(this, it.mimic, 13.0f, cSub, false));
            card.addView(label("Скорая помощь"));
            card.addView(Ui.text(this, it.fix, 13.0f, cMain, false));
            card.addView(label("Профилактика"));
            card.addView(Ui.text(this, it.prevent, 13.0f, cSub, false));
            root.addView(card);
        }
    }

    private TextView label(String s) {
        TextView t = Ui.text(this, s, 11.0f, getResources().getColor(R.color.text_sub), true);
        t.setPadding(0, Ui.dp(this, 6.0f), 0, 0);
        return t;
    }
}
