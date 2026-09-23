package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 🔎 Поиск по всем справочникам приложения: посадка, культуры, болезни,
 * вредители, дефициты элементов. Живой ввод — результаты обновляются на лету.
 */
public class SearchActivity extends Activity {

    private final Map<String, Plant> plantsById = new HashMap<String, Plant>();
    private EditText input;
    private LinearLayout results;
    private int cMain;
    private int cSub;
    private int cGreen;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    public static void show(Context ctx) {
        ctx.startActivity(new Intent(ctx, SearchActivity.class));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("Поиск по справочнику");

        for (Plant p : Plant.all()) {
            plantsById.put(p.id, p);
        }

        cMain = getResources().getColor(R.color.text_main);
        cSub = getResources().getColor(R.color.text_sub);
        cGreen = getResources().getColor(R.color.green_900);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        LinearLayout searchCard = Ui.card(this);
        searchCard.addView(Ui.text(this, "🔎 Что ищем в саду?", 15.0f, cMain, true));
        input = new EditText(this);
        input.setHint("Культура, болезнь, вредитель, элемент…");
        input.setTextSize(15.0f);
        input.setSingleLine(true);
        input.setMaxLines(1);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setBackgroundResource(R.drawable.card_bg);
        input.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 10.0f), Ui.dp(this, 12.0f), Ui.dp(this, 10.0f));
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, -2);
        inputParams.topMargin = Ui.dp(this, 6.0f);
        searchCard.addView(input, inputParams);
        TextView hint = Ui.text(this,
                "Ищет сразу во всех разделах: посадка (46 культур), карточки культур, "
                        + "болезни, вредители, дефициты элементов, агроприёмы и фенология. "
                        + "Введите минимум 2 буквы.",
                12.0f, cSub, false);
        hint.setPadding(0, Ui.dp(this, 6.0f), 0, 0);
        searchCard.addView(hint);
        root.addView(searchCard);

        results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        root.addView(results);

        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void afterTextChanged(Editable s) {
                render(s.toString());
            }
        });

        render("");
    }

    /** Перерисовать результаты по запросу. */
    private void render(String raw) {
        results.removeAllViews();
        String q = raw.trim().toLowerCase(Locale.getDefault());
        if (q.length() < 2) {
            results.addView(examplesCard());
            return;
        }

        int total = 0;
        total += addPlanting(q);
        total += addCrops(q);
        total += addDiseases(q, false);
        total += addDiseases(q, true);
        total += addDeficiencies(q);
        total += addTechniques(q);
        total += addPhenology(q);

        if (total == 0) {
            LinearLayout empty = Ui.card(this);
            empty.addView(Ui.text(this, "😕 Ничего не нашлось", 14.0f, cMain, true));
            TextView e = Ui.text(this,
                    "Попробуйте другое слово: название культуры («смородина»), болезни («парша», "
                            + "«мучнистая роса»), вредителя («тля», «плодожорка»), элемент («азот», «калий»), "
                            + "приём («прививка», «обрезка») или примету («черёмуха»).",
                    12.0f, cSub, false);
            e.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
            empty.addView(e);
            results.addView(empty);
        }
    }

    /** Стартовое состояние: примеры запросов одним тапом. */
    private LinearLayout examplesCard() {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Попробуйте, например:", 13.0f, cMain, true));
        String[] examples = {"парша", "тля", "азот", "яблоня", "мульча", "прививка", "черёмуха"};
        for (final String ex : examples) {
            TextView chip = Ui.text(this, "🔎 " + ex, 14.0f, cGreen, true);
            chip.setPadding(0, Ui.dp(this, 8.0f), 0, 0);
            chip.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    input.setText(ex);
                    input.setSelection(ex.length());
                }
            });
            card.addView(chip);
        }
        return card;
    }

    /** Посадка: инструкции по культурам (по названию и содержанию). */
    private int addPlanting(String q) {
        int found = 0;
        LinearLayout section = null;
        for (PlantingGuide.Entry e : PlantingGuide.all()) {
            Plant p = plantsById.get(e.plantId);
            if (p == null || !hit(p.name, q) && !hit(e.dates, q) && !hit(e.soil, q) && !hit(e.pit, q)
                    && !hit(e.spacing, q) && !hit(e.care, q) && !hit(e.friends, q) && !hit(e.foes, q)) {
                continue;
            }
            if (section == null) {
                section = startSection("🌱 Посадка растений");
            }
            final String plantId = e.plantId;
            section.addView(row(p.icon, p.name, "Инструкция по посадке", new View.OnClickListener() {
                public void onClick(View view) {
                    PlantingActivity.showFor(SearchActivity.this, plantId);
                }
            }));
            found++;
            if (found >= 12) {
                break;
            }
        }
        return found;
    }

    /** Карточки культур: сорта, урожай, история. */
    private int addCrops(String q) {
        int found = 0;
        LinearLayout section = null;
        for (final Plant p : Plant.all()) {
            if (!hit(p.name, q)) {
                continue;
            }
            if (section == null) {
                section = startSection("🍏 Культуры");
            }
            section.addView(row(p.icon, p.name, "Карточка: сорта, урожай, журнал", new View.OnClickListener() {
                public void onClick(View view) {
                    CropInfoSheet.show(SearchActivity.this, p.id, new Storage(SearchActivity.this));
                }
            }));
            found++;
            if (found >= 12) {
                break;
            }
        }
        return found;
    }

    /** Болезни и вредители из справочника. */
    private int addDiseases(String q, boolean pests) {
        int found = 0;
        LinearLayout section = null;
        for (final Disease d : DiseaseDb.all(this)) {
            boolean isPest = d.kind != null && d.kind.startsWith("Вредитель");
            if (isPest != pests) {
                continue;
            }
            Plant p = plantsById.get(d.plantId);
            String plantName = p != null ? p.name : d.plantId;
            if (!hit(d.name, q) && !hit(plantName, q) && !hit(d.symptoms, q)) {
                continue;
            }
            if (section == null) {
                section = startSection(pests ? "🐛 Вредители" : "🍂 Болезни");
            }
            section.addView(row(pests ? "🐛" : "🍂", d.name, plantName, new View.OnClickListener() {
                public void onClick(View view) {
                    DiseaseActivity.show(SearchActivity.this, d.plantId, d.kind.startsWith("Вредитель") ? "pest" : "disease");
                }
            }));
            found++;
            if (found >= 12) {
                break;
            }
        }
        return found;
    }

    /** Дефициты элементов питания. */
    private int addDeficiencies(String q) {
        int found = 0;
        LinearLayout section = null;
        for (final DeficiencyGuide.Item it : DeficiencyGuide.all()) {
            if (!hit(it.name, q) && !hit(it.signs, q)) {
                continue;
            }
            if (section == null) {
                section = startSection("🔬 Дефициты элементов");
            }
            section.addView(row(it.symbol, it.name, "Симптомы и скорая помощь", new View.OnClickListener() {
                public void onClick(View view) {
                    DeficiencyActivity.show(SearchActivity.this);
                }
            }));
            found++;
            if (found >= 12) {
                break;
            }
        }
        return found;
    }

    /** Агроприёмы: обрезка, рассада, размножение. */
    private int addTechniques(String q) {
        int found = 0;
        LinearLayout section = null;
        for (final TechniqueGuide.Item it : TechniqueGuide.all()) {
            if (!Search.matchesTechnique(it, q)) {
                continue;
            }
            if (section == null) {
                section = startSection("🛠 Агроприёмы");
            }
            section.addView(row(it.icon, it.title, it.group + " · сроки, техника, ошибки", new View.OnClickListener() {
                public void onClick(View view) {
                    PlantingActivity.showTechnique(SearchActivity.this, it.id);
                }
            }));
            found++;
            if (found >= 12) {
                break;
            }
        }
        return found;
    }

    /** Фенология: природные ориентиры сроков работ. */
    private int addPhenology(String q) {
        int found = 0;
        LinearLayout section = null;
        for (final PhenologyGuide.Sign s : PhenologyGuide.all()) {
            if (!Search.matchesSign(s, q)) {
                continue;
            }
            if (section == null) {
                section = startSection("🌸 Фенология");
            }
            section.addView(row(s.emoji, s.title, s.season + " · " + s.period, new View.OnClickListener() {
                public void onClick(View view) {
                    PhenologyActivity.show(SearchActivity.this);
                }
            }));
            found++;
            if (found >= 12) {
                break;
            }
        }
        return found;
    }

    /** Заголовок секции результатов. */
    private LinearLayout startSection(String title) {
        Ui.section(results, this, title);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        results.addView(box);
        return box;
    }

    /** Строка результата с иконкой, названием, подсказкой и переходом. */
    private View row(String icon, String title, String subtitle, View.OnClickListener click) {
        LinearLayout card = Ui.card(this);
        card.setOnClickListener(click);

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView iconView = Ui.text(this, icon == null || icon.length() == 0 ? "•" : icon, 20.0f, cMain, false);
        line.addView(iconView, new LinearLayout.LayoutParams(Ui.dp(this, 40.0f), -2));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(Ui.text(this, title, 14.0f, cGreen, true));
        TextView sub = Ui.text(this, subtitle, 12.0f, cSub, false);
        sub.setPadding(0, Ui.dp(this, 2.0f), 0, 0);
        texts.addView(sub);
        line.addView(texts, new LinearLayout.LayoutParams(-1, -2));

        TextView arrow = Ui.text(this, "›", 20.0f, cSub, true);
        line.addView(arrow, new LinearLayout.LayoutParams(-2, -2));

        card.addView(line);
        return card;
    }

    /** Подстрочный поиск без регистра. */
    private static boolean hit(String hay, String q) {
        return hay != null && hay.toLowerCase(Locale.getDefault()).contains(q);
    }
}
