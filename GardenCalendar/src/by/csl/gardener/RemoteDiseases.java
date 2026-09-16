package by.csl.gardener;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Загрузка обновлений справочника болезней с сервера (при изменении выбора культур и при старте). */
public final class RemoteDiseases {
    private RemoteDiseases() {}

    private static final String FILE = "diseases_remote.json";
    private static List<Disease> cache = new ArrayList<>();

    /** Кэш обновлений (читается с диска один раз). */
    public static synchronized List<Disease> cached(Context ctx) {
        if (cache.isEmpty()) {
            cache = loadFromDisk(ctx);
        }
        return cache;
    }

    /** Фоновое обновление с сервера; при неудаче тихо остаёмся на встроенном каталоге. */
    public static void refreshAsync(final Context ctx) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    String url = new Storage(ctx).diseaseUpdateUrl();
                    if (url.length() == 0) return;
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(12000);
                    conn.setRequestProperty("Accept", "application/json");
                    if (conn.getResponseCode() != 200) return;
                    BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line).append('\n');
                    r.close();
                    String json = sb.toString();
                    List<Disease> parsed = DiseaseDb.fromJson(json);
                    if (parsed.isEmpty()) return;
                    File f = new File(ctx.getFilesDir(), FILE);
                    FileOutputStream out = new FileOutputStream(f);
                    out.write(json.getBytes("UTF-8"));
                    out.close();
                    synchronized (RemoteDiseases.class) {
                        cache = parsed;
                    }
                } catch (Exception ignored) {
                    // нет сети / нет файла на сервере — работаем на встроенном справочнике
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Количество записей в кэше обновлений — для отображения в интерфейсе. */
    public static synchronized int cachedCount(Context ctx) {
        return cached(ctx).size();
    }

    private static List<Disease> loadFromDisk(Context ctx) {
        try {
            File f = new File(ctx.getFilesDir(), FILE);
            if (!f.exists()) return new ArrayList<>();
            byte[] buf = new byte[(int) f.length()];
            FileInputStream in = new FileInputStream(f);
            int read = 0;
            while (read < buf.length) {
                int n = in.read(buf, read, buf.length - read);
                if (n < 0) break;
                read += n;
            }
            in.close();
            return DiseaseDb.fromJson(new String(buf, "UTF-8"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
