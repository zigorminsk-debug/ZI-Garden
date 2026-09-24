package by.csl.gardener;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Calendar;

/**
 * 📅 Календарь посещения участка: отметить дни, когда вы на месте.
 * В дачном режиме планировщик переносит работы к ближайшему отмеченному дню
 * (если ничего не отмечено — к субботам и воскресеньям).
 */
public class VisitCalendarActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    private Storage store;
    private int showYear;
    private int showMonth; // 1..12
    private LinearLayout grid;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.store = new Storage(this);
        Calendar today = Dates.today();
        this.showYear = today.get(Calendar.YEAR);
        this.showMonth = today.get(Calendar.MONTH) + 1;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 8.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        Ui.setContent(this, root);

        list.addView(Ui.text(this,
                "Отметьте дни, когда вы будете на участке. В дачном режиме все работы "
                        + "переносятся к ближайшему отмеченному дню — к приезду всё будет выполнимо. "
                        + "Если ничего не отмечено, план собирается к субботам и воскресеньям. "
                        + "Отпуск отмечайте диапазоном дней.",
                13.0f, getResources().getColor(R.color.text_sub), false));

        // ‹ Месяц ›
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        Button prev = new Button(this);
        prev.setText("◀");
        Button next = new Button(this);
        next.setText("▶");
        final TextView title = Ui.text(this, "", 17.0f, getResources().getColor(R.color.green_900), true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(Ui.dp(this, 8.0f), 0, Ui.dp(this, 8.0f), 0);
        nav.addView(prev, new LinearLayout.LayoutParams(0, -2, 1.0f));
        nav.addView(title, new LinearLayout.LayoutParams(0, -2, 2.6f));
        nav.addView(next, new LinearLayout.LayoutParams(0, -2, 1.0f));
        list.addView(nav);

        this.grid = new LinearLayout(this);
        this.grid.setOrientation(LinearLayout.VERTICAL);
        list.addView(this.grid);

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.VERTICAL);
        Button markWeekends = new Button(this);
        markWeekends.setText("✅ Отметить выходные этого месяца");
        Button clearMonth = new Button(this);
        clearMonth.setText("🧹 Снять отметки этого месяца");
        quick.addView(markWeekends);
        quick.addView(clearMonth);
        list.addView(quick);

        final TextView counter = Ui.text(this, "", 13.0f, getResources().getColor(R.color.text_sub), false);
        counter.setPadding(0, Ui.dp(this, 8.0f), 0, 0);
        list.addView(counter);

        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VisitCalendarActivity.this.showMonth--;
                if (VisitCalendarActivity.this.showMonth < 1) {
                    VisitCalendarActivity.this.showMonth = 12;
                    VisitCalendarActivity.this.showYear--;
                }
                VisitCalendarActivity.this.render(title, counter);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VisitCalendarActivity.this.showMonth++;
                if (VisitCalendarActivity.this.showMonth > 12) {
                    VisitCalendarActivity.this.showMonth = 1;
                    VisitCalendarActivity.this.showYear++;
                }
                VisitCalendarActivity.this.render(title, counter);
            }
        });
        markWeekends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VisitCalendarActivity v = VisitCalendarActivity.this;
                Calendar c = Dates.at(v.showYear, v.showMonth, 1);
                int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                for (int d = 1; d <= daysInMonth; d++) {
                    Calendar day = Dates.at(v.showYear, v.showMonth, d);
                    int dw = day.get(Calendar.DAY_OF_WEEK);
                    if (dw == Calendar.SATURDAY || dw == Calendar.SUNDAY) {
                        v.store.setVisitDay(v.showYear, v.showMonth, d, true);
                    }
                }
                v.render(title, counter);
                Ui.toast(v, "Выходные отмечены — план соберётся к ним");
            }
        });
        clearMonth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VisitCalendarActivity v = VisitCalendarActivity.this;
                Calendar c = Dates.at(v.showYear, v.showMonth, 1);
                int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                for (int d = 1; d <= daysInMonth; d++) {
                    v.store.setVisitDay(v.showYear, v.showMonth, d, false);
                }
                v.render(title, counter);
                Ui.toast(v, "Отметки месяца сняты");
            }
        });

        render(title, counter);
    }

    /** Отрисовка сетки месяца: дни недели (пн…вс) и дни-кнопки с отметками. */
    private void render(TextView title, TextView counter) {
        title.setText(Dates.MONTHS_NOM[this.showMonth - 1] + " " + this.showYear);
        this.grid.removeAllViews();

        // заголовок дней недели, неделя с понедельника
        String[] wd = {"пн", "вт", "ср", "чт", "пт", "сб", "вс"};
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        for (String s : wd) {
            TextView t = Ui.text(this, s, 12.0f, getResources().getColor(R.color.text_sub), false);
            t.setGravity(Gravity.CENTER);
            head.addView(t, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }
        this.grid.addView(head);

        Calendar first = Dates.at(this.showYear, this.showMonth, 1);
        int daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH);
        int lead = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7; // понедельник = 0

        LinearLayout week = new LinearLayout(this);
        week.setOrientation(LinearLayout.HORIZONTAL);
        this.grid.addView(week);
        for (int i = 0; i < lead; i++) {
            TextView empty = Ui.text(this, "", 14.0f, getResources().getColor(R.color.text_sub), false);
            empty.setGravity(Gravity.CENTER);
            week.addView(empty, new LinearLayout.LayoutParams(0, Ui.dp(this, 42.0f), 1.0f));
        }
        for (int d = 1; d <= daysInMonth; d++) {
            if (week.getChildCount() == 7) {
                week = new LinearLayout(this);
                week.setOrientation(LinearLayout.HORIZONTAL);
                this.grid.addView(week);
            }
            final int day = d;
            boolean marked = this.store.isVisitDay(this.showYear, this.showMonth, d);
            TextView cell = Ui.text(this, String.valueOf(d), 15.0f,
                    getResources().getColor(marked ? R.color.green_900 : R.color.text_main), true);
            cell.setGravity(Gravity.CENTER);
            cell.setBackgroundResource(marked ? R.drawable.ok_bg : R.drawable.card_bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 42.0f), 1.0f);
            lp.setMargins(Ui.dp(this, 1.0f), Ui.dp(this, 1.0f), Ui.dp(this, 1.0f), Ui.dp(this, 1.0f));
            cell.setLayoutParams(lp);
            cell.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    boolean now = !VisitCalendarActivity.this.store.isVisitDay(
                            VisitCalendarActivity.this.showYear, VisitCalendarActivity.this.showMonth, day);
                    VisitCalendarActivity.this.store.setVisitDay(
                            VisitCalendarActivity.this.showYear, VisitCalendarActivity.this.showMonth, day, now);
                    VisitCalendarActivity.this.render(title, counter);
                }
            });
            week.addView(cell);
        }
        while (week.getChildCount() < 7 && week.getChildCount() > 0) {
            TextView empty = Ui.text(this, "", 14.0f, getResources().getColor(R.color.text_sub), false);
            empty.setGravity(Gravity.CENTER);
            week.addView(empty, new LinearLayout.LayoutParams(0, Ui.dp(this, 42.0f), 1.0f));
        }

        int total = this.store.visitDays().size();
        counter.setText(total == 0
                ? "Отмеченных дней нет: в дачном режиме план идёт на сб/вс."
                : "Всего отмечено дней: " + total + ". Работа перенесётся к ближайшему отмеченному.");
    }
}
