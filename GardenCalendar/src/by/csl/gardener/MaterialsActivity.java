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
import java.util.Calendar;
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
        linearLayout2.addView(Ui.text(this, "Период: ", 14.0f, getResources().getColor(R.color.text_main), false));
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
        Ui.setContent(this, scrollView);
        render();
    }

    /** Карточка «Бюджет сезона»: план покупок на год, купленное и разбивка по месяцам. */
    private LinearLayout budgetCard(Planner planner) {
        int year = Dates.today().get(Calendar.YEAR);
        Planner.Budget budget = planner.seasonBudget(year);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "📊 Бюджет сезона " + year, 17.0f, getResources().getColor(R.color.green_900), true));
        if (budget.planned <= 0.0d) {
            card.addView(Ui.text(this, "Покупки на этот сезон не запланированы — отметьте растения в разделе «Растения».", 13.0f, getResources().getColor(R.color.text_sub), false));
            return card;
        }
        card.addView(Ui.text(this, String.format(Locale.US, "План: %d позиций · %d уп. · %s", Integer.valueOf(budget.positions), Integer.valueOf(budget.packs), Ui.money(budget.planned, budget.currency)), 14.0f, getResources().getColor(R.color.text_main), false));
        if (budget.boughtCost > 0.0d) {
            card.addView(Ui.text(this, "Куплено: " + Ui.money(budget.boughtCost, budget.currency) + " · осталось " + Ui.money(budget.planned - budget.boughtCost, budget.currency), 14.0f, getResources().getColor(R.color.text_main), false));
        } else {
            card.addView(Ui.text(this, "Отмечайте купленное галочками в списке ниже — здесь появятся итоги сезона.", 13.0f, getResources().getColor(R.color.text_sub), false));
        }
        card.addView(Ui.text(this, "Расходы по месяцам (план, справочные цены):", 13.0f, getResources().getColor(R.color.text_sub), true));
        double maxMonth = 0.0d;
        for (int m = 0; m < 12; m++) {
            if (budget.byMonth[m] > maxMonth) {
                maxMonth = budget.byMonth[m];
            }
        }
        if (maxMonth <= 0.0d) {
            maxMonth = 1.0d;
        }
        for (int m = 0; m < 12; m++) {
            if (budget.byMonth[m] <= 0.0d) {
                continue;
            }
            int blocks = (int) Math.round((10.0d * budget.byMonth[m]) / maxMonth);
            if (blocks < 1) {
                blocks = 1;
            }
            StringBuilder bar = new StringBuilder();
            for (int b = 0; b < blocks; b++) {
                bar.append('█');
            }
            card.addView(Ui.text(this, Dates.MONTHS_NOM[m] + "  " + bar + "  " + Ui.money(budget.byMonth[m], budget.currency), 12.0f, getResources().getColor(R.color.text_sub), false));
        }
        return card;
    }

    public void render() {
        LinearLayout linearLayout;
        String sb;
        this.list.removeAllViews();
        Storage storage = this.store;
        Planner planner = new Planner(storage, Weather.fromJson(storage.weatherCache()));
        this.list.addView(budgetCard(planner));
        List<Planner.ShopItem> shoppingList = planner.shoppingList(this.daysAhead);
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
            linearLayout2.addView(Ui.text(this, "Нужно: " + sb, 13.0f, getResources().getColor(R.color.green_900), true));
            StringBuilder sb3 = new StringBuilder("Для: ");
            for (int i2 = 0; i2 < shopItem.usedFor.size(); i2++) {
                if (i2 > 0) {
                    sb3.append(", ");
                }
                sb3.append(shopItem.usedFor.get(i2));
            }
            linearLayout2.addView(Ui.text(this, sb3.toString(), 12.0f, getResources().getColor(R.color.text_sub), false));
            if (shopItem.price > 0.0d) {
                linearLayout2.addView(Ui.text(this, String.format(Locale.US, "%.2f %s/уп. · итого %.2f %s", Double.valueOf(shopItem.price), shopItem.currency, Double.valueOf(shopItem.cost()), shopItem.currency), 12.0f, getResources().getColor(R.color.accent), false));
                d += shopItem.cost();
            }
            i += shopItem.packs;
            linearLayout2.setAlpha(bought.contains(str) ? 0.45f : 1.0f);
            this.list.addView(linearLayout2);
        }
        Region detect = Region.detect(this.store.lat(), this.store.lon());
        TextView text = Ui.text(this, String.format(Locale.US, "Итого: %d упаковок · ориентировочно %.2f %s", Integer.valueOf(i), Double.valueOf(d), detect.symbol), 15.0f, getResources().getColor(R.color.green_900), true);
        text.setPadding(0, Ui.dp(this, 8.0f), 0, Ui.dp(this, 4.0f));
        this.list.addView(text);
        this.list.addView(Ui.text(this, "Цены справочные для региона: " + detect.displayName + ". " + detect.note + " Уточняйте наличие в вашем районе.", 11.0f, getResources().getColor(R.color.text_note), false));
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
