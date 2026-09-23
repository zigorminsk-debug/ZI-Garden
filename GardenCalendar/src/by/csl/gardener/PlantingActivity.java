package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 🌱 Посадка растений: сроки, ямы, схемы, первый уход и соседство культур.
 */
public class PlantingActivity extends Activity {

    /** Порядок разделов справочника (пустые скрываются). */
    private static final String[] GROUPS = {
            "Плодовые деревья", "Косточковые", "Ягодные кустарники", "Ягодники",
            "Овощные культуры", "Виноград и лианы", "Декоративные", "Теплица и постройки"
    };

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    public static void show(Context ctx) {
        ctx.startActivity(new android.content.Intent(ctx, PlantingActivity.class));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("Посадка растений");

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);
        int cGreen = getResources().getColor(R.color.green_900);

        LinearLayout intro = Ui.card(this);
        intro.addView(Ui.text(this, "🌱 Как сажать правильно", 15.0f, cMain, true));
        TextView introText = Ui.text(this, PlantingGuide.INTRO, 13.0f, cMain, false);
        introText.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
        intro.addView(introText);
        TextView count = Ui.text(this, "В справочнике: " + PlantingGuide.all().size() + " культур — и список растёт",
                12.0f, cSub, true);
        count.setPadding(0, Ui.dp(this, 6.0f), 0, 0);
        intro.addView(count);
        root.addView(intro);

        for (String group : GROUPS) {
            List<PlantingGuide.Entry> entries = new ArrayList<>();
            for (Plant p : Plant.all()) {
                if (!group.equals(p.group)) {
                    continue;
                }
                PlantingGuide.Entry e = PlantingGuide.byPlant(p.id);
                if (e != null) {
                    entries.add(e);
                }
            }
            if (entries.isEmpty()) {
                continue;
            }

            TextView header = Ui.text(this, group, 14.0f, cSub, true);
            header.setPadding(Ui.dp(this, 4.0f), Ui.dp(this, 10.0f), 0, Ui.dp(this, 4.0f));
            root.addView(header);

            for (final PlantingGuide.Entry it : entries) {
                Plant plant = null;
                for (Plant p : Plant.all()) {
                    if (p.id.equals(it.plantId)) {
                        plant = p;
                        break;
                    }
                }
                LinearLayout card = Ui.card(this);

                card.addView(Ui.text(this,
                        (plant != null ? plant.icon : "🌱") + " " + (plant != null ? plant.name : it.plantId),
                        15.0f, cGreen, true));

                addDiagram(card, it.diagram);
                card.addView(label("📅 Сроки посадки"));
                card.addView(Ui.text(this, it.dates, 13.0f, cMain, false));
                card.addView(label("🟫 Почва и заправка"));
                card.addView(Ui.text(this, it.soil, 13.0f, cMain, false));
                card.addView(label("🕳 Яма / лунка и техника"));
                card.addView(Ui.text(this, it.pit, 13.0f, cMain, false));
                card.addView(label("📏 Схема и расстояния"));
                card.addView(Ui.text(this, it.spacing, 13.0f, cMain, false));
                card.addView(label("💧 Первоначальный уход"));
                card.addView(Ui.text(this, it.care, 13.0f, cMain, false));
                if ("Плодовые деревья".equals(group) || "Косточковые".equals(group)
                        || "Ягодные кустарники".equals(group)) {
                    addDiagram(card, "plant_mulch_water");
                }
                card.addView(label("🤝 Хорошие соседи"));
                card.addView(Ui.text(this, it.friends, 13.0f, cMain, false));
                card.addView(label("⛔ Плохие соседи"));
                card.addView(Ui.text(this, it.foes, 13.0f, cMain, false));
                root.addView(card);
            }
        }
    }

    /** Схема-картинка по строковому имени ресурса (тап — увеличить). */
    private void addDiagram(LinearLayout card, String name) {
        int imgRes = DiseaseDb.imageRes(name);
        if (imgRes == 0) {
            return;
        }
        ImageView img = new ImageView(this);
        img.setImageResource(imgRes);
        img.setAdjustViewBounds(true);
        final int zoom = imgRes;
        img.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View view) {
                Ui.zoomPhoto(PlantingActivity.this, zoom);
            }
        });
        card.addView(img, new LinearLayout.LayoutParams(-1, -2));
    }

    private TextView label(String s) {
        TextView t = Ui.text(this, s, 11.0f, getResources().getColor(R.color.text_sub), true);
        t.setPadding(0, Ui.dp(this, 6.0f), 0, 0);
        return t;
    }
}
