package by.csl.gardener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Самообновление с GitHub Releases: фоновая проверка не чаще раза в 6 часов,
 * диалог «Доступна версия», скачивание APK и запуск штатного установщика.
 */
public final class AppUpdate {
    private static final String RELEASES_API =
            "https://api.github.com/repos/zigorminsk-debug/ZI-Garden/releases/latest";
    private static final long CHECK_EVERY_MS = 6L * 60 * 60 * 1000; // раз в 6 часов
    private static final String PREFS = "app_update";
    private static final String KEY_LAST_CHECK = "last_check";

    private AppUpdate() {}

    /** Авто-проверка при запуске приложения (с троттлингом, в фоне). */
    public static void autoCheck(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, 0);
        long last = prefs.getLong(KEY_LAST_CHECK, 0);
        if (System.currentTimeMillis() - last < CHECK_EVERY_MS) {
            return;
        }
        check(activity, false);
    }

    /** Ручная проверка из экрана «О приложении» — без троттлинга, с ответом пользователю. */
    public static void checkNow(Activity activity) {
        check(activity, true);
    }

    private static void check(final Activity activity, final boolean manual) {
        if (manual) {
            Ui.toast(activity, "Проверяем обновления…");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateInfo info = fetchLatest();
                saveLastCheck(activity);
                final long own = ownVersionCode(activity);
                if (info != null && UpdateInfo.isNewer(info.versionCode, own)) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            offer(activity, info);
                        }
                    });
                } else if (manual) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Ui.toast(activity, info == null
                                    ? "Не удалось проверить обновления (нет интернета?)"
                                    : "У вас последняя версия — " + ownVersionName(activity));
                        }
                    });
                }
            }
        }, "update-check").start();
    }

    /** Загрузка JSON последнего релиза. В фоне. */
    static UpdateInfo fetchLatest() {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(RELEASES_API).openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "ZI-Garden-App");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            if (conn.getResponseCode() != 200) {
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return UpdateInfo.fromReleaseJson(sb.toString());
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** Код версии установленного приложения (17000+N для CI, 2x0000 для релизов). */
    static long ownVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pi == null) return 0;
            if (Build.VERSION.SDK_INT >= 28) {
                return pi.getLongVersionCode();
            }
            return pi.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    static String ownVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi == null || pi.versionName == null ? "?" : pi.versionName;
        } catch (Exception e) {
            return "?";
        }
    }

    private static void saveLastCheck(Context context) {
        context.getSharedPreferences(PREFS, 0).edit()
                .putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
    }

    private static void offer(final Activity activity, final UpdateInfo info) {
        String notes = info.notes == null ? "" : info.notes.trim();
        if (notes.length() > 500) {
            notes = notes.substring(0, 500) + "…";
        }
        StringBuilder msg = new StringBuilder("На GitHub опубликована новая версия ")
                .append(info.tag).append(".\nУ вас: ").append(ownVersionName(activity));
        if (notes.length() > 0) {
            msg.append("\n\nЧто нового:\n").append(notes);
        }
        new AlertDialog.Builder(activity)
                .setTitle("⬇️ Доступно обновление")
                .setMessage(msg.toString())
                .setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        download(activity, info);
                    }
                })
                .setNegativeButton("Позже", null)
                .show();
    }

    private static void download(final Activity activity, final UpdateInfo info) {
        final ProgressDialog progress = new ProgressDialog(activity);
        progress.setMessage("Скачиваем " + info.tag + "…");
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ok = downloadTo(activity, info.apkUrl);
                dismiss(progress);
                if (ok) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            install(activity);
                        }
                    });
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Ui.toast(activity, "Не удалось скачать обновление — попробуйте позже");
                        }
                    });
                }
            }
        }, "update-download").start();
    }

    private static void dismiss(final ProgressDialog progress) {
        try {
            progress.dismiss();
        } catch (Exception ignored) {
        }
    }

    /** Скачивает APK в приватную папку приложения. Вызывать только из фонового потока. */
    static boolean downloadTo(Context context, String url) {
        HttpURLConnection conn = null;
        File target = ApkProvider.targetFile(context);
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("User-Agent", "ZI-Garden-App");
            if (conn.getResponseCode() != 200) {
                return false;
            }
            InputStream in = conn.getInputStream();
            FileOutputStream out = new FileOutputStream(target);
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.flush();
            out.close();
            in.close();
            return target.length() > 100000; // APK меньше ~100 КБ быть не может
        } catch (Exception e) {
            if (target.isFile()) {
                target.delete();
            }
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** Запускает установщик со скачанным APK. */
    private static void install(Activity activity) {
        if (Build.VERSION.SDK_INT >= 26
                && !activity.getPackageManager().canRequestPackageInstalls()) {
            Ui.toast(activity, "Разрешите установку из этого источника и вернитесь в приложение");
            try {
                activity.startActivity(new Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES",
                        Uri.parse("package:" + activity.getPackageName())));
            } catch (Exception e) {
                activity.startActivity(new Intent("android.settings.SECURITY_SETTINGS"));
            }
            return;
        }
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(ApkProvider.contentUri());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Ui.toast(activity, "Установщик недоступен: " + e.getMessage());
        }
    }
}
