package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.Calendar;
import java.util.List;

/** Журнал сада: что и когда было сделано + сроки ожидания после обработок. */
public class JournalActivity extends Activity {
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("📒 Журнал сада");

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        scroll.addView(root);
        setContentView(scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);
        int cGreen = getResources().getColor(R.color.green_900);

        List<String> rows = new Storage(this).journal(Journal.MAX_ENTRIES);
        root.addView(Ui.text(this,
                rows.isEmpty()
                        ? "Пока пусто. Отмечайте работы выполненными (в задачах, календаре или из уведомления) — они появятся здесь с датой."
                        : "Выполненных работ: " + rows.size() + ". После обработок следим за сроком ожидания препарата.",
                13.0f, cSub, false));

        long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        for (String raw : rows) {
            String[] f = Journal.decode(raw);
            if (f == null) continue;
            long when = Journal.when(f);
            c.setTimeInMillis(when);
            String date = Dates.fmt(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
                    + ", " + Dates.weekday(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));

            LinearLayout card = Ui.card(this);
            card.addView(Ui.text(this, date + (f[Journal.F_OP].length() > 0
                    ? "  " + Operation.icon(f[Journal.F_OP]) : ""), 12.0f, cSub, false));
            card.addView(Ui.text(this,
                    (f[Journal.F_PLANT].length() > 0 ? f[Journal.F_PLANT] + " — " : "") + f[Journal.F_TITLE],
                    15.0f, cMain, true));
            if (f[Journal.F_MATS].length() > 0) {
                card.addView(Ui.text(this, "💊 " + f[Journal.F_MATS], 12.0f, cSub, false));
            }
            String status = WaitDays.statusLine(f[Journal.F_OP], f[Journal.F_MATS], when, now);
            if (status.length() > 0) {
                card.addView(Ui.text(this, status, 13.0f, cGreen, true));
            }
            root.addView(card);
        }
    }
}
