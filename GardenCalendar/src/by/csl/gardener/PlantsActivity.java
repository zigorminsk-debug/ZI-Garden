package by.csl.gardener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlantsActivity extends Activity {
    private final List<CheckBox> boxes = new ArrayList();
    private final Map<String, CheckBox> byId = new HashMap();
    private Region reg;
    private Storage store;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Storage storage = new Storage(this);
        this.store = storage;
        this.reg = Region.detect(storage.lat(), this.store.lon());
        Set<String> plants = this.store.plants();
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        TextView text = Ui.text(this, "Отметьте культуры, которые растут на участке. Календарь построит работы только по ним. Регион: " + this.reg.displayName + ". «ⓘ» или долгий тап по культуре — справка и выбор сроков созревания сортов (можно несколько). «✔» — культура хорошо подходит здесь, «⚠» — нетипична.", 13.0f, getResources().getColor(R.color.text_sub), false);
        text.setPadding(0, 0, 0, Ui.dp(this, 10.0f));
        linearLayout.addView(text);
        LinkedHashMap<String, List<Plant>> linkedHashMap = new LinkedHashMap<>();
        for (Plant plant : Plant.all()) {
            List<Plant> list = linkedHashMap.get(plant.group);
            if (list == null) {
                list = new ArrayList<>();
                linkedHashMap.put(plant.group, list);
            }
            list.add(plant);
        }
        for (Map.Entry<String, List<Plant>> entry : linkedHashMap.entrySet()) {
            Ui.section(linearLayout, this, (String) entry.getKey());
            LinearLayout card = Ui.card(this);
            for (Plant plant2 : entry.getValue()) {
                LinearLayout linearLayout2 = new LinearLayout(this);
                linearLayout2.setOrientation(0);
                linearLayout2.setGravity(16);
                CheckBox checkBox = new CheckBox(this);
                checkBox.setTextSize(15.0f);
                checkBox.setChecked(plants.contains(plant2.id));
                checkBox.setTag(plant2.id);
                checkBox.setPadding(Ui.dp(this, 4.0f), Ui.dp(this, 4.0f), Ui.dp(this, 4.0f), Ui.dp(this, 4.0f));
                linearLayout2.addView(checkBox, new LinearLayout.LayoutParams(0, -2, 1.0f));
                Button button = new Button(this);
                button.setText("ⓘ");
                button.setTextSize(14.0f);
                button.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 46.0f), Ui.dp(this, 44.0f)));
                final String str = plant2.id;
                button.setOnClickListener(new View.OnClickListener() {
                    public final void onClick(View view) {
                        PlantsActivity.this.m2lambda$onCreate$0$bycslgardenerPlantsActivity(str, view);
                    }
                });
                linearLayout2.addView(button);
                checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                    public final boolean onLongClick(View view) {
                        return PlantsActivity.this.m3lambda$onCreate$1$bycslgardenerPlantsActivity(str, view);
                    }
                });
                this.boxes.add(checkBox);
                this.byId.put(plant2.id, checkBox);
                card.addView(linearLayout2);
            }
            linearLayout.addView(card);
        }
        final TextView text2 = Ui.text(this, "", 14.0f, getResources().getColor(R.color.green_900), true);
        linearLayout.addView(text2);
        scrollView.addView(linearLayout);
        setContentView(scrollView);
        updateBadges();
        updateCount(text2);
        Iterator<CheckBox> it = this.boxes.iterator();
        while (it.hasNext()) {
            it.next().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    PlantsActivity.this.m4lambda$onCreate$2$bycslgardenerPlantsActivity(text2, compoundButton, z);
                }
            });
        }
    }

    void m2lambda$onCreate$0$bycslgardenerPlantsActivity(String str, View view) {
        CropInfoSheet.show(this, str, this.store);
    }

    boolean m3lambda$onCreate$1$bycslgardenerPlantsActivity(String str, View view) {
        CropInfoSheet.show(this, str, this.store);
        return true;
    }

    void m4lambda$onCreate$2$bycslgardenerPlantsActivity(TextView textView, CompoundButton compoundButton, boolean z) {
        save();
        updateCount(textView);
    }

    protected void onResume() {
        super.onResume();
        updateBadges();
    }

    private void updateBadges() {
        String str;
        for (Map.Entry<String, CheckBox> entry : this.byId.entrySet()) {
            Plant byId = Plant.byId(entry.getKey());
            if (byId != null) {
                String str2 = "";
                if (this.reg.isRecommended(byId.id)) {
                    str = "  ✔ рекомендуется здесь";
                } else {
                    str = this.reg.isNotTypical(byId.id) ? "  ⚠ нетипично для региона" : "";
                }
                Set<String> varietyGroups = this.store.varietyGroups(byId.id);
                boolean z = varietyGroups.size() == 1 && varietyGroups.contains("mid");
                if (CropInfo.byId(byId.id) != null && CropInfo.byId(byId.id).hasVarieties() && !z) {
                    str2 = " · " + Storage.groupsLabel(varietyGroups);
                }
                if (byId.iconRes != 0) {
                    entry.getValue().setText("  " + byId.name + str2 + str);
                    entry.getValue().setCompoundDrawablesWithIntrinsicBounds(byId.iconRes, 0, 0, 0);
                } else {
                    entry.getValue().setText(byId.icon + "  " + byId.name + str2 + str);
                    entry.getValue().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }
    }

    private void save() {
        HashSet hashSet = new HashSet();
        for (CheckBox checkBox : this.boxes) {
            if (checkBox.isChecked()) {
                hashSet.add((String) checkBox.getTag());
            }
        }
        this.store.setPlants(hashSet);
    }

    private void updateCount(TextView textView) {
        Iterator<CheckBox> it = this.boxes.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().isChecked()) {
                i++;
            }
        }
        textView.setText("Отмечено культур: " + i + " из " + this.boxes.size());
    }
}
