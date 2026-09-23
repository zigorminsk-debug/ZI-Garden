package by.csl.gardener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CropInfoSheet {
    private CropInfoSheet() {
    }

    public static void show(final Activity activity, final String str, final Storage storage) {
        Plant byId = Plant.byId(str);
        CropInfo byId2 = CropInfo.byId(str);
        if (byId == null || byId2 == null) {
            return;
        }
        LinearLayout linearLayout = new LinearLayout(activity);
        char c = 1;
        linearLayout.setOrientation(1);
        char c2 = 0;
        linearLayout.setPadding(Ui.dp(activity, 18.0f), Ui.dp(activity, 10.0f), Ui.dp(activity, 18.0f), 0);

        // 📷 Своё фото культуры: показываем, если сохранено; кнопки «добавить/убрать»
        if (PlantPhotos.has(activity, str)) {
            android.graphics.Bitmap bmp = PlantPhotos.load(activity, str,
                    Ui.dp(activity, 600.0f));
            if (bmp != null) {
                android.widget.ImageView photoView = new android.widget.ImageView(activity);
                photoView.setImageBitmap(bmp);
                photoView.setAdjustViewBounds(true);
                photoView.setPadding(0, 0, 0, Ui.dp(activity, 8.0f));
                linearLayout.addView(photoView);
            }
        }
        android.widget.Button btnPhoto = new android.widget.Button(activity);
        btnPhoto.setText(PlantPhotos.has(activity, str)
                ? "📷 Заменить свою фотографию" : "📷 Добавить свою фотографию");
        final AlertDialog[] self = new AlertDialog[1]; // чтобы закрыть себя при удалении
        btnPhoto.setOnClickListener(new android.view.View.OnClickListener() {
            public final void onClick(android.view.View view) {
                PlantPhotos.pendingPlantId = str;
                activity.startActivity(new android.content.Intent(activity, PhotoPickActivity.class));
                if (self[0] != null) {
                    self[0].dismiss();
                }
            }
        });
        linearLayout.addView(btnPhoto);
        if (PlantPhotos.has(activity, str)) {
            android.widget.Button btnRemove = new android.widget.Button(activity);
            btnRemove.setText("🗑 Убрать свою фотографию");
            btnRemove.setOnClickListener(new android.view.View.OnClickListener() {
                public final void onClick(android.view.View view) {
                    PlantPhotos.remove(activity, str);
                    Ui.toast(activity, "Своя фотография убрана");
                    if (self[0] != null) {
                        self[0].dismiss();
                    }
                    CropInfoSheet.show(activity, str, storage);
                }
            });
            linearLayout.addView(btnRemove);
            TextView note = Ui.text(activity, "Своё фото хранится только на этом устройстве (в резервную копию не входит).", 12.0f, activity.getResources().getColor(R.color.text_sub), false);
            linearLayout.addView(note);
        }

        TextView text = Ui.text(activity, byId2.card(), 14.0f, activity.getResources().getColor(R.color.text_main), false);
        text.setLineSpacing(Ui.dp(activity, 2.0f), 1.0f);
        linearLayout.addView(text);
        if (byId2.hasVarieties() && storage != null) {
            TextView text2 = Ui.text(activity, "🍓 Сроки созревания сортов на участке (можно отметить несколько — уборка планируется по каждой группе):", 14.0f, activity.getResources().getColor(R.color.green_900), true);
            text2.setPadding(0, Ui.dp(activity, 10.0f), 0, Ui.dp(activity, 4.0f));
            linearLayout.addView(text2);
            final LinkedHashSet linkedHashSet = new LinkedHashSet(storage.varietyGroups(str));
            String[][] strArr = {new String[]{"early", "Ранние (уборка примерно на 10 дней раньше)"}, new String[]{"mid", "Средние"}, new String[]{"late", "Поздние (примерно на 10 дней позже)"}};
            int i = 0;
            while (i < 3) {
                final String[] strArr2 = strArr[i];
                CheckBox checkBox = new CheckBox(activity);
                checkBox.setText(strArr2[c]);
                checkBox.setChecked(linkedHashSet.contains(strArr2[c2]));
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                        CropInfoSheet.lambda$show$0(linkedHashSet, strArr2, activity, storage, str, compoundButton, z);
                    }
                });
                linearLayout.addView(checkBox);
                i++;
                c = 1;
                c2 = 0;
            }
        }
        // 🌾 Урожай культуры: итог за этот год, прошлогодний для сравнения, запись вручную
        int yearNow = Dates.today().get(java.util.Calendar.YEAR);
        String hNow = storage == null ? "" : storage.harvestSummary(yearNow, str);
        String hPrev = storage == null ? "" : storage.harvestSummary(yearNow - 1, str);
        if (hNow.length() > 0 || hPrev.length() > 0) {
            StringBuilder hLine = new StringBuilder("🌾 Урожай ").append(yearNow).append(": ")
                    .append(hNow.length() > 0 ? hNow : "пока ничего");
            if (hPrev.length() > 0) {
                hLine.append(" · в ").append(yearNow - 1).append(": ").append(hPrev);
            }
            TextView hText = Ui.text(activity, hLine.toString(), 13.0f,
                    activity.getResources().getColor(R.color.green_900), true);
            hText.setPadding(0, Ui.dp(activity, 10.0f), 0, 0);
            linearLayout.addView(hText);
        }
        android.widget.Button btnHarvest = new android.widget.Button(activity);
        btnHarvest.setText("🧺 Записать урожай");
        btnHarvest.setAllCaps(false);
        btnHarvest.setOnClickListener(new android.view.View.OnClickListener() {
            public final void onClick(android.view.View view) {
                HarvestDialog.ask(activity, Dates.today().get(java.util.Calendar.YEAR), str,
                        byId == null ? str : byId.name, "запись вручную");
            }
        });
        linearLayout.addView(btnHarvest);

        // 📸 Фото-дневник: снимки культуры по сезонам
        int photoCount = PhotoDiary.count(activity, str);
        android.widget.Button btnPhotos = new android.widget.Button(activity);
        btnPhotos.setText("📸 Фото-дневник · " + photoCount + " фото");
        btnPhotos.setAllCaps(false);
        btnPhotos.setOnClickListener(new android.view.View.OnClickListener() {
            public final void onClick(android.view.View view) {
                PhotosActivity.show(activity, str);
            }
        });
        linearLayout.addView(btnPhotos);

        ScrollView scrollView = new ScrollView(activity);
        scrollView.addView(linearLayout);
        AlertDialog dialog = new AlertDialog.Builder(activity).setIcon(byId.iconRes != 0 ? byId.iconRes : 0).setTitle((byId.iconRes != 0 ? "" : byId.icon + " ") + byId.name).setView(scrollView).setPositiveButton("Закрыть", (DialogInterface.OnClickListener) null).show();
        self[0] = dialog;
    }

    static void lambda$show$0(Set set, String[] strArr, Activity activity, Storage storage, String str, CompoundButton compoundButton, boolean z) {
        if (z) {
            set.add(strArr[0]);
        } else {
            set.remove(strArr[0]);
        }
        if (set.isEmpty()) {
            set.add(strArr[0]);
            ((CheckBox) compoundButton).setChecked(true);
            Ui.toast(activity, "Должна остаться хотя бы одна группа сроков");
        } else {
            storage.setVarietyGroups(str, set);
            Ui.toast(activity, "Сохранено: " + Storage.groupsLabel(set));
        }
    }
}
