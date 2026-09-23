package by.csl.gardener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Фото с жестами: щипок — масштаб (до 8× от вписывания), перетасивание одним
 * пальцем — панорамирование, двойной тап — приблизить/сбросить, одиночный
 * тап — закрыть (задаётся слушателем {@link #setOnTap}).
 * Используется полноэкранным просмотром {@link Ui#zoomPhoto}.
 */
public class ZoomView extends ImageView {

    /** Максимальное увеличение относительно вписывания в экран. */
    private static final float MAX_ZOOM = 8.0f;
    /** Приближение по двойному тапу. */
    private static final float TAP_ZOOM = 2.5f;

    private final Matrix matrix = new Matrix();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;

    private float fitScale = 1.0f; // масштаб вписывания картинки в вид
    private float zoom = 1.0f;     // 1..MAX_ZOOM
    private float panX = 0.0f;     // сдвиг от центра, в координатах вида
    private float panY = 0.0f;
    private float imgW = 0.0f;     // размер картинки в пикселях
    private float imgH = 0.0f;
    private boolean laidOut = false;
    private Runnable onTap;

    public ZoomView(Context context) {
        super(context);
        super.setScaleType(ScaleType.MATRIX);
        this.scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float newZoom = ZoomView.this.zoom * detector.getScaleFactor();
                if (newZoom < 1.0f) newZoom = 1.0f;
                if (newZoom > MAX_ZOOM) newZoom = MAX_ZOOM;
                // приближаем вокруг точки щипка: точка картинки под пальцами неподвижна
                float fx = detector.getFocusX();
                float fy = detector.getFocusY();
                float s = scale();
                float u = (fx - imageLeft()) / s; // координаты картинки
                float v = (fy - imageTop()) / s;
                ZoomView.this.zoom = newZoom;
                float s2 = scale();
                ZoomView.this.panX = fx - centerX() - u * s2;
                ZoomView.this.panY = fy - centerY() - v * s2;
                applyMatrix();
                return true;
            }
        });
        this.gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (ZoomView.this.scaleDetector.isInProgress()) {
                    return false; // щипок сам двигает картинку
                }
                ZoomView.this.panX -= distanceX;
                ZoomView.this.panY -= distanceY;
                applyMatrix();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (ZoomView.this.zoom > 1.01f) {
                    reset();
                } else {
                    float fx = e.getX();
                    float fy = e.getY();
                    float s = scale();
                    float u = (fx - imageLeft()) / s;
                    float v = (fy - imageTop()) / s;
                    ZoomView.this.zoom = Math.min(TAP_ZOOM, MAX_ZOOM);
                    float s2 = scale();
                    ZoomView.this.panX = fx - centerX() - u * s2;
                    ZoomView.this.panY = fy - centerY() - v * s2;
                }
                applyMatrix();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (ZoomView.this.onTap != null) {
                    ZoomView.this.onTap.run();
                    return true;
                }
                return false;
            }
        });
    }

    /** Одиночный тап — закрыть просмотр. */
    public void setOnTap(Runnable listener) {
        this.onTap = listener;
    }

    /** Сброс к вписыванию. */
    public void reset() {
        this.zoom = 1.0f;
        this.panX = 0.0f;
        this.panY = 0.0f;
        applyMatrix();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        reset();
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        reset();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        this.laidOut = w > 0 && h > 0;
        applyMatrix();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.scaleDetector.onTouchEvent(event);
        this.gestureDetector.onTouchEvent(event);
        return true;
    }

    /** Итоговый масштаб картинки. */
    private float scale() {
        return this.fitScale * this.zoom;
    }

    /** Горизонталь центра вида (для позиционирования от центра). */
    private float centerX() {
        return (getWidth() - this.imgW * scale()) / 2.0f;
    }

    /** Вертикаль центра вида. */
    private float centerY() {
        return (getHeight() - this.imgH * scale()) / 2.0f;
    }

    /** Левый край картинки в координатах вида. */
    private float imageLeft() {
        return centerX() + this.panX;
    }

    /** Верхний край картинки в координатах вида. */
    private float imageTop() {
        return centerY() + this.panY;
    }

    /** Пересчитать вписывание, применить матрицу и удержать картинку на экране. */
    private void applyMatrix() {
        if (this.laidOut) {
            Drawable d = getDrawable();
            if (d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0) {
                this.imgW = d.getIntrinsicWidth();
                this.imgH = d.getIntrinsicHeight();
                this.fitScale = Math.min(getWidth() / this.imgW, getHeight() / this.imgH);
            }
        }
        clampPan();
        this.matrix.setScale(scale(), scale());
        this.matrix.postTranslate(imageLeft(), imageTop());
        setImageMatrix(this.matrix);
        invalidate();
    }

    /** Картинку нельзя утащить за край: хотя бы половина экрана остаётся закрыта картинкой. */
    private void clampPan() {
        float w = this.imgW * scale();
        float h = this.imgH * scale();
        if (w <= getWidth()) {
            this.panX = 0.0f;
        } else {
            float maxX = (w - getWidth()) / 2.0f;
            this.panX = Math.max(-maxX, Math.min(maxX, this.panX));
        }
        if (h <= getHeight()) {
            this.panY = 0.0f;
        } else {
            float maxY = (h - getHeight()) / 2.0f;
            this.panY = Math.max(-maxY, Math.min(maxY, this.panY));
        }
    }
}
