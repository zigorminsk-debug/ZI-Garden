package by.csl.gardener;

import android.content.Context;
import android.content.res.Configuration;

/** Размер шрифта интерфейса: ступени масштаба и их применение к контексту активности. */
public final class Fonts {

    /** Ступени масштаба шрифта: мелкий / обычный / крупный / очень крупный / огромный / гигантский. */
    public static final float[] SCALES = {0.85f, 1.0f, 1.2f, 1.4f, 1.6f, 1.85f};
    public static final String[] LABELS = {"Мелкий", "Обычный", "Крупный", "Очень крупный", "Огромный", "Гигантский"};

    private Fonts() {
    }

    /** Масштаб шрифта из настроек (индекс SCALES). */
    public static float scale(Context context) {
        int fontSize = new Storage(context).fontSize();
        if (fontSize < 0 || fontSize >= SCALES.length) {
            fontSize = 1;
        }
        return SCALES[fontSize];
    }

    /**
     * Оборачивает контекст выбранным пользователем масштабом шрифта.
     * Вызывается из attachBaseContext каждой активности — тогда весь текст
     * (XML и программный, все размеры в sp) масштабируется единообразно.
     */
    public static Context applyFont(Context context) {
        float scale = scale(context);
        if (scale == 1.0f) {
            return context;
        }
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.fontScale = scale;
        return context.createConfigurationContext(configuration);
    }
}
