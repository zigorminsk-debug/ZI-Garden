package by.csl.gardener;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Фото-дневник культуры: снимки хранятся внутри приложения в
 * files/photos/&lt;plantId&gt;/, имя файла — метка времени создания.
 * Доступ к галерее — разовый системный выборщик, постоянные разрешения не нужны.
 */
public final class PhotoDiary {

    private PhotoDiary() {
    }

    /** Каталог фото культуры (корень передан явно — удобно для тестов). */
    public static File dir(File root, String plantId) {
        return new File(new File(root, "photos"), safe(plantId));
    }

    /** Каталог фото культуры от контекста. */
    public static File dir(Context ctx, String plantId) {
        return dir(ctx.getFilesDir(), plantId);
    }

    /** Сохранить фото из потока с заданной меткой времени; null — при ошибке. */
    public static File add(File root, String plantId, InputStream in, long whenMs) {
        try {
            File d = dir(root, plantId);
            d.mkdirs();
            File out = new File(d, whenMs + ".jpg");
            FileOutputStream fo = new FileOutputStream(out);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                fo.write(buf, 0, n);
            }
            fo.close();
            in.close();
            return out;
        } catch (Exception unused) {
            return null;
        }
    }

    /** Сохранить фото из потока (сейчас). */
    public static File add(Context ctx, String plantId, InputStream in) {
        return add(ctx.getFilesDir(), plantId, in, System.currentTimeMillis());
    }

    /** Фото культуры по возрастанию времени (старые снизу). */
    public static List<File> photos(File root, String plantId) {
        List<File> out = new ArrayList<>();
        File d = dir(root, plantId);
        File[] arr = d.listFiles();
        if (arr == null) {
            return out;
        }
        List<String> names = new ArrayList<>();
        for (File f : arr) {
            if (f.getName().endsWith(".jpg")) {
                names.add(f.getName());
            }
        }
        Collections.sort(names);
        for (String n : names) {
            out.add(new File(d, n));
        }
        return out;
    }

    /** Фото культуры от контекста. */
    public static List<File> photos(Context ctx, String plantId) {
        return photos(ctx.getFilesDir(), plantId);
    }

    /** Сколько фото у культуры. */
    public static int count(Context ctx, String plantId) {
        return photos(ctx, plantId).size();
    }

    /** Время съёмки из имени файла. */
    public static long takenAt(File photo) {
        try {
            return Long.parseLong(photo.getName().replace(".jpg", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /** Удалить фото. */
    public static boolean delete(File photo) {
        return photo != null && photo.delete();
    }

    /** Идентификатор культуры в безопасное имя каталога. */
    static String safe(String plantId) {
        if (plantId == null || plantId.length() == 0) {
            return "none";
        }
        return plantId.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
