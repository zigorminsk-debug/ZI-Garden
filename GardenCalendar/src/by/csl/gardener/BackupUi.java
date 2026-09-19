package by.csl.gardener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.Locale;
import java.util.Map;

/** Общий диалог восстановления из резервной копии (настройки, письмо семьи, тап по .json). */
final class BackupUi {
    private BackupUi() {}

    /**
     * Показывает «Восстановить? N записей, копия от …» и при согласии заменяет данные.
     * json — сырой текст копии; afterOk — необязательное действие после восстановления
     * (например, перезапустить экран или открыть главный).
     */
    static void confirm(final Activity activity, String json, final Runnable afterOk) {
        final Map<String, Object>[] decoded = Backup.decode(json);
        if (decoded == null) {
            Ui.toast(activity, "Это не файл резервной копии ZI Garden (или текст испорчен)");
            return;
        }
        int count = decoded[0].size() + decoded[1].size() + decoded[2].size();
        long ts = Backup.timestamp(json);
        String when = ts > 0
                ? new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
                        .format(new java.util.Date(ts))
                : "дата неизвестна";
        new AlertDialog.Builder(activity)
                .setTitle("Восстановить данные?")
                .setMessage("Копия от " + when + " — " + count + " записей.\n\n"
                        + "Текущие данные будут стёрты и заменены данными из копии — отменить это нельзя.")
                .setPositiveButton("Восстановить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int n = new Storage(activity).importAll(decoded[0], decoded[1], decoded[2]);
                        Ui.toast(activity, "✅ Восстановлено " + n + " записей");
                        if (afterOk != null) {
                            afterOk.run();
                        }
                    }
                })
                .setNegativeButton("Отмена", (DialogInterface.OnClickListener) null)
                .show();
    }
}
