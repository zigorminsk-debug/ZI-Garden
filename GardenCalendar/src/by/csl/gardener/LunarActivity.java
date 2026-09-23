package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Calendar;

/**
 * 🌙 Лунный календарь: сетка месяца с фазами Луны и советами по дням.
 * Растущая луна — посевы и подкормки надземных культур, убывающая — корнеплоды
 * и обрезка, новолуние и полнолуние — дни без посева. Расчёт — в {@link Moon}.
 * Открывается плиткой «Луна» на главном экране.
 */
public class LunarActivity extends Activity {

    private static final String[] WEEKDAYS = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

    private int year;
    private int month; // 1..12
    private int selectedDay;
    private TextView monthTitle;
    private LinearLayout grid;
    private TextView guide;

    public static void show(Context ctx) {
        ctx.startActivity(new Intent(ctx, LunarActivity.class));
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar now = Calendar.getInstance();
        this.year = now.get(Calendar.YEAR);
        this.month = now.get(Calendar.MONTH) + 1;
        this.selectedDay = now.get(Calendar.DAY_OF_MONTH);
        buildUi();
        render();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));

        // ◀ Сентябрь 2026 ▶
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        Button prev = new Button(this);
        prev.setText("◀");
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LunarActivity.this.shift(-1);
            }
        });
        header.addView(prev, new LinearLayout.LayoutParams(-2, -2));
        this.monthTitle = Ui.text(this, "", 18.0f, getResources().getColor(R.color.green_900), true);
        this.monthTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        header.addView(this.monthTitle, titleLp);
        Button next = new Button(this);
        next.setText("▶");
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LunarActivity.this.shift(1);
            }
        });
        header.addView(next, new LinearLayout.LayoutParams(-2, -2));
        root.addView(header);

        // шапка дней недели
        LinearLayout weekRow = new LinearLayout(this);
        weekRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 7; i++) {
            TextView wd = Ui.text(this, WEEKDAYS[i], 11.0f, getResources().getColor(R.color.text_sub), true);
            wd.setGravity(Gravity.CENTER);
            weekRow.addView(wd, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }
        weekRow.setPadding(0, Ui.dp(this, 8.0f), 0, Ui.dp(this, 2.0f));
        root.addView(weekRow);

        // карточка сетки месяца
        LinearLayout card = Ui.card(this);
        this.grid = new LinearLayout(this);
        this.grid.setOrientation(LinearLayout.VERTICAL);
        card.addView(this.grid);
        root.addView(card);

        // совет выбранного дня
        this.guide = Ui.text(this, "", 15.0f, getResources().getColor(R.color.text_main), false);
        this.guide.setLineSpacing(Ui.dp(this, 2.0f), 1.0f);
        this.guide.setPadding(Ui.dp(this, 4.0f), Ui.dp(this, 4.0f), Ui.dp(this, 4.0f), 0);
        root.addView(this.guide);

        // краткая легенда традиций
        TextView legend = Ui.text(this,
                "По лунным традициям: 🌒 растущая — посев и подкормка надземных культур; "
                        + "🌖 убывающая — корнеплоды, луковичные и обрезка; "
                        + "🌑 новолуние и 🌕 полнолуние — без посева и пересадки.",
                12.0f, getResources().getColor(R.color.text_sub), false);
        legend.setLineSpacing(Ui.dp(this, 2.0f), 1.0f);
        legend.setPadding(Ui.dp(this, 4.0f), Ui.dp(this, 8.0f), Ui.dp(this, 4.0f), 0);
        root.addView(legend);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        Ui.setContent(this, scroll);
    }

    /** Сдвиг месяца: −1/＋1, с переходом через год. */
    private void shift(int delta) {
        this.month += delta;
        if (this.month < 1) {
            this.month = 12;
            this.year--;
        } else if (this.month > 12) {
            this.month = 1;
            this.year++;
        }
        this.selectedDay = 1;
        render();
    }

    /** Перерисовать сетку месяца и совет выбранного дня. */
    private void render() {
        this.monthTitle.setText(Dates.MONTHS[this.month - 1] + " " + this.year);
        this.grid.removeAllViews();
        Calendar first = Calendar.getInstance();
        first.set(this.year, this.month - 1, 1);
        int daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH);
        int lead = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Пн = 0
        Calendar now = Calendar.getInstance();
        boolean isCurrentMonth = now.get(Calendar.YEAR) == this.year
                && now.get(Calendar.MONTH) + 1 == this.month;
        if (!isCurrentMonth) {
            this.selectedDay = Math.min(this.selectedDay, daysInMonth);
        }
        LinearLayout row = newRow();
        for (int i = 0; i < lead; i++) {
            addEmptyCell(row);
        }
        for (int day = 1; day <= daysInMonth; day++) {
            if (((lead + day - 1) % 7) == 0 && day > 1) {
                row = newRow();
            }
            final int d = day;
            boolean today = isCurrentMonth && now.get(Calendar.DAY_OF_MONTH) == day;
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, Ui.dp(this, 4.0f), 0, Ui.dp(this, 4.0f));
            TextView moon = Ui.text(this, Moon.emoji(this.year, this.month, day), 16.0f,
                    getResources().getColor(R.color.text_main), false);
            moon.setGravity(Gravity.CENTER);
            cell.addView(moon);
            TextView num = Ui.text(this, String.valueOf(day), 13.0f,
                    getResources().getColor(today ? R.color.green_900 : R.color.text_main), today);
            num.setGravity(Gravity.CENTER);
            cell.addView(num);
            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LunarActivity.this.selectedDay = d;
                    LunarActivity.this.updateGuide();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1.0f);
            lp.setMargins(Ui.dp(this, 1.0f), Ui.dp(this, 1.0f), Ui.dp(this, 1.0f), Ui.dp(this, 1.0f));
            row.addView(cell, lp);
        }
        updateGuide();
    }

    private LinearLayout newRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        this.grid.addView(row);
        return row;
    }

    private void addEmptyCell(LinearLayout row) {
        View empty = new View(this);
        row.addView(empty, new LinearLayout.LayoutParams(0, Ui.dp(this, 36.0f), 1.0f));
    }

    /** Совет лунных традиций на выбранный день. */
    private void updateGuide() {
        Calendar c = Calendar.getInstance();
        c.set(this.year, this.month - 1, this.selectedDay);
        this.guide.setText(Dates.fmt(this.year, this.month, this.selectedDay) + " · " + Moon.guide(c));
    }
}
