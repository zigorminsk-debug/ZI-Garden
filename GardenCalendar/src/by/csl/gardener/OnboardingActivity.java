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

/**
 * 👋 Онбординг: знакомство с приложением при первом запуске.
 * Показывается один раз (флаг onboarding_done в Storage); повторно доступен
 * из «О программе» → «Показать введение заново».
 */
public class OnboardingActivity extends Activity {

    /** Один экран знакомства: эмодзи, заголовок, текст, подпись «где найти». */
    static class Slide {
        final String emoji;
        final String title;
        final String body;
        final String where;

        Slide(String emoji, String title, String body, String where) {
            this.emoji = emoji;
            this.title = title;
            this.body = body;
            this.where = where;
        }
    }

    static final Slide[] SLIDES = {
        new Slide("🌱",
                "Добро пожаловать в ваш сад!",
                "ZI Garden — карманный помощник садовода для Беларуси и средней полосы. Приложение подстраивается под то, что растёт именно у вас: выберите свои культуры в разделе «Растения» — и календарь работ, напоминания и подсказки соберутся вокруг них.",
                "👉 Начните с кнопки «Растения» на главном экране"),
        new Slide("📅",
                "Календарь работ с погодой",
                "Задачи на каждую неделю сезона: что и когда сеять, обрезать, подкормить и обработать. Рядом — прогноз погоды для вашего города, чтобы выбрать правильный день. Отмечайте выполненное — сделанное уходит в архив и не мешается под руками.",
                "👉 Кнопки «Задачи» и «Календарь»; напоминания приходят с 8:00 до 21:00"),
        new Slide("🍅",
                "Болезни, вредители и дефициты",
                "233 карточки с фотографиями: как выглядит проблема, чем лечить и как не допустить повторения. Отметьте болезни, которые уже были на участке, — приложение включит кросс-сезонные напоминания о профилактике. Отдельный раздел поможет читать растение по листьям и плодам.",
                "👉 Кнопки «Болезни», «Вредители» и «Дефициты»"),
        new Slide("📸",
                "Фото-дневник и урожай",
                "Фотографируйте каждую культуру по этапам — весна, цветение, урожай — и ведите историю сезонов. Записывайте урожай: приложение посчитает итоги по культурам. Журнал до 200 записей, экспорт в CSV и синхронизация с семьёй между устройствами.",
                "👉 Кнопка «📸 Фото-дневник» в карточке культуры"),
        new Slide("🧺",
                "Схемы, фенология, виджеты",
                "46 культур с индивидуальными схемами посадки и 13 агроприёмов — от обрезки до прививки. Природный календарь фенологии подскажет сроки: от «копеечного листа» берёзы до черёмуховых холодов. Четыре виджета рабочего стола, обновления приходят по воздуху прямо из приложения.",
                "👉 Добавьте виджет долгим нажатием на рабочий стол"),
    };

    /** Показать онбординг, если пользователь видит приложение впервые. */
    public static void showIfNeeded(Context ctx) {
        Storage store = new Storage(ctx);
        if (!store.onboardingDone()) {
            ctx.startActivity(new Intent(ctx, OnboardingActivity.class));
        }
    }

    /** Открыть онбординг принудительно (повторный показ из «О программе»). */
    public static void show(Context ctx) {
        ctx.startActivity(new Intent(ctx, OnboardingActivity.class));
    }

    private int index;
    private TextView dots;
    private LinearLayout slideBox;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTitle("Знакомство с приложением");

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 16.0f), Ui.dp(this, 16.0f), Ui.dp(this, 16.0f), Ui.dp(this, 12.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);
        int cAccent = getResources().getColor(R.color.accent);

        slideBox = Ui.card(this);
        root.addView(slideBox);

        dots = Ui.text(this, "", 16.0f, cSub, false);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, Ui.dp(this, 10.0f), 0, 0);
        root.addView(dots);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(0, Ui.dp(this, 8.0f), 0, 0);

        Button skip = new Button(this);
        skip.setText("Пропустить");
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishIntro();
            }
        });
        LinearLayout.LayoutParams skipLp = new LinearLayout.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        nav.addView(skip, skipLp);

        Button next = new Button(this);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnboardingActivity a = OnboardingActivity.this;
                if (a.index < SLIDES.length - 1) {
                    a.index++;
                    a.renderSlide();
                } else {
                    a.finishIntro();
                }
            }
        });
        LinearLayout.LayoutParams nextLp = new LinearLayout.LayoutParams(0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        nextLp.leftMargin = Ui.dp(this, 8.0f);
        nav.addView(next, nextLp);
        this.nextButton = next;
        root.addView(nav);

        Button pick = new Button(this);
        pick.setText("🌿 Сразу выбрать свои культуры");
        pick.setVisibility(View.GONE);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlantsActivity.show(OnboardingActivity.this);
                finishIntro();
            }
        });
        this.pickButton = pick;
        root.addView(pick);

        TextView hint = Ui.text(this,
                "Всё это всегда под рукой — приложение работает и без интернета",
                12.0f, cSub, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, Ui.dp(this, 10.0f), 0, 0);
        root.addView(hint);

        this.cMain = cMain;
        this.cSub = cSub;
        this.cAccent = cAccent;
        renderSlide();
    }

    private Button nextButton;
    private Button pickButton;
    private int cMain;
    private int cSub;
    private int cAccent;

    /** Перерисовать текущий слайд, точки и кнопку «Далее/Начать». */
    private void renderSlide() {
        Slide s = SLIDES[index];

        slideBox.removeAllViews();
        TextView emoji = Ui.text(this, s.emoji, 40.0f, cMain, false);
        emoji.setGravity(Gravity.CENTER);
        emoji.setPadding(0, Ui.dp(this, 6.0f), 0, Ui.dp(this, 6.0f));
        slideBox.addView(emoji);

        TextView title = Ui.text(this, s.title, 19.0f, cMain, true);
        title.setGravity(Gravity.CENTER);
        slideBox.addView(title);

        TextView body = Ui.text(this, s.body, 14.0f, cMain, false);
        body.setPadding(0, Ui.dp(this, 10.0f), 0, 0);
        slideBox.addView(body);

        TextView where = Ui.text(this, s.where, 13.0f, cAccent, true);
        where.setPadding(0, Ui.dp(this, 10.0f), 0, Ui.dp(this, 6.0f));
        slideBox.addView(where);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SLIDES.length; i++) {
            sb.append(i == index ? "●" : "○");
            if (i < SLIDES.length - 1) {
                sb.append(' ');
            }
        }
        dots.setText(sb.toString());

        nextButton.setText(index < SLIDES.length - 1 ? "Далее →" : "Начать 🌱");
        pickButton.setVisibility(index == SLIDES.length - 1 ? View.VISIBLE : View.GONE);
    }

    /** Завершить знакомство и запомнить, что оно пройдено. */
    private void finishIntro() {
        new Storage(OnboardingActivity.this).setOnboardingDone(true);
        finish();
    }
}
