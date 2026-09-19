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
    private static final String RELEASES_PAGE =
            "https://github.com/zigorminsk-debug/ZI-Garden/releases";
    private static final String PAGES_MIRROR =
            "https://zigorminsk-debug.github.io/ZI-Garden/latest.json"; // зеркало на GitHub Pages (включается в настройках)
    private static final String RAW_MIRROR =
            "https://raw.githubusercontent.com/zigorminsk-debug/ZI-Garden/arena/01a0aa7c-zi-garden/pages/latest.json"; // raw-CDN, работает без настройки
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
                FetchResult res = fetchDetailed(activity);
                saveLastCheck(activity);
                final long own = ownVersionCode(activity);
                final UpdateInfo info = res.info;
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
                            if (info != null) {
                                Ui.toast(activity, "У вас последняя версия — " + ownVersionName(activity));
                            } else {
                                showErrorDialog(activity, res.error);
                            }
                        }
                    });
                }
            }
        }, "update-check").start();
    }

    /** Результат проверки: либо описание релиза, либо понятная пользователю причина сбоя. */
    static final class FetchResult {
        final UpdateInfo info;
        final String error;

        FetchResult(UpdateInfo info, String error) {
            this.info = info;
            this.error = error;
        }
    }

    /**
     * Проверка обновлений по четырём независимым каналам:
     * 1) GitHub API, 2) HTML-страница релизов, 3) raw-зеркало (raw.githubusercontent.com),
     * 4) зеркало latest.json на GitHub Pages (github.io — отдельный CDN, включается в настройках).
     */
    static FetchResult fetchDetailed(Context ctx) {
        if (!isOnline(ctx)) {
            return new FetchResult(null,
                    "На устройстве нет интернета — включите Wi-Fi или мобильную сеть и повторите.\n"
                    + "Либо скачайте новую версию кнопкой «В браузере» на другом устройстве.");
        }
        StringBuilder errors = new StringBuilder();
        // канал 1 — GitHub API (основной)
        try {
            String body = httpGet(RELEASES_API);
            UpdateInfo info = UpdateInfo.fromReleaseJson(body);
            if (info != null) {
                return new FetchResult(info, null);
            }
            errors.append("• API: GitHub ответил без ссылки на APK\n");
        } catch (Exception e) {
            errors.append("• API: ").append(ruError(e)).append('\n');
        }
        // канал 2 — обычная страница релизов на github.com
        try {
            String html = httpGet(RELEASES_PAGE);
            UpdateInfo info = UpdateInfo.fromHtmlPage(html);
            if (info != null) {
                return new FetchResult(info, null);
            }
            errors.append("• Страница: не нашла тега версии или APK\n");
        } catch (Exception e2) {
            errors.append("• Страница: ").append(ruError(e2)).append('\n');
        }
        // канал 3 — raw-зеркало (raw.githubusercontent.com — отдельный CDN, без настройки)
        try {
            String json = httpGet(RAW_MIRROR);
            UpdateInfo info = UpdateInfo.fromReleaseJson(json);
            if (info != null) {
                return new FetchResult(info, null);
            }
            errors.append("• Зеркало(raw): не нашлось ссылки на APK\n");
        } catch (Exception e3) {
            errors.append("• Зеркало(raw): ").append(ruError(e3)).append('\n');
        }
        // канал 4 — зеркало на GitHub Pages (github.io), если включено владельцем
        try {
            String json = httpGet(PAGES_MIRROR);
            UpdateInfo info = UpdateInfo.fromReleaseJson(json);
            if (info != null) {
                return new FetchResult(info, null);
            }
            errors.append("• Зеркало(github.io): не нашлось ссылки на APK");
        } catch (Exception e4) {
            errors.append("• Зеркало(github.io): ").append(ruError(e4));
        }
        return new FetchResult(null, errors.toString().trim());
    }

    /** Диалог с полным текстом сбоя + кнопкой «открыть страницу в браузере». */
    static void showErrorDialog(final Activity activity, String error) {
        new AlertDialog.Builder(activity)
                .setTitle("Не удалось проверить обновления")
                .setMessage((error == null || error.length() == 0 ? "Неизвестная ошибка" : error)
                        + "\n\nМожно скачать новую версию вручную через браузер.")
                .setPositiveButton("Открыть страницу", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openReleasePage(activity);
                    }
                })
                .setNegativeButton("Закрыть", null)
                .show();
    }

    /** Страница с последним релизом — в браузере (сработает, если GitHub доступен). */
    public static void openReleasePage(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(RELEASES_PAGE + "/latest")));
        } catch (Exception e) {
            Ui.toast(activity, "Откройте в браузере: " + RELEASES_PAGE);
        }
    }

    /** GET с таймаутами и User-Agent; не-200 → IOException. В фоне. */
    private static String httpGet(String url) throws java.io.IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(12000);
            conn.setRequestProperty("User-Agent", "ZI-Garden-App");
            conn.setRequestProperty("Accept", "application/vnd.github+json, text/html");
            int code = conn.getResponseCode();
            if (code != 200) {
                throw new java.io.IOException("HTTP " + code);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = br.read(buf)) > 0) {
                sb.append(buf, 0, n);
                if (sb.length() > 1500000) {
                    break; // страницу релизов целиком не нужна
                }
            }
            br.close();
            return sb.toString();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** Есть ли сейчас вообще сеть (до обращения к GitHub). */
    static boolean isOnline(Context ctx) {
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) ctx.getSystemService("connectivity");
            if (cm == null) {
                return true; // не смогли узнать — считаем, что сеть есть, и пробуем
            }
            android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (Exception e) {
            return true; // не смогли узнать — считаем, что сеть есть, и пробуем
        }
    }

    /** Причина сбоя сети по-русски. */
    static String ruError(Exception e) {
        return NetErrors.ru(e);
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
                boolean ok = false;
                for (String url : info.apkUrls) {
                    if (downloadTo(activity, url)) {
                        ok = true;
                        break;
                    }
                }
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
