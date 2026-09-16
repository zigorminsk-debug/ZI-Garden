package by.csl.gardener;

import android.app.Activity;
import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import by.csl.gardener.Weather;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    private LinearLayout list;
    private Storage store;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.store = new Storage(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setPadding(Ui.dp(this, 8.0f), Ui.dp(this, 8.0f), Ui.dp(this, 8.0f), Ui.dp(this, 8.0f));
        horizontalScrollView.addView(linearLayout2);
        linearLayout.addView(horizontalScrollView);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout3 = new LinearLayout(this);
        this.list = linearLayout3;
        linearLayout3.setOrientation(1);
        this.list.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 4.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        scrollView.addView(this.list);
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        setContentView(linearLayout);
        Weather fromJson = Weather.fromJson(this.store.weatherCache());
        renderStrip(linearLayout2, fromJson);
        render(fromJson);
    }

    private void renderStrip(LinearLayout linearLayout, Weather weather) {
        Calendar calendar = Dates.today();
        int i = 0;
        while (i < 21) {
            Calendar plusDays = Dates.plusDays(calendar, i);
            int i2 = plusDays.get(1);
            int i3 = plusDays.get(2) + 1;
            int i4 = plusDays.get(5);
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(1);
            linearLayout2.setGravity(17);
            linearLayout2.setBackgroundResource(i == 0 ? R.drawable.ok_bg : R.drawable.card_bg);
            linearLayout2.setPadding(Ui.dp(this, 8.0f), Ui.dp(this, 6.0f), Ui.dp(this, 8.0f), Ui.dp(this, 6.0f));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Ui.dp(this, 58.0f), -2);
            layoutParams.rightMargin = Ui.dp(this, 6.0f);
            linearLayout2.setLayoutParams(layoutParams);
            linearLayout2.addView(Ui.text(this, Dates.weekdayShort(i2, i3, i4).toUpperCase(Locale.US), 11.0f, -10721696, false));
            TextView text = Ui.text(this, String.valueOf(i4), 18.0f, -14983648, true);
            text.setGravity(17);
            linearLayout2.addView(text);
            linearLayout2.addView(Ui.text(this, Dates.MONTHS[i3 - 1].substring(0, 3), 10.0f, -10721696, false));
            Weather.Day dayFor = weather == null ? null : weather.dayFor(i2, i3, i4);
            if (dayFor != null) {
                TextView text2 = Ui.text(this, dayFor.icon(), 16.0f, -14670049, false);
                text2.setGravity(17);
                linearLayout2.addView(text2);
                TextView text3 = Ui.text(this, String.format(Locale.US, "%.0f…%.0f", Double.valueOf(dayFor.tMin), Double.valueOf(dayFor.tMax)), 10.0f, -10721696, false);
                text3.setGravity(17);
                linearLayout2.addView(text3);
            }
            linearLayout.addView(linearLayout2);
            i++;
        }
    }

    private void render(Weather weather) {
        Calendar calendar;
        this.list.removeAllViews();
        List<Task> tasks = new Planner(this.store, weather).tasks(21);
        Calendar calendar2 = Dates.today();
        int i = 0;
        int i2 = 0;
        for (int i3 = 21; i < i3; i3 = 21) {
            Calendar plusDays = Dates.plusDays(calendar2, i);
            boolean z = true;
            int i4 = plusDays.get(1);
            int i5 = plusDays.get(2) + 1;
            int i6 = plusDays.get(5);
            Weather.Day dayFor = weather == null ? null : weather.dayFor(i4, i5, i6);
            Iterator<Task> it = tasks.iterator();
            while (true) {
                if (!it.hasNext()) {
                    z = false;
                    break;
                }
                Task next = it.next();
                if (next.year == i4 && next.month == i5 && next.day == i6) {
                    break;
                } else {
                    calendar2 = calendar2;
                }
            }
            if (z) {
                StringBuilder sb = new StringBuilder();
                sb.append(i == 0 ? "Сегодня · " : "");
                sb.append(Dates.fmt(i4, i5, i6));
                sb.append(", ");
                sb.append(Dates.weekday(i4, i5, i6));
                Ui.section(this.list, this, sb.toString());
                if (dayFor != null) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(dayFor.icon());
                    sb2.append(" ");
                    sb2.append(dayFor.summary());
                    calendar = calendar2;
                    sb2.append(String.format(Locale.US, " · осадки %.1f мм (%.0f%%)", Double.valueOf(dayFor.precipMm), Double.valueOf(dayFor.precipProb)));
                    TextView text = Ui.text(this, sb2.toString(), 13.0f, -15374912, false);
                    text.setPadding(0, 0, 0, Ui.dp(this, 4.0f));
                    this.list.addView(text);
                } else {
                    calendar = calendar2;
                }
                for (Task task : tasks) {
                    if (task.year == i4 && task.month == i5 && task.day == i6) {
                        this.list.addView(Ui.taskCard(this, task, new Runnable() {
                            public final void run() {
                                CalendarActivity.this.m2lambda$render$0$bycslgardenerCalendarActivity();
                            }
                        }));
                        i2++;
                    }
                }
            } else {
                calendar = calendar2;
            }
            i++;
            calendar2 = calendar;
        }
        if (i2 == 0) {
            this.list.addView(Ui.text(this, "Работ на ближайшие 3 недели не найдено. Проверьте, отмечены ли растения.", 14.0f, getResources().getColor(R.color.text_sub), false));
        }
    }

    void m2lambda$render$0$bycslgardenerCalendarActivity() {
        render(Weather.fromJson(this.store.weatherCache()));
    }
}
