package by.csl.gardener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import by.csl.gardener.Task;

public final class TaskDialog {
    private TaskDialog() {
    }

    public static void show(final Context context, final Task task, final Runnable runnable) {
        int r8;
        ScrollView scrollView = new ScrollView(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        int dp = Ui.dp(context, 18.0f);
        linearLayout.setPadding(dp, dp, dp, dp);
        Plant byId = Plant.byId(task.plantId);
        StringBuilder sb = new StringBuilder();
        sb.append(byId == null ? "🌱 " : byId.icon + " ");
        sb.append(task.plantName);
        linearLayout.addView(Ui.text(context, sb.toString(), 24.0f, -14983648, true));
        float f = 6.0f;
        int i = -2;
        int i2 = -1;
        if (byId != null && (context instanceof Activity)) {
            Button button = new Button(context);
            button.setText("📖 О культуре: состав, польза, сорта, сроки");
            button.setTextSize(13.0f);
            button.setAllCaps(false);
            final String str = task.plantId;
            final Activity activity = (Activity) context;
            button.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    Activity activity2 = activity;
                    CropInfoSheet.show(activity2, str, new Storage(activity2));
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = Ui.dp(context, 6.0f);
            button.setLayoutParams(layoutParams);
            linearLayout.addView(button);
        }
        Plant dlgPlant = Plant.byId(task.plantId);
        TextView dlgTitle = Ui.text(context, (dlgPlant != null && dlgPlant.iconRes != 0 ? dlgPlant.name : (dlgPlant != null ? dlgPlant.icon + " " + dlgPlant.name : "")) + " — " + Operation.icon(task.op) + " " + task.title, 17.0f, -14670049, true);
        if (dlgPlant != null && dlgPlant.iconRes != 0) {
            dlgTitle.setCompoundDrawablesWithIntrinsicBounds(dlgPlant.iconRes, 0, 0, 0);
            dlgTitle.setCompoundDrawablePadding(Ui.dp(context, 6.0f));
        }
        linearLayout.addView(dlgTitle);
        linearLayout.addView(Ui.text(context, Operation.label(task.op) + " · окно: " + task.window + " · " + Dates.fmt(task.year, task.month, task.day) + ", " + Dates.weekday(task.year, task.month, task.day), 13.0f, -10721696, false));
        TextView weatherBadge = Ui.weatherBadge(context, task);
        if (weatherBadge != null) {
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams2.topMargin = Ui.dp(context, 8.0f);
            weatherBadge.setLayoutParams(layoutParams2);
            linearLayout.addView(weatherBadge);
        }
        int forTask = Scheme.forTask(task);
        if (forTask != 0) {
            TextView text = Ui.text(context, "Схема выполнения", 15.0f, -14670049, true);
            text.setPadding(0, Ui.dp(context, 14.0f), 0, Ui.dp(context, 6.0f));
            linearLayout.addView(text);
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(forTask);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            linearLayout.addView(imageView);
            TextView text2 = Ui.text(context, Scheme.captionFor(forTask), 12.0f, -10721696, false);
            text2.setPadding(0, Ui.dp(context, 4.0f), 0, Ui.dp(context, 4.0f));
            text2.setLineSpacing(Ui.dp(context, 2.0f), 1.0f);
            linearLayout.addView(text2);
        }
        TextView text3 = Ui.text(context, "Как выполнять", 15.0f, -14670049, true);
        text3.setPadding(0, Ui.dp(context, 14.0f), 0, Ui.dp(context, 4.0f));
        linearLayout.addView(text3);
        TextView text4 = Ui.text(context, task.text, 14.0f, -14670049, false);
        text4.setLineSpacing(Ui.dp(context, 3.0f), 1.0f);
        linearLayout.addView(text4);
        if (task.items.isEmpty()) {
            r8 = 0;
        } else {
            TextView text5 = Ui.text(context, "Материалы и дозировки", 15.0f, -14670049, true);
            text5.setPadding(0, Ui.dp(context, 14.0f), 0, Ui.dp(context, 4.0f));
            linearLayout.addView(text5);
            if (task.solutionL > 0.0d) {
                TextView text6 = Ui.text(context, "Рабочий раствор: " + Planner.num(task.solutionL) + " л на растение (готовить в день применения)", 13.0f, -15374912, false);
                text6.setPadding(0, 0, 0, Ui.dp(context, 6.0f));
                linearLayout.addView(text6);
            }
            for (Task.Item item : task.items) {
                LinearLayout linearLayout2 = new LinearLayout(context);
                linearLayout2.setOrientation(1);
                linearLayout2.setBackgroundResource(R.drawable.card_bg);
                linearLayout2.setPadding(Ui.dp(context, 10.0f), Ui.dp(context, 8.0f), Ui.dp(context, 10.0f), Ui.dp(context, 8.0f));
                LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(i2, i);
                layoutParams3.bottomMargin = Ui.dp(context, f);
                linearLayout2.setLayoutParams(layoutParams3);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(item.alternative ? "↔ " : "• ");
                sb2.append(item.name);
                linearLayout2.addView(Ui.text(context, sb2.toString(), 14.0f, item.alternative ? -10721696 : -14670049, !item.alternative));
                linearLayout2.addView(Ui.text(context, "Норма: " + item.dose, 13.0f, -10721696, false));
                StringBuilder sb3 = new StringBuilder();
                sb3.append(item.alternative ? "Если брать его: " : "Нужно: ");
                sb3.append(item.need);
                linearLayout2.addView(Ui.text(context, sb3.toString(), 13.0f, item.alternative ? -10721696 : -14983648, !item.alternative));
                if (item.price > 0.0d) {
                    StringBuilder sb4 = new StringBuilder("Цена упаковки: ");
                    sb4.append(Ui.money(item.price, item.currency));
                    sb4.append(item.alternative ? "" : " · ориентировочно " + Ui.money(item.price * item.packs, item.currency));
                    linearLayout2.addView(Ui.text(context, sb4.toString(), 12.0f, -15374912, false));
                }
                linearLayout.addView(linearLayout2);
                f = 6.0f;
                i = -2;
                i2 = -1;
            }
            TextView text7 = Ui.text(context, "Ориентировочная стоимость: " + Ui.money(task.totalCost(), task.currency) + " (без учёта заменителей)", 14.0f, -14983648, true);
            r8 = 0;
            text7.setPadding(0, Ui.dp(context, 4.0f), 0, Ui.dp(context, 4.0f));
            linearLayout.addView(text7);
            linearLayout.addView(Ui.text(context, "Цены — справочные, по рознице Республики Беларусь (csl.by и др.).", 11.0f, -7695732, false));
        }
        TextView text8 = Ui.text(context, "⚠️ Работайте в перчатках и респираторе. Соблюдайте срок ожидания до сбора урожая, указанный на упаковке препарата. Не смешивайте препараты без проверки совместимости.", 12.0f, -5091328, false);
        text8.setPadding(r8, Ui.dp(context, 12.0f), r8, r8);
        linearLayout.addView(text8);
        scrollView.addView(linearLayout);
        AlertDialog.Builder positiveButton = new AlertDialog.Builder(context).setView(scrollView).setPositiveButton(task.done ? "Снять отметку" : "Отметить выполненной", new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i3) {
                TaskDialog.lambda$show$1(context, task, runnable, dialogInterface, i3);
            }
        });
        AlertDialog create = positiveButton.setNegativeButton("Закрыть", (DialogInterface.OnClickListener) null).create();
        create.show();
        Button button2 = create.getButton(-1);
        if (button2 != null) {
            button2.setTypeface(null, 1);
            button2.setGravity(17);
            button2.setTextSize(2, 14.0f);
        }
    }

    static void lambda$show$1(Context context, Task task, Runnable runnable, DialogInterface dialogInterface, int i) {
        new Storage(context).setDone(task.id, task.year, !task.done);
        task.done = !task.done;
        if (runnable != null) {
            runnable.run();
        }
    }
}
