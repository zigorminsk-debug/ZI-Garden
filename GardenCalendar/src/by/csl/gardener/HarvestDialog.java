package by.csl.gardener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/** Диалог «Сколько собрали?»: количество и единица (кг/шт/л) — запись в дневник урожая культуры. */
public final class HarvestDialog {

    private HarvestDialog() {
    }

    /** Спросить урожай: после отметки «Сбор урожая» выполненной или вручную из карточки культуры. */
    public static void ask(final Activity activity, final int year, final String plantId,
            final String plantName, String what) {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(activity, 16.0f);
        box.setPadding(pad, Ui.dp(activity, 8.0f), pad, 0);
        box.addView(Ui.text(activity, "🧺 " + (plantName.length() > 0 ? plantName + " — " : "") + what
                + ". Сколько собрали? Можно пропустить — отметка «выполнено» сохранится и так.",
                13.0f, activity.getResources().getColor(R.color.text_sub), false));
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("например, 12,5");
        box.addView(input);
        final RadioGroup units = new RadioGroup(activity);
        units.setOrientation(LinearLayout.HORIZONTAL);
        RadioButton kg = new RadioButton(activity);
        kg.setText("кг");
        kg.setId(View.generateViewId());
        RadioButton pc = new RadioButton(activity);
        pc.setText("шт");
        pc.setId(View.generateViewId());
        RadioButton liters = new RadioButton(activity);
        liters.setText("л");
        liters.setId(View.generateViewId());
        units.addView(kg);
        units.addView(pc);
        units.addView(liters);
        units.check(kg.getId());
        box.addView(units);
        final int kgId = kg.getId();
        final int pcId = pc.getId();
        new AlertDialog.Builder(activity)
                .setTitle("🌾 Урожай " + (plantName.length() > 0 ? plantName : ""))
                .setView(box)
                .setPositiveButton("Записать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        double amount = parse(input.getText().toString());
                        String unit = units.getCheckedRadioButtonId() == kgId ? "кг"
                                : units.getCheckedRadioButtonId() == pcId ? "шт" : "л";
                        if (amount > 0.0d) {
                            Storage store = new Storage(activity);
                            store.addHarvest(year, plantId, amount, unit);
                            String total = store.harvestSummary(year, plantId);
                            Ui.toast(activity, "Записано: " + String.format(java.util.Locale.US, "%.1f", Double.valueOf(amount))
                                    + " " + unit + (total.length() > 0 ? " · всего за год: " + total : ""));
                        } else {
                            Ui.toast(activity, "Количество не введено — урожай не записан");
                        }
                    }
                })
                .setNegativeButton("Пропустить", (DialogInterface.OnClickListener) null)
                .show();
    }

    private static double parse(String raw) {
        try {
            return Double.parseDouble(raw.replace(',', '.').trim());
        } catch (Exception unused) {
            return 0.0d;
        }
    }
}
