package by.csl.gardener;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import by.csl.gardener.Planner;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MaterialsActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    private int daysAhead = 30;
    private LinearLayout list;
    private Storage store;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.store = new Storage(this);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        TextView text = Ui.text(this, "Что нужно купить к работам ближайшего месяца. Количество посчитано по вашим растениям, цены — в валюте вашего региона.", 13.0f, getResources().getColor(R.color.text_sub), false);
        text.setPadding(0, 0, 0, Ui.dp(this, 8.0f));
        linearLayout.addView(text);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.addView(Ui.text(this, "Период: ", 14.0f, -14670049, false));
        Spinner spinner = new Spinner(this);
        final int[] iArr = {7, 14, 30};
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"7 дней", "14 дней", "30 дней"}));
        spinner.setSelection(2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
                MaterialsActivity.this.daysAhead = iArr[i];
                MaterialsActivity.this.render();
            }
        });
        linearLayout2.addView(spinner, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout.addView(linearLayout2);
        LinearLayout linearLayout3 = new LinearLayout(this);
        this.list = linearLayout3;
        linearLayout3.setOrientation(1);
        linearLayout.addView(this.list);
        scrollView.addView(linearLayout);
        setContentView(scrollView);
        render();
    }

    public void render() {
        LinearLayout linearLayout;
        String sb;
        this.list.removeAllViews();
        Storage storage = this.store;
        List<Planner.ShopItem> shoppingList = new Planner(storage, Weather.fromJson(storage.weatherCache())).shoppingList(this.daysAhead);
        List<String> bought = this.store.bought();
        if (shoppingList.isEmpty()) {
            this.list.addView(Ui.text(this, "Покупки не требуются.", 14.0f, getResources().getColor(R.color.text_sub), false));
            return;
        }
        int i = 0;
        double d = 0.0d;
        for (Planner.ShopItem shopItem : shoppingList) {
            LinearLayout card = Ui.card(this);
            final String str = shopItem.materialId;
            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(bought.contains(str));
            checkBox.setText(shopItem.name);
            checkBox.setTextSize(15.0f);
            checkBox.setTypeface(Typeface.DEFAULT_BOLD);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    MaterialsActivity.this.m12lambda$render$0$bycslgardenerMaterialsActivity(str, compoundButton, z);
                }
            });
            card.addView(checkBox);
            if (shopItem.unit.equals("шт")) {
                sb = shopItem.packs + " шт.";
                linearLayout = card;
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(shopItem.packs);
                sb2.append(" уп. по ");
                sb2.append(shopItem.pack);
                sb2.append(" = ");
                linearLayout = card;
                sb2.append(Planner.num(shopItem.packs * shopItem.packQty));
                sb2.append(" ");
                sb2.append(shopItem.unit);
                sb = sb2.toString();
            }
            LinearLayout linearLayout2 = linearLayout;
            linearLayout2.addView(Ui.text(this, "Нужно: " + sb, 13.0f, -14983648, true));
            StringBuilder sb3 = new StringBuilder("Для: ");
            for (int i2 = 0; i2 < shopItem.usedFor.size(); i2++) {
                if (i2 > 0) {
                    sb3.append(", ");
                }
                sb3.append(shopItem.usedFor.get(i2));
            }
            linearLayout2.addView(Ui.text(this, sb3.toString(), 12.0f, -10721696, false));
            if (shopItem.price > 0.0d) {
                linearLayout2.addView(Ui.text(this, String.format(Locale.US, "%.2f %s/уп. · итого %.2f %s", Double.valueOf(shopItem.price), shopItem.currency, Double.valueOf(shopItem.cost()), shopItem.currency), 12.0f, -15374912, false));
                d += shopItem.cost();
            }
            i += shopItem.packs;
            linearLayout2.setAlpha(bought.contains(str) ? 0.45f : 1.0f);
            this.list.addView(linearLayout2);
        }
        Region detect = Region.detect(this.store.lat(), this.store.lon());
        TextView text = Ui.text(this, String.format(Locale.US, "Итого: %d упаковок · ориентировочно %.2f %s", Integer.valueOf(i), Double.valueOf(d), detect.symbol), 15.0f, -14983648, true);
        text.setPadding(0, Ui.dp(this, 8.0f), 0, Ui.dp(this, 4.0f));
        this.list.addView(text);
        this.list.addView(Ui.text(this, "Цены справочные для региона: " + detect.displayName + ". " + detect.note + " Уточняйте наличие в вашем районе.", 11.0f, -7695732, false));
    }

    void m12lambda$render$0$bycslgardenerMaterialsActivity(String str, CompoundButton compoundButton, boolean z) {
        ArrayList arrayList = new ArrayList(this.store.bought());
        if (z && !arrayList.contains(str)) {
            arrayList.add(str);
        }
        if (!z) {
            arrayList.remove(str);
        }
        this.store.setBought(arrayList);
        render();
    }
}
