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
    private String filter = "";
    private String kindFilter; // null = всё, "pest" = только вредители, "disease" = только болезни

    public static void show(Context ctx, String plantId) {
        show(ctx, plantId, null);
    }

    /** Открыть справочник по культуре, сразу в разделе «вредители» («pest») или «болезни» («disease»). */
    public static void show(Context ctx, String plantId, String kind) {
        Intent intent = new Intent(ctx, DiseaseActivity.class);
        intent.putExtra("plant", plantId);
        if (kind != null) intent.putExtra("kind", kind);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.plantId = getIntent().getStringExtra("plant");
        this.kindFilter = getIntent().getStringExtra("kind");
        updateTitle();

        ScrollView scroll = new ScrollView(this);
        this.root = new LinearLayout(this);
        this.root.setOrientation(LinearLayout.VERTICAL);
        this.root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        scroll.addView(this.root);

        // Поиск по справочнику: название болезни, симптом («паутина», «пятна») или препарат
        android.widget.EditText search = new android.widget.EditText(this);
        search.setHint("🔎 Поиск: название, симптом, препарат…");
        search.setSingleLine(true);
        search.setPadding(Ui.dp(this, 12.0f), 0, Ui.dp(this, 12.0f), 0);
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(android.text.Editable s) {
                DiseaseActivity.this.filter = s == null ? "" : s.toString();
                DiseaseActivity.this.render();
            }
        });

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(search, new LinearLayout.LayoutParams(-1, -2));
        wrap.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        Ui.setContent(this, wrap);
        render();
    }

    /** Вредитель по полю kind каталога: записи вредителей начинаются со слова «Вредитель». */
    private boolean matchesKind(Disease dz) {
        boolean pest = dz.kind != null && dz.kind.startsWith("Вредитель");
        if ("pest".equals(this.kindFilter)) return pest;
        if ("disease".equals(this.kindFilter)) return !pest;
        return true;
    }

    private String kindTitle() {
        if ("pest".equals(this.kindFilter)) return "Вредители";
        if ("disease".equals(this.kindFilter)) return "Болезни";
        return "Болезни и вредители";
    }

    private void updateTitle() {
        Plant plant = Plant.byId(this.plantId);
        setTitle(kindTitle() + ": " + (plant != null ? plant.name : this.plantId));
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

        // Переключатель раздела: всё / только болезни / только вредители
        LinearLayout kindRow = new LinearLayout(this);
        kindRow.setOrientation(LinearLayout.HORIZONTAL);
        final String[][] kinds = {{"", "Все"}, {"disease", "🍂 Болезни"}, {"pest", "🐛 Вредители"}};
        for (final String[] kv : kinds) {
            Button kb = new Button(this);
            final String cur = this.kindFilter == null ? "" : this.kindFilter;
            kb.setText(kv[1] + (kv[0].equals(cur) ? " ✓" : ""));
            kb.setTextSize(13.0f);
            kb.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    DiseaseActivity.this.kindFilter = kv[0].length() == 0 ? null : kv[0];
                    DiseaseActivity.this.updateTitle();
                    DiseaseActivity.this.render();
                }
            });
            kindRow.addView(kb, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }
        this.root.addView(kindRow);

        List<Disease> all = DiseaseDb.forPlant(this, this.plantId);
        List<Disease> byKind = new java.util.ArrayList<>();
        for (Disease dz : all) {
            if (matchesKind(dz)) byKind.add(dz);
        }
        List<Disease> list = new java.util.ArrayList<>();
        for (Disease dz : byKind) {
            if (Search.matches(dz, this.filter)) {
                list.add(dz);
            }
        }
        String noun = "pest".equals(this.kindFilter) ? "вредителей"
                : ("disease".equals(this.kindFilter) ? "болезней" : "болезней и вредителей");
        int updated = RemoteDiseases.cachedCount(this);
        TextView header = Ui.text(this, (this.filter.trim().length() > 0
                ? "Найдено по запросу «" + this.filter.trim() + "»: " + list.size() + " из " + byKind.size() + ". "
                : "Найдено " + noun + ": " + list.size())
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
                final int photoRes = dz.imageRes();
                img.setOnClickListener(new View.OnClickListener() {
                    public final void onClick(View view) {
                        Ui.zoomPhoto(DiseaseActivity.this, photoRes);
                    }
                });
                card.addView(img, new LinearLayout.LayoutParams(-1, -2));
                TextView cap = Ui.text(this, "На фото: типичные симптомы — «" + dz.name + "». Нажмите — открыть на весь экран.", 12.0f, cSub, false);
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

            // Стадии развития вредителя: фото каждой стадии, где развивается и какой вред
            PestStage[] stages = PestStages.forPest(dz.id);
            if (stages != null && stages.length > 0) {
                TextView stLabel = Ui.text(this, "🔄 Стадии развития: где живут и какой вред", 14.0f, cMain, true);
                stLabel.setPadding(0, Ui.dp(this, 8.0f), 0, Ui.dp(this, 4.0f));
                card.addView(stLabel);
                for (PestStage st : stages) {
                    int stRes = DiseaseDb.imageRes(st.image);
                    if (stRes != 0) {
                        ImageView simg = new ImageView(this);
                        simg.setImageResource(stRes);
                        simg.setAdjustViewBounds(true);
                        final int zoomRes = stRes;
                        simg.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                Ui.zoomPhoto(DiseaseActivity.this, zoomRes);
                            }
                        });
                        card.addView(simg, new LinearLayout.LayoutParams(-1, -2));
                    }
                    TextView stTitle = Ui.text(this, "▸ " + st.title, 14.0f, cMain, true);
                    stTitle.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
                    card.addView(stTitle);
                    card.addView(Ui.text(this, "Где развивается: " + st.where, 13.0f, cMain, false));
                    card.addView(Ui.text(this, "Какой вред: " + st.harm, 13.0f, cMain, false));
                }
            }

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

            // ── Перекрёстная ссылка: календарь обработок по фенофазам ──
            final Disease sprayDz = dz;
            if (SprayLinks.techniqueFor(dz.id) != null) {
                TextView spray = Ui.text(this, "🛡️ Календарь обработок по фенофазам →", 13.0f,
                        getResources().getColor(R.color.accent), true);
                spray.setPadding(0, Ui.dp(this, 8.0f), 0, 0);
                spray.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        PlantingActivity.showTechnique(DiseaseActivity.this,
                                SprayLinks.techniqueFor(sprayDz.id));
                    }
                });
                card.addView(spray);
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
