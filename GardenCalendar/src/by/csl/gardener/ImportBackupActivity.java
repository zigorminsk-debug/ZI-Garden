package by.csl.gardener;

import android.app.Activity;
import android.os.Bundle;

/**
 * Принимает тап по .json-вложению из Gmail/проводника (интент VIEW, mime application/json):
 * читает файл и показывает общий диалог восстановления, затем открывает главный экран.
 */
public final class ImportBackupActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        android.net.Uri uri = getIntent() == null ? null : getIntent().getData();
        if (uri == null) {
            Ui.toast(this, "Нет файла для импорта");
            finish();
            return;
        }
        readAndConfirm(uri);
    }

    private void readAndConfirm(final android.net.Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String json = null;
                try {
                    java.io.InputStream in = getContentResolver().openInputStream(uri);
                    StringBuilder sb = new StringBuilder();
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(in, "UTF-8"));
                    char[] buf = new char[8192];
                    int n;
                    while ((n = br.read(buf)) > 0 && sb.length() < 4194304) {
                        sb.append(buf, 0, n);
                    }
                    br.close();
                    json = sb.toString();
                } catch (Exception ignored) {
                }
                final String text = json;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (text == null) {
                            Ui.toast(ImportBackupActivity.this, "Не удалось прочитать файл");
                            finish();
                            return;
                        }
                        BackupUi.confirm(ImportBackupActivity.this, text, new Runnable() {
                            @Override
                            public void run() {
                                android.content.Intent main = new android.content.Intent(
                                        ImportBackupActivity.this, MainActivity.class);
                                main.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(main);
                                finish();
                            }
                        });
                    }
                });
            }
        }, "backup-import").start();
    }
}
