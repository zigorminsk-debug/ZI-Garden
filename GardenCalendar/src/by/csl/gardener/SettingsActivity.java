package by.csl.gardener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import by.csl.gardener.Geo;
import java.util.ArrayList;
import java.util.Locale;

public class SettingsActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    private static final String[] CITIES = {"Минск", "Брест", "Гомель", "Гродно", "Витебск", "Могилёв", "Бобруйск", "Барановичи", "Борисов", "Пинск", "Орша", "Мозырь", "Солигорск", "Новополоцк", "Лида", "Молодечно", "Полоцк", "Светлогорск", "Жлобин", "Речица", "Слуцк", "Сморгонь", "Волковыск", "Осиповичи"};
    private static final double[][] COORDS;
    private Storage store;

    static {
        double[] dArr = new double[2];
        // fill-array-data instruction
        dArr[0] = 53.8884d;
        dArr[1] = 25.2991d;
        COORDS = new double[][]{new double[]{53.9045d, 27.5615d}, new double[]{52.0976d, 23.7341d}, new double[]{52.4345d, 30.9754d}, new double[]{53.6778d, 23.8295d}, new double[]{55.1904d, 30.2049d}, new double[]{53.9007d, 30.3314d}, new double[]{53.1384d, 29.2214d}, new double[]{53.1327d, 26.0139d}, new double[]{54.2279d, 28.5053d}, new double[]{52.1229d, 26.0951d}, new double[]{54.5081d, 30.4172d}, new double[]{52.0472d, 29.2453d}, new double[]{52.7876d, 27.5415d}, new double[]{55.5322d, 28.65d}, dArr, new double[]{54.3167d, 26.8464d}, new double[]{55.4868d, 28.7861d}, new double[]{52.63d, 29.73d}, new double[]{52.8946d, 30.0383d}, new double[]{52.3622d, 30.3945d}, new double[]{53.0276d, 27.5581d}, new double[]{54.4833d, 26.4d}, new double[]{53.1606d, 24.4492d}, new double[]{53.2996d, 28.6431d}};
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        this.store = new Storage(this);
        final Spinner spinner = (Spinner) findViewById(R.id.city);
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, CITIES));
        int i = 0;
        while (true) {
            String[] strArr = CITIES;
            if (i >= strArr.length) {
                i = 0;
                break;
            } else if (strArr[i].equals(this.store.city())) {
                break;
            } else {
                i++;
            }
        }
        spinner.setSelection(i);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            public void onItemSelected(AdapterView<?> adapterView, View view, int i2, long j) {
                ((EditText) SettingsActivity.this.findViewById(R.id.lat)).setText(String.valueOf(SettingsActivity.COORDS[i2][0]));
                ((EditText) SettingsActivity.this.findViewById(R.id.lon)).setText(String.valueOf(SettingsActivity.COORDS[i2][1]));
            }
        });
        ((EditText) findViewById(R.id.lat)).setText(String.valueOf(this.store.lat()));
        ((EditText) findViewById(R.id.lon)).setText(String.valueOf(this.store.lon()));
        findViewById(R.id.save_location).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.m15lambda$onCreate$0$bycslgardenerSettingsActivity(spinner, view);
            }
        });
        findViewById(R.id.btn_gps).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.m16lambda$onCreate$1$bycslgardenerSettingsActivity(view);
            }
        });
        updateRegionStatus();
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.size_group);
        int plantSize = this.store.plantSize();
        radioGroup.check(plantSize == 0 ? R.id.size_young : plantSize == 2 ? R.id.size_old : R.id.size_medium);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public final void onCheckedChanged(RadioGroup radioGroup2, int i2) {
                SettingsActivity.this.m17lambda$onCreate$2$bycslgardenerSettingsActivity(radioGroup2, i2);
            }
        });
        RadioGroup fontGroup = (RadioGroup) findViewById(R.id.font_group);
        int fontSize = this.store.fontSize();
        fontGroup.check(fontSize == 0 ? R.id.font_small : fontSize == 2 ? R.id.font_large : fontSize == 3 ? R.id.font_xlarge : fontSize == 4 ? R.id.font_xxlarge : fontSize == 5 ? R.id.font_xxxlarge : R.id.font_normal);
        fontGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public final void onCheckedChanged(RadioGroup radioGroup2, int i2) {
                SettingsActivity.this.store.setFontSize(i2 == R.id.font_small ? 0 : i2 == R.id.font_large ? 2 : i2 == R.id.font_xlarge ? 3 : i2 == R.id.font_xxlarge ? 4 : i2 == R.id.font_xxxlarge ? 5 : 1);
                SettingsActivity.this.recreate();
            }
        });
        CheckBox checkBox = (CheckBox) findViewById(R.id.notify_enabled);
        checkBox.setChecked(this.store.notifyEnabled());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                SettingsActivity.this.m18lambda$onCreate$3$bycslgardenerSettingsActivity(compoundButton, z);
            }
        });
        final Button button = (Button) findViewById(R.id.notify_time);
        updateTimeButton(button);
        button.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.m20lambda$onCreate$5$bycslgardenerSettingsActivity(button, view);
            }
        });
        Spinner spinner2 = (Spinner) findViewById(R.id.lead);
        spinner2.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"В день работы", "За 1 день", "За 2 дня", "За 3 дня"}));
        spinner2.setSelection(Math.max(0, Math.min(3, this.store.leadDays())));
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            public void onItemSelected(AdapterView<?> adapterView, View view, int i2, long j) {
                SettingsActivity.this.store.setLeadDays(i2);
                SettingsActivity settingsActivity = SettingsActivity.this;
                settingsActivity.status(Notifications.scheduleAll(settingsActivity));
            }
        });
        findViewById(R.id.test_notify).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.m21lambda$onCreate$6$bycslgardenerSettingsActivity(view);
            }
        });
        findViewById(R.id.reset_year).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.m23lambda$onCreate$8$bycslgardenerSettingsActivity(view);
            }
        });
        findViewById(R.id.clear_bought).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.m24lambda$onCreate$9$bycslgardenerSettingsActivity(view);
            }
        });
        findViewById(R.id.backup_export).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.onBackupExport();
            }
        });
        findViewById(R.id.backup_import).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SettingsActivity.this.onBackupImport();
            }
        });
        status(Notifications.scheduleAll(this));
    }

    private static final int REQ_BACKUP_SAVE = 46;
    private static final int REQ_BACKUP_OPEN = 47;

    /** Экспорт: системное окно «сохранить файл» (Storage Access Framework, без разрешений). */
    void onBackupExport() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("application/json");
        String stamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(new java.util.Date());
        intent.putExtra(android.content.Intent.EXTRA_TITLE, "zi-garden-backup-" + stamp + ".json");
        try {
            startActivityForResult(intent, REQ_BACKUP_SAVE);
        } catch (Exception e) {
            Ui.toast(this, "Не нашлось файлового менеджера — обновите устройство и повторите");
        }
    }

    /** Импорт: сначала предупреждение о полной замене данных. */
    void onBackupImport() {
        new AlertDialog.Builder(this)
                .setTitle("Импорт данных")
                .setMessage("Импорт ПОЛНОСТЬЮ ЗАМЕНИТ текущие данные (культуры, отметки «выполнено», журнал, настройки) содержимым файла.\n\nСовет: сначала сделайте экспорт — это ваша страховка.")
                .setPositiveButton("Выбрать файл", new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory("android.intent.category.OPENABLE");
                        intent.setType("*/*");
                        try {
                            startActivityForResult(intent, REQ_BACKUP_OPEN);
                        } catch (Exception e) {
                            Ui.toast(SettingsActivity.this, "Не нашлось файлового менеджера — обновите устройство и повторите");
                        }
                    }
                })
                .setNegativeButton("Отмена", (DialogInterface.OnClickListener) null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        android.net.Uri uri = data.getData();
        if (requestCode == REQ_BACKUP_SAVE) {
            doBackupWrite(uri);
        } else if (requestCode == REQ_BACKUP_OPEN) {
            doBackupRead(uri);
        }
    }

    /** Пишет резервную копию (в фоне), затем сообщает итог. */
    private void doBackupWrite(final android.net.Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                try {
                    java.util.Map<String, ?>[] maps = SettingsActivity.this.store.exportAll();
                    int count = maps[0].size() + maps[1].size() + maps[2].size();
                    String json = Backup.encode(maps[0], maps[1], maps[2], AppUpdate.ownVersionCode(SettingsActivity.this));
                    java.io.OutputStream out = SettingsActivity.this.getContentResolver().openOutputStream(uri, "wt");
                    out.write(json.getBytes("UTF-8"));
                    out.flush();
                    out.close();
                    message = "💾 Резервная копия сохранена: " + count + " записей. Храните файл в надёжном месте.";
                } catch (Exception e) {
                    message = "Не удалось сохранить копию: " + e.getMessage();
                }
                final String toast = message;
                SettingsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Ui.toast(SettingsActivity.this, toast);
                    }
                });
            }
        }, "backup-write").start();
    }

    /** Читает файл, декодирует и предлагает подтвердить восстановление. */
    private void doBackupRead(final android.net.Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    java.io.InputStream in = SettingsActivity.this.getContentResolver().openInputStream(uri);
                    StringBuilder sb = new StringBuilder();
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(in, "UTF-8"));
                    char[] buf = new char[8192];
                    int n;
                    while ((n = br.read(buf)) > 0 && sb.length() < 4194304) {
                        sb.append(buf, 0, n);
                    }
                    br.close();
                    final java.util.Map<String, Object>[] decoded = Backup.decode(sb.toString());
                    SettingsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SettingsActivity.this.confirmBackupRestore(decoded);
                        }
                    });
                } catch (Exception e) {
                    SettingsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Ui.toast(SettingsActivity.this, "Не удалось прочитать файл — выбран не тот файл?");
                        }
                    });
                }
            }
        }, "backup-read").start();
    }

    /** Диалог подтверждения и само восстановление. */
    void confirmBackupRestore(final java.util.Map<String, Object>[] decoded) {
        if (decoded == null) {
            Ui.toast(this, "Это не файл резервной копии ZI Garden (или файл испорчен)");
            return;
        }
        int count = decoded[0].size() + decoded[1].size() + decoded[2].size();
        new AlertDialog.Builder(this)
                .setTitle("Восстановить данные?")
                .setMessage("В файле " + count + " записей. Текущие данные будут стёрты и заменены данными из файла — отменить это нельзя.")
                .setPositiveButton("Восстановить", new DialogInterface.OnClickListener() {
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        int n = SettingsActivity.this.store.importAll(decoded[0], decoded[1], decoded[2]);
                        Ui.toast(SettingsActivity.this, "✅ Восстановлено " + n + " записей. Откройте нужный экран — данные уже на месте.");
                        SettingsActivity.this.setResult(Activity.RESULT_OK);
                        SettingsActivity.this.recreate();
                    }
                })
                .setNegativeButton("Отмена", (DialogInterface.OnClickListener) null)
                .show();
    }

    void m15lambda$onCreate$0$bycslgardenerSettingsActivity(Spinner spinner, View view) {
        String str = CITIES[spinner.getSelectedItemPosition()];
        this.store.setLocation(str, parse(((EditText) findViewById(R.id.lat)).getText().toString(), this.store.lat()), parse(((EditText) findViewById(R.id.lon)).getText().toString(), this.store.lon()));
        this.store.setLocationSource("manual");
        this.store.setWeatherCache("");
        Ui.toast(this, "Участок сохранён: " + str + ". Прогноз обновится на главном экране.");
        finish();
    }

    void m16lambda$onCreate$1$bycslgardenerSettingsActivity(View view) {
        onGps();
    }

    void m17lambda$onCreate$2$bycslgardenerSettingsActivity(RadioGroup radioGroup, int i) {
        this.store.setPlantSize(i == R.id.size_young ? 0 : i == R.id.size_old ? 2 : 1);
    }

    void m18lambda$onCreate$3$bycslgardenerSettingsActivity(CompoundButton compoundButton, boolean z) {
        this.store.setNotifyEnabled(z);
        status(Notifications.scheduleAll(this));
    }

    void m20lambda$onCreate$5$bycslgardenerSettingsActivity(final Button button, View view) {
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            public final void onTimeSet(TimePicker timePicker, int i, int i2) {
                SettingsActivity.this.m19lambda$onCreate$4$bycslgardenerSettingsActivity(button, timePicker, i, i2);
            }
        }, this.store.notifyHour(), this.store.notifyMinute(), true).show();
    }

    void m19lambda$onCreate$4$bycslgardenerSettingsActivity(Button button, TimePicker timePicker, int i, int i2) {
        this.store.setNotifyTime(i, i2);
        updateTimeButton(button);
        status(Notifications.scheduleAll(this));
    }

    void m21lambda$onCreate$6$bycslgardenerSettingsActivity(View view) {
        testNotify();
    }

    void m23lambda$onCreate$8$bycslgardenerSettingsActivity(View view) {
        new AlertDialog.Builder(this).setMessage("Снять все отметки «выполнено» за " + Dates.today().get(1) + " год?").setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                SettingsActivity.this.m22lambda$onCreate$7$bycslgardenerSettingsActivity(dialogInterface, i);
            }
        }).setNegativeButton("Нет", (DialogInterface.OnClickListener) null).show();
    }

    void m22lambda$onCreate$7$bycslgardenerSettingsActivity(DialogInterface dialogInterface, int i) {
        this.store.clearDoneForCurrentYear(Dates.today().get(1));
        Ui.toast(this, "Отметки сброшены");
    }

    void m24lambda$onCreate$9$bycslgardenerSettingsActivity(View view) {
        this.store.setBought(new ArrayList());
        Ui.toast(this, "Список покупок очищен");
    }

    public void status(int i) {
        TextView textView = (TextView) findViewById(R.id.notify_status);
        if (!this.store.notifyEnabled()) {
            textView.setText("Напоминания выключены.");
        } else if (Build.VERSION.SDK_INT >= 33 && !Notifications.permissionGranted(this)) {
            textView.setText("⚠️ Разрешение на уведомления не выдано — система не покажет их. Разрешите уведомления для приложения в настройках Android.");
        } else {
            textView.setText(String.format(Locale.US, "Поставлено напоминаний: %d · время %02d:%02d · за %d дн. до работы · тишина 21:00–8:00", Integer.valueOf(i), Integer.valueOf(this.store.notifyHour()), Integer.valueOf(this.store.notifyMinute()), Integer.valueOf(this.store.leadDays())));
        }
    }

    private void updateTimeButton(Button button) {
        button.setText(String.format(Locale.US, "Время напоминания: %02d:%02d", Integer.valueOf(this.store.notifyHour()), Integer.valueOf(this.store.notifyMinute())));
    }

    private double parse(String str, double d) {
        try {
            return Double.parseDouble(str.replace(',', '.'));
        } catch (Exception unused) {
            return d;
        }
    }

    private void testNotify() {
        Notification.Builder builder;
        Notifications.ensureChannel(this);
        if (Build.VERSION.SDK_INT >= 33 && !Notifications.permissionGranted(this)) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 55);
            Ui.toast(this, "Выдайте разрешение на уведомления, затем повторите");
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(this, Notifications.CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setSmallIcon(R.drawable.ic_leaf).setContentTitle("🌿 Тестовое напоминание").setContentText("Так будут выглядеть уведомления о работах в саду").setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        if (notificationManager != null) {
            notificationManager.notify(9999, builder.build());
        }
    }

    private void onGps() {
        if (!Geo.hasPermission(this)) {
            Geo.requestPermission(this);
        } else {
            Geo.locate(this, new Geo.OnFix() {
                public final void onFixed(double d, double d2, String str) {
                    SettingsActivity.this.m25lambda$onGps$10$bycslgardenerSettingsActivity(d, d2, str);
                }
            });
        }
    }

    void m25lambda$onGps$10$bycslgardenerSettingsActivity(double d, double d2, String str) {
        this.store.setLocation(str, d, d2);
        this.store.setLocationSource("gps");
        this.store.setWeatherCache("");
        ((EditText) findViewById(R.id.lat)).setText(String.valueOf(d));
        ((EditText) findViewById(R.id.lon)).setText(String.valueOf(d2));
        updateRegionStatus();
        Ui.toast(this, "Местоположение определено: " + Region.detect(d, d2).displayName + ". Цены и сроки пересчитаны.");
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 77) {
            onGps();
        }
        if (i == 55) {
            status(Notifications.scheduleAll(this));
        }
    }

    private void updateRegionStatus() {
        Region detect = Region.detect(this.store.lat(), this.store.lon());
        ((TextView) findViewById(R.id.region_status)).setText("🗺️ Регион: " + detect.displayName + " · валюта: " + detect.currencyCode + "\n" + detect.note);
    }
}
