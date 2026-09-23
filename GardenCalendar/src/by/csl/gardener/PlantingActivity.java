package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 🌱 Посадка растений: интерактивный выбор культуры и переход к рекомендациям —
 * сроки, ямы, схемы, первый уход и соседство.
 */
public class PlantingActivity extends Activity {

    /** Порядок разделов справочника (пустые скрываются). */
    private static final String[] GROUPS = {
            "Плодовые деревья", "Косточковые", "Ягодные кустарники", "Ягодники",
            "Овощные культуры", "Виноград и лианы", "Декоративные", "Теплица и постройки"
    };

    /** Порядок разделов агроприёмов. */
    private static final String[] TECH_GROUPS = {
            "Обрезка и формировка", "Рассада", "Размножение"
    };

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    public static void show(Context ctx) {
        ctx.startActivity(new Intent(ctx, PlantingActivity.class));
    }

    /** Открыть рекомендации по конкретной культуре. */
    public static void showFor(Context ctx, String plantId) {
        ctx.startActivity(new Intent(ctx, PlantingActivity.class).putExtra("plantId", plantId));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        String plantId = getIntent().getStringExtra("plantId");
        PlantingGuide.Entry entry = plantId != null ? PlantingGuide.byPlant(plantId) : null;
        if (entry != null) {
            renderDetail(entry);
        } else {
            String technique = getIntent().getStringExtra("technique");
            TechniqueGuide.Item item = technique != null ? TechniqueGuide.byId(technique) : null;
            if (item != null) {
                renderTechnique(item);
            } else {
                renderList();
            }
        }
    }

    /** Режим списка: интерактивный выбор культуры с переходом к рекомендациям. */
    private void renderList() {
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
        TextView count = Ui.text(this,
                "В справочнике: " + PlantingGuide.all().size() + " культур + "
                        + TechniqueGuide.all().size() + " агроприёмов · 👆 выберите ниже",
                12.0f, cSub, true);
        count.setPadding(0, Ui.dp(this, 6.0f), 0, 0);
        intro.addView(count);
        root.addView(intro);

        for (String group : GROUPS) {
            List<Plant> plants = new ArrayList<>();
            for (Plant p : Plant.all()) {
                if (group.equals(p.group) && PlantingGuide.byPlant(p.id) != null) {
                    plants.add(p);
                }
            }
            if (plants.isEmpty()) {
                continue;
            }
            Ui.section(root, this, group);
            for (final Plant plant : plants) {
                root.addView(pickerRow(plant));
            }
        }

        // ── Агроприёмы: обрезка, рассада, размножение ──
        for (String techGroup : TECH_GROUPS) {
            List<TechniqueGuide.Item> items = new ArrayList<>();
            for (TechniqueGuide.Item it : TechniqueGuide.all()) {
                if (techGroup.equals(it.group)) {
                    items.add(it);
                }
            }
            if (items.isEmpty()) {
                continue;
            }
            Ui.section(root, this, techGroup);
            for (final TechniqueGuide.Item it : items) {
                root.addView(techRow(it));
            }
        }
    }

    /** Строка агроприёма: иконка, название, тап — переход к инструкции. */
    private View techRow(final TechniqueGuide.Item it) {
        LinearLayout card = Ui.card(this);
        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(PlantingActivity.this, PlantingActivity.class)
                        .putExtra("technique", it.id));
            }
        });
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView icon = Ui.text(this, it.icon, 22.0f, getResources().getColor(R.color.text_main), false);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(Ui.dp(this, 44.0f), -2);
        row.addView(icon, iconParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(Ui.text(this, it.title, 15.0f, getResources().getColor(R.color.green_900), true));
        TextView hint = Ui.text(this, "Агроприём: сроки · инструмент · техника · ошибки", 12.0f,
                getResources().getColor(R.color.text_sub), false);
        hint.setPadding(0, Ui.dp(this, 2.0f), 0, 0);
        texts.addView(hint);
        row.addView(texts, new LinearLayout.LayoutParams(-1, -2));

        TextView arrow = Ui.text(this, "›", 22.0f, getResources().getColor(R.color.text_sub), true);
        row.addView(arrow, new LinearLayout.LayoutParams(-2, -2));

        card.addView(row);
        return card;
    }

    /** Режим инструкции по агроприёму. */
    private void renderTechnique(TechniqueGuide.Item it) {
        setTitle(it.title);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        LinearLayout back = Ui.card(this);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
        back.addView(Ui.text(this, "📋 ← Все культуры и приёмы", 14.0f,
                getResources().getColor(R.color.green_900), true));
        root.addView(back);

        int cMain = getResources().getColor(R.color.text_main);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, it.icon + " " + it.title, 15.0f,
                getResources().getColor(R.color.green_900), true));
        addDiagram(card, it.diagram);
        card.addView(label("🕒 Когда проводить"));
        card.addView(Ui.text(this, it.whenText, 13.0f, cMain, false));
        card.addView(label("🛠 Инструмент и материалы"));
        card.addView(Ui.text(this, it.tools, 13.0f, cMain, false));
        card.addView(label("📐 Техника шаг за шагом"));
        card.addView(Ui.text(this, it.how, 13.0f, cMain, false));
        card.addView(label("⚠️ Частые ошибки"));
        card.addView(Ui.text(this, it.mistakes, 13.0f, cMain, false));
        root.addView(card);
    }

    /** Строка выбора культуры: иконка, название, тап — переход к рекомендациям. */
    private View pickerRow(final Plant plant) {
        LinearLayout card = Ui.card(this);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView icon = Ui.text(this, plant.icon, 22.0f, getResources().getColor(R.color.text_main), false);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(Ui.dp(this, 44.0f), -2);
        row.addView(icon, iconParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(Ui.text(this, plant.name, 15.0f, getResources().getColor(R.color.green_900), true));
        TextView hint = Ui.text(this, "Сроки · яма · схема · уход · соседи", 12.0f,
                getResources().getColor(R.color.text_sub), false);
        hint.setPadding(0, Ui.dp(this, 2.0f), 0, 0);
        texts.addView(hint);
        row.addView(texts, new LinearLayout.LayoutParams(-1, -2));

        TextView arrow = Ui.text(this, "›", 22.0f, getResources().getColor(R.color.text_sub), true);
        row.addView(arrow, new LinearLayout.LayoutParams(-2, -2));

        card.addView(row);
        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showFor(PlantingActivity.this, plant.id);
            }
        });
        return card;
    }

    /** Режим рекомендации: подробная карточка выбранной культуры. */
    private void renderDetail(PlantingGuide.Entry entry) {
        Plant plant = null;
        for (Plant p : Plant.all()) {
            if (p.id.equals(entry.plantId)) {
                plant = p;
                break;
            }
        }
        setTitle("Посадка: " + (plant != null ? plant.name : entry.plantId));

        ScrollView scroll = new ScrollView(this);
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        LinearLayout back = Ui.card(this);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
        back.addView(Ui.text(this, "📋 ← Все культуры", 14.0f,
                getResources().getColor(R.color.green_900), true));
        root.addView(back);

        root.addView(entryCard(entry, plant));
    }

    /** Карточка с рекомендациями по посадке выбранной культуры. */
    private LinearLayout entryCard(PlantingGuide.Entry it, Plant plant) {
        int cMain = getResources().getColor(R.color.text_main);
        int cGreen = getResources().getColor(R.color.green_900);
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
        if (woody(plant)) {
            addDiagram(card, "plant_mulch_water");
        }
        card.addView(label("🤝 Хорошие соседи"));
        card.addView(Ui.text(this, it.friends, 13.0f, cMain, false));
        card.addView(label("⛔ Плохие соседи"));
        card.addView(Ui.text(this, it.foes, 13.0f, cMain, false));
        return card;
    }

    /** Древесные (деревья и кустарники) — им показываем и схему полива с мульчой. */
    private static boolean woody(Plant plant) {
        return plant != null && ("Плодовые деревья".equals(plant.group)
                || "Косточковые".equals(plant.group)
                || "Ягодные кустарники".equals(plant.group));
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
        img.setOnClickListener(new View.OnClickListener() {
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
