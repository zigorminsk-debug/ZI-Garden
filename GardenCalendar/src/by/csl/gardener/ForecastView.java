package by.csl.gardener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Погодная полоса на главном экране: до 7 дней — иконка погоды, столбик
 * температур (синий — холоднее, оранжевый — теплее), столбик осадков в мм
 * и оранжевая точка у дня, когда погода мешает запланированным работам.
 */
public class ForecastView extends View {

    private List<ForecastModel.Cell> cells = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ForecastView(Context context) {
        super(context);
    }

    public ForecastView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Обновить данные полосы; null или пустой список — полоса пустая. */
    public void setCells(List<ForecastModel.Cell> list) {
        this.cells = list == null ? new ArrayList<ForecastModel.Cell>() : list;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthSpec), Ui.dp(getContext(), 150.0f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.cells.isEmpty()) {
            return;
        }
        int n = this.cells.size();
        float w = getWidth();
        float h = getHeight();
        float colW = w / n;
        Context ctx = getContext();
        int cSub = ctx.getResources().getColor(R.color.text_sub);
        int cMain = ctx.getResources().getColor(R.color.text_main);
        int cWarn = ctx.getResources().getColor(R.color.warn_stroke);
        int cPrecip = ctx.getResources().getColor(R.color.accent);
        double[] range = ForecastModel.tempRange(this.cells);
        double precipScale = ForecastModel.precipScale(this.cells);
        float dp2 = Ui.dp(ctx, 2.0f);
        this.paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < n; i++) {
            ForecastModel.Cell c = this.cells.get(i);
            float cx = colW * i + colW / 2.0f;
            // день недели и число
            this.paint.setTextSize(Ui.dp(ctx, 9.0f));
            this.paint.setColor(cSub);
            canvas.drawText(c.weekday, cx, h * 0.075f, this.paint);
            this.paint.setColor(cMain);
            this.paint.setTextSize(Ui.dp(ctx, 10.0f));
            canvas.drawText(String.valueOf(c.day), cx, h * 0.155f, this.paint);
            // иконка погоды
            this.paint.setTextSize(Ui.dp(ctx, 13.0f));
            canvas.drawText(c.icon, cx, h * 0.26f, this.paint);
            // столбик температур: зона 0.30–0.66 высоты
            float zTop = h * 0.30f;
            float zBot = h * 0.66f;
            float fracMax = (float) ((c.tMax - range[0]) / (range[1] - range[0]));
            float fracMin = (float) ((c.tMin - range[0]) / (range[1] - range[0]));
            float barTop = zBot - fracMax * (zBot - zTop);
            float barBot = zBot - fracMin * (zBot - zTop);
            float minBar = Ui.dp(ctx, 3.0f);
            if (barBot - barTop < minBar) {
                barTop = barBot - minBar;
            }
            this.paint.setColor(tempColor((c.tMax + c.tMin) / 2.0d, range[0], range[1]));
            canvas.drawRoundRect(cx - Ui.dp(ctx, 5.0f), barTop, cx + Ui.dp(ctx, 5.0f), barBot,
                    Ui.dp(ctx, 3.0f), Ui.dp(ctx, 3.0f), this.paint);
            this.paint.setColor(cMain);
            this.paint.setTextSize(Ui.dp(ctx, 8.0f));
            canvas.drawText(String.format(Locale.US, "%.0f°", Double.valueOf(c.tMax)), cx, barTop - dp2, this.paint);
            canvas.drawText(String.format(Locale.US, "%.0f°", Double.valueOf(c.tMin)), cx, barBot + Ui.dp(ctx, 8.0f), this.paint);
            // осадки: зона 0.74–0.96 высоты
            float pTop0 = h * 0.74f;
            float pBot0 = h * 0.96f;
            if (c.precipMm >= 0.5d) {
                float pw = (float) Math.min(1.0d, c.precipMm / precipScale);
                float pTop = pBot0 - pw * (pBot0 - pTop0);
                this.paint.setColor(cPrecip);
                canvas.drawRoundRect(cx - Ui.dp(ctx, 5.0f), pTop, cx + Ui.dp(ctx, 5.0f), pBot0,
                        Ui.dp(ctx, 2.0f), Ui.dp(ctx, 2.0f), this.paint);
                this.paint.setTextSize(Ui.dp(ctx, 7.0f));
                canvas.drawText(String.format(Locale.US, "%.0f", Double.valueOf(c.precipMm)), cx, pTop - dp2, this.paint);
            } else {
                this.paint.setColor(cSub);
                this.paint.setTextSize(Ui.dp(ctx, 7.0f));
                canvas.drawText("—", cx, pBot0, this.paint);
            }
            // оранжевая точка: в этот день погода мешает работам
            if (c.warn) {
                this.paint.setColor(cWarn);
                canvas.drawCircle(colW * (i + 1) - Ui.dp(ctx, 6.0f), h * 0.075f - Ui.dp(ctx, 5.0f), Ui.dp(ctx, 2.5f), this.paint);
            }
        }
    }

    /** Цвет столбика: от синего #64B5F6 (холод) к оранжевому #FFB74D (тепло) по шкале недели. */
    private int tempColor(double t, double lo, double hi) {
        float f = (float) ((t - lo) / Math.max(1.0d, hi - lo));
        if (f < 0.0f) {
            f = 0.0f;
        }
        if (f > 1.0f) {
            f = 1.0f;
        }
        int r = (int) (100.0f + f * 155.0f);
        int g = (int) (181.0f + f * 2.0f);
        int b = (int) (246.0f - f * 169.0f);
        return Color.argb(255, r, g, b);
    }
}
