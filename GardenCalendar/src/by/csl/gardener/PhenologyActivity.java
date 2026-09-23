package by.csl.gardener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 🌸 Фенология сада: природный календарь — «когда что делать» подсказывают сами растения.
 */
public class PhenologyActivity extends Activity {

    private static final String[] SEASONS = {"Весна", "Лето", "Осень", "Зима"};

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    public static void show(Context ctx) {
        ctx.startActivity(new android.content.Intent(ctx, PhenologyActivity.class));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("Фенология сада");

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);

        LinearLayout intro = Ui.card(this);
        intro.addView(Ui.text(this, "🌸 Календарь природы", 15.0f, cMain, true));
        TextView introText = Ui.text(this, PhenologyGuide.INTRO, 13.0f, cMain, false);
        introText.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
        intro.addView(introText);
        root.addView(intro);

        // ── Сейчас в природе: сезон по текущей дате ──
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String season = PhenologyGuide.seasonFor(cal.get(java.util.Calendar.MONTH) + 1);
        int seasonIdx = 0;
        for (int i = 0; i < SEASONS.length; i++) {
            if (SEASONS[i].equals(season)) {
                seasonIdx = i;
                break;
            }
        }

        LinearLayout now = Ui.card(this);
        now.addView(Ui.text(this, PhenologyGuide.seasonEmoji(season) + " Сейчас в природе: " + season.toLowerCase(),
                16.0f, cMain, true));
        TextView nowText = Ui.text(this, PhenologyGuide.seasonSummary(season), 13.0f, cMain, false);
        nowText.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
        now.addView(nowText);

        StringBuilder sb = new StringBuilder();
        for (PhenologyGuide.Sign s : PhenologyGuide.bySeason(season)) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(s.emoji).append(' ').append(s.title).append(" — ").append(s.period);
        }
        TextView nowList = Ui.text(this, sb.toString(), 13.0f, cSub, false);
        nowList.setPadding(0, Ui.dp(this, 8.0f), 0, 0);
        now.addView(nowList);
        root.addView(now);

        // ── Все сезоны ──
        for (int i = 0; i < SEASONS.length; i++) {
            String s = SEASONS[i];
            Ui.section(root, this, PhenologyGuide.seasonEmoji(s) + " " + s);
            for (PhenologyGuide.Sign sign : PhenologyGuide.bySeason(s)) {
                LinearLayout card = Ui.card(this);
                card.addView(Ui.text(this, sign.emoji + " " + sign.title, 15.0f, cMain, true));
                TextView period = Ui.text(this, "🕒 " + sign.period, 12.0f, cSub, false);
                period.setPadding(0, Ui.dp(this, 3.0f), 0, 0);
                card.addView(period);
                TextView signal = Ui.text(this, sign.signal, 13.0f, cMain, false);
                signal.setPadding(0, Ui.dp(this, 6.0f), 0, 0);
                card.addView(signal);
                TextView works = Ui.text(this, "👉 " + sign.works, 13.0f, cMain, false);
                works.setPadding(0, Ui.dp(this, 6.0f), 0, 0);
                card.addView(works);
                root.addView(card);
            }
        }
    }
}
