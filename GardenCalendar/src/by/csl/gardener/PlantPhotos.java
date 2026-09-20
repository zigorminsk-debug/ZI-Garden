package by.csl.gardener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Пользовательские фотографии культур: копия изображения складывается в приватную папку
 * приложения (уменьшенная до 1280px по длинной стороне, JPEG 85) — работает и офлайн,
 * не требует раз никаких разрешений (выбор — через системный файловый диалог SAF).
 * Фото живут только на этом устройстве (в резервную копию не входят).
 */
final class PlantPhotos {
    static final String DIR = "plant_photos";
    static final int MAX_SIDE = 1280;
    private static final int JPEG_QUALITY = 85;

    /** Разовая привязка «последний запрошенный выбор фото → культура» для приёмника. */
    static String pendingPlantId = null;

    private PlantPhotos() {}

    static File dir(Context ctx) {
        File d = new File(ctx.getFilesDir(), DIR);
        if (!d.isDirectory()) {
            d.mkdirs();
        }
        return d;
    }

    static File fileFor(Context ctx, String plantId) {
        return new File(dir(ctx), plantId + ".jpg");
    }

    static boolean has(Context ctx, String plantId) {
        File f = fileFor(ctx, plantId);
        return f.isFile() && f.length() > 0;
    }

    /** Декодирует фото для показа (с подвыборкой по ширине экрана — не ест память). */
    static Bitmap load(Context ctx, String plantId, int reqWidthPx) {
        File f = fileFor(ctx, plantId);
        if (!f.isFile()) {
            return null;
        }
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), bounds);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize(bounds.outWidth, reqWidthPx > 0 ? reqWidthPx : 600);
            return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        } catch (Exception e) {
            return null;
        }
    }

    /** Сохранить картинку из SAF-URI: уменьшить и записать JPEG. true при успехе. */
    static boolean saveFromUri(Context ctx, String plantId, Uri uri) {
        if (uri == null) {
            return false;
        }
        try {
            try {
                ctx.getContentResolver().takePersistableUriPermission(uri, 1 /* FLAG_GRANT_READ_URI_PERMISSION */);
            } catch (Exception ignored) {
            }
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            InputStream in1 = ctx.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(in1, null, bounds);
            try { in1.close(); } catch (Exception ignored) {}
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return false;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = Math.max(1, Math.min(bounds.outWidth, bounds.outHeight) / 512);
            InputStream in2 = ctx.getContentResolver().openInputStream(uri);
            Bitmap full = BitmapFactory.decodeStream(in2, null, opts);
            try { in2.close(); } catch (Exception ignored) {}
            if (full == null) {
                return false;
            }
            Bitmap scaled = full;
            int w = full.getWidth();
            int h = full.getHeight();
            int longSide = Math.max(w, h);
            if (longSide > MAX_SIDE) {
                float k = ((float) MAX_SIDE) / longSide;
                scaled = Bitmap.createScaledBitmap(full,
                        Math.max(1, Math.round(w * k)), Math.max(1, Math.round(h * k)), true);
            }
            File target = fileFor(ctx, plantId);
            FileOutputStream out = new FileOutputStream(target);
            boolean ok = scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
            out.flush();
            out.close();
            return ok && target.isFile() && target.length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean remove(Context ctx, String plantId) {
        File f = fileFor(ctx, plantId);
        return !f.exists() || f.delete();
    }

    private static int sampleSize(int width, int target) {
        int s = 1;
        while (width / (s * 2) >= target) {
            s *= 2;
        }
        return s;
    }
}
