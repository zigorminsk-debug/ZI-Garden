package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.Calendar;
import java.util.List;

/** Журнал сада: что и когда было сделано + сроки ожидания после обработок. */
public class JournalActivity extends Activity {
    private Storage store;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("📒 Журнал сада");
        this.store = new Storage(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);
        int cGreen = getResources().getColor(R.color.green_900);

        List<String> rows = this.store.journal(Journal.MAX_ENTRIES);
        root.addView(Ui.text(this,
                rows.isEmpty()
                        ? "Пока пусто. Отмечайте работы выполненными (в задачах, календаре или из уведомления) — они появятся здесь с датой."
                        : "Выполненных работ: " + rows.size() + ". После обработок следим за сроком ожидания препарата.",
                13.0f, cSub, false));

        Button share = new Button(this);
        share.setText("📤 Отправить итоги сезона");
        share.setAllCaps(false);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareSeason();
            }
        });
        root.addView(share);

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

    /** Отчёт «Итоги сезона» (журнал + бюджет закупок) — отправка через мессенджер или почту. */
    private void shareSeason() {
        int year = Dates.today().get(Calendar.YEAR);
        Planner.Budget budget = new Planner(this.store, Weather.fromJson(this.store.weatherCache())).seasonBudget(year);
        String region = Region.detect(this.store.lat(), this.store.lon()).displayName;
        String text = ShareText.seasonSummary(this.store.journal(Journal.MAX_ENTRIES), budget, year, region);
        android.content.Intent send = new android.content.Intent(android.content.Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(android.content.Intent.EXTRA_SUBJECT, "ZI Garden — итоги сезона " + year);
        send.putExtra(android.content.Intent.EXTRA_TEXT, text);
        try {
            startActivity(android.content.Intent.createChooser(send, "Отправить итоги через…"));
        } catch (Exception e) {
            Ui.toast(this, "Не нашлось приложения для отправки");
        }
    }
}
