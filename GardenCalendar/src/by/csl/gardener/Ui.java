package by.csl.gardener;

import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import by.csl.gardener.Task;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

public final class Ui {
    private Ui() {
    }

    /** Размер шрифта из настроек применён к контексту активности (логика — в Fonts). */
    public static Context applyFont(Context context) {
        return Fonts.applyFont(applyTheme(context));
    }

    /** Тема оформления: 0 — как в системе, 1 — светлая, 2 — тёмная. Применяется раньше шрифта. */
    public static Context applyTheme(Context context) {
        int mode = new Storage(context).themeMode();
        if (mode == 0) {
            return context;
        }
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        int night = mode == 2 ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        if ((configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == night) {
            return context;
        }
        configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | night;
        return context.createConfigurationContext(configuration);
    }

    public static int dp(Context context, float f) {
        return (int) TypedValue.applyDimension(1, f, context.getResources().getDisplayMetrics());
    }

    public static TextView text(Context context, String str, float f, int i, boolean z) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextSize(2, f);
        textView.setTextColor(i);
        if (z) {
            textView.setTypeface(null, 1);
        }
        return textView;
    }

    public static LinearLayout card(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundResource(R.drawable.card_bg);
        linearLayout.setPadding(dp(context, 12.0f), dp(context, 12.0f), dp(context, 12.0f), dp(context, 12.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.bottomMargin = dp(context, 8.0f);
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }

    /** Прозрачность водяного знака «дачный участок» на фоне экранов (вариант A — лёгкая дымка). */
    public static final float WM_ALPHA = 0.12f;

    /** setContentView с водяным знаком баннера на фоне всего экрана. */
    public static void setContent(android.app.Activity activity, View content) {
        android.widget.FrameLayout wrap = new android.widget.FrameLayout(activity);
        android.widget.ImageView wm = new android.widget.ImageView(activity);
        wm.setImageResource(R.drawable.banner_garden);
        wm.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        wm.setAlpha(WM_ALPHA);
        wrap.addView(wm, new android.widget.FrameLayout.LayoutParams(-1, -1));
        wrap.addView(content, new android.widget.FrameLayout.LayoutParams(-1, -1));
        activity.setContentView(wrap);
    }

    /** То же для экранов с XML-разметкой. */
    public static void setContent(android.app.Activity activity, int layoutRes) {
        setContent(activity, activity.getLayoutInflater().inflate(layoutRes, (android.view.ViewGroup) null));
    }

    public static View taskCard(final Context context, final Task task, final Runnable runnable) {
        LinearLayout card = card(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        CheckBox checkBox = new CheckBox(context);
        checkBox.setChecked(task.done);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                Ui.lambda$taskCard$0(context, task, runnable, compoundButton, z);
            }
        });
        linearLayout.addView(checkBox);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(1);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
        Plant cardPlant = Plant.byId(task.plantId);
        TextView text = text(context, (cardPlant != null && cardPlant.iconRes != 0 ? "" : plantIcon(task) + " ") + task.plantName, 20.0f, context.getResources().getColor(R.color.green_900), true);
        if (cardPlant != null && cardPlant.iconRes != 0) {
            text.setCompoundDrawablesWithIntrinsicBounds(cardPlant.iconRes, 0, 0, 0);
            text.setCompoundDrawablePadding(dp(context, 6.0f));
        }
        text.setAlpha(task.done ? 0.45f : 1.0f);
        linearLayout2.addView(text);
        TextView text2 = text(context, Operation.icon(task.op) + " " + task.title, 14.0f, context.getResources().getColor(R.color.text_main), false);
        text2.setAlpha(task.done ? 0.45f : 1.0f);
        linearLayout2.addView(text2);
        StringBuilder sb = new StringBuilder();
        sb.append(Operation.label(task.op));
        sb.append(" · ");
        sb.append(task.window);
        sb.append(task.priority == 3 ? " · срочно" : "");
        TextView text3 = text(context, sb.toString(), 12.0f, context.getResources().getColor(R.color.text_sub), false);
        text3.setAlpha(task.done ? 0.45f : 1.0f);
        linearLayout2.addView(text3);
        if (Scheme.forTask(task) != 0) {
            linearLayout2.addView(text(context, "📐 Есть схема обрезки — нажмите на карточку", 12.0f, context.getResources().getColor(R.color.accent), false));
        }
        Iterator<Task.Item> it = task.items.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (!it.next().alternative) {
                i++;
            }
        }
        if (i > 0) {
            linearLayout2.addView(text(context, "Материалы: " + i + " · " + money(task.totalCost(), task.currency), 12.0f, context.getResources().getColor(R.color.accent), false));
        }
        linearLayout.addView(linearLayout2);
        card.addView(linearLayout);
        TextView weatherBadge = weatherBadge(context, task);
        if (weatherBadge != null) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = dp(context, 6.0f);
            weatherBadge.setLayoutParams(layoutParams);
            card.addView(weatherBadge);
        }
        card.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                TaskDialog.show(context, task, runnable);
            }
        });
        return card;
    }

    static void lambda$taskCard$0(Context context, Task task, Runnable runnable, CompoundButton compoundButton, boolean z) {
        new Storage(context).setDone(task.id, task.year, z);
        if (z) {
            // Журнал сада: фиксируем выполненную работу (надо сроки ожидания после обработок следить)
            new Storage(context).addJournal(task.op, task.plantName, task.title, WaitDays.matsCsv(task));
            // Урожай: после «Сбора урожая» предлагаем записать, сколько собрали
            if (Operation.HARVEST.equals(task.op) && task.plantId != null
                    && (context instanceof android.app.Activity)) {
                HarvestDialog.ask((android.app.Activity) context, task.year, task.plantId, task.plantName, task.title);
            }
        }
        task.done = z;
        if (runnable != null) {
            runnable.run();
        }
    }

    private static String plantIcon(Task task) {
        Plant byId = Plant.byId(task.plantId);
        return byId == null ? "🌱" : byId.icon;
    }

    public static TextView weatherBadge(Context context, Task task) {
        String str;
        int i;
        int i2;
        if (task.weatherState == 4) {
            return null;
        }
        TextView textView = new TextView(context);
        textView.setPadding(dp(context, 8.0f), dp(context, 6.0f), dp(context, 8.0f), dp(context, 6.0f));
        textView.setTextSize(2, 12.0f);
        int i3 = task.weatherState;
        if (i3 != 0) {
            i2 = context.getResources().getColor(R.color.warn_text);
            i = R.drawable.warn_bg;
            if (i3 == 1) {
                str = "🌧 " + task.weatherNote;
            } else if (i3 == 2) {
                str = "❄️ " + task.weatherNote;
            } else {
                str = "🔥 " + task.weatherNote;
            }
        } else {
            str = "✅ " + task.weatherNote;
            i = R.drawable.ok_bg;
            i2 = context.getResources().getColor(R.color.green_900);
        }
        if (task.weatherState != 0 && task.suggestDayOffset > 0) {
            Calendar plusDays = Dates.plusDays(Dates.at(task.year, task.month, task.day), task.suggestDayOffset);
            str = str + ". Подходящий день: " + Dates.fmtShort(plusDays.get(1), plusDays.get(2) + 1, plusDays.get(5));
        }
        textView.setText(str);
        textView.setBackgroundResource(i);
        textView.setTextColor(i2);
        return textView;
    }

    public static String money(double d) {
        return money(d, "BYN");
    }

    public static String money(double d, String str) {
        return d <= 0.0d ? "бесплатно" : String.format(Locale.US, "%.2f %s", Double.valueOf(d), str);
    }

    public static void section(LinearLayout linearLayout, Context context, String str) {
        TextView text = text(context, str, 16.0f, context.getResources().getColor(R.color.green_900), true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = dp(context, 6.0f);
        layoutParams.bottomMargin = dp(context, 4.0f);
        text.setLayoutParams(layoutParams);
        linearLayout.addView(text);
    }

    public static View divider(Context context) {
        View view = new View(context);
        view.setBackgroundColor(context.getResources().getColor(R.color.card_stroke));
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.max(1, dp(context, 1.0f))));
        return view;
    }

    public static void toast(Context context, String str) {
        Toast.makeText(context, str, 1).show();
    }

    /** Полноэкранный просмотр фото (тап — закрыть). Используется для снимков симптомов болезней. */
    public static void zoomPhoto(android.app.Activity activity, int resId) {
        final android.app.Dialog dialog = new android.app.Dialog(activity,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        android.widget.ImageView image = new android.widget.ImageView(activity);
        image.setImageResource(resId);
        image.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        image.setBackgroundColor(android.graphics.Color.BLACK);
        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setContentView(image);
        dialog.show();
    }
}
