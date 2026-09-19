package by.csl.gardener;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import by.csl.gardener.Geo;
import by.csl.gardener.Weather;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    private static final int REQ_NOTIFY = 1001;
    private Calendar lastDay;
    private boolean loading;
    private String pendingTaskId;
    private Storage store;
    private Weather weather;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        RemoteDiseases.refreshAsync(this);
        setContentView(R.layout.activity_main);
        this.store = new Storage(this);
        Notifications.ensureChannel(this);
        findViewById(R.id.btn_plants).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.m5lambda$onCreate$0$bycslgardenerMainActivity(view);
            }
        });
        findViewById(R.id.btn_tasks).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.m6lambda$onCreate$1$bycslgardenerMainActivity(view);
            }
        });
        findViewById(R.id.btn_calendar).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.m7lambda$onCreate$2$bycslgardenerMainActivity(view);
            }
        });
        findViewById(R.id.btn_materials).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.m8lambda$onCreate$3$bycslgardenerMainActivity(view);
            }
        });
        findViewById(R.id.btn_about).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.m9lambda$onCreate$4$bycslgardenerMainActivity(view);
            }
        });
        findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) SettingsActivity.class));
            }
        });
        findViewById(R.id.btn_gps).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.m10lambda$onCreate$5$bycslgardenerMainActivity(view);
            }
        });
        findViewById(R.id.btn_weather).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.loadWeather(false);
            }
        });
        if (Build.VERSION.SDK_INT >= 33 && !Notifications.permissionGranted(this)) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_NOTIFY);
        }
        if (this.store.locationSource().equals("default") && Geo.hasPermission(this)) {
            trySilentGps();
        }
        String stringExtra = getIntent() == null ? null : getIntent().getStringExtra(Notifications.EXTRA_TASK);
        if (stringExtra != null) {
            getIntent().removeExtra(Notifications.EXTRA_TASK);
            this.pendingTaskId = stringExtra;
        }
        AppUpdate.autoCheck(this);
    }

    void m5lambda$onCreate$0$bycslgardenerMainActivity(View view) {
        startActivity(new Intent(this, (Class<?>) PlantsActivity.class));
    }

    void m6lambda$onCreate$1$bycslgardenerMainActivity(View view) {
        startActivity(new Intent(this, (Class<?>) TasksActivity.class));
    }

    void m7lambda$onCreate$2$bycslgardenerMainActivity(View view) {
        startActivity(new Intent(this, (Class<?>) CalendarActivity.class));
    }

    void m8lambda$onCreate$3$bycslgardenerMainActivity(View view) {
        startActivity(new Intent(this, (Class<?>) MaterialsActivity.class));
    }

    void m9lambda$onCreate$4$bycslgardenerMainActivity(View view) {
        startActivity(new Intent(this, (Class<?>) AboutActivity.class));
    }

    void m10lambda$onCreate$5$bycslgardenerMainActivity(View view) {
        onGpsButton();
    }

    protected void onResume() {
        super.onResume();
        this.weather = Weather.fromJson(this.store.weatherCache());
        m1lambda$showPendingTask$0$bycslgardenerMainActivity();
        Notifications.scheduleAll(this);
        if ((this.weather == null || System.currentTimeMillis() - this.weather.fetchedAt > 10800000) && !this.loading) {
            loadWeather(true);
        }
        showPendingTask();
    }

    private void showPendingTask() {
        if (this.pendingTaskId == null) {
            return;
        }
        for (Task task : new Planner(this.store, this.weather).tasks(21)) {
            if (task.id.equals(this.pendingTaskId)) {
                this.pendingTaskId = null;
                TaskDialog.show(this, task, new Runnable() {
                    public final void run() {
                        MainActivity.this.m1lambda$showPendingTask$0$bycslgardenerMainActivity();
                    }
                });
                return;
            }
        }
        this.pendingTaskId = null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Обновить погоду");
        menu.add(0, 2, 1, R.string.settings);
        menu.add(0, 3, 2, R.string.about);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == 1) {
            loadWeather(false);
            return true;
        }
        if (itemId == 2) {
            startActivity(new Intent(this, (Class<?>) SettingsActivity.class));
            return true;
        }
        if (itemId == 3) {
            startActivity(new Intent(this, (Class<?>) AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void trySilentGps() {
        LocationManager locationManager = (LocationManager) getSystemService("location");
        if (locationManager == null) {
            return;
        }
        try {
            Iterator<String> it = locationManager.getAllProviders().iterator();
            Location location = null;
            while (it.hasNext()) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(it.next());
                if (lastKnownLocation != null && (location == null || lastKnownLocation.getTime() > location.getTime())) {
                    location = lastKnownLocation;
                }
            }
            if (location == null || System.currentTimeMillis() - location.getTime() > 43200000) {
                return;
            }
            applyGpsFix(Geo.round4(location.getLatitude()), Geo.round4(location.getLongitude()), Geo.label(this, location.getLatitude(), location.getLongitude()), false);
        } catch (Exception unused) {
        }
    }

    private void onGpsButton() {
        if (!Geo.hasPermission(this)) {
            Geo.requestPermission(this);
        } else {
            Geo.locate(this, new Geo.OnFix() {
                public final void onFixed(double d, double d2, String str) {
                    MainActivity.this.m11lambda$onGpsButton$6$bycslgardenerMainActivity(d, d2, str);
                }
            });
        }
    }

    void m11lambda$onGpsButton$6$bycslgardenerMainActivity(double d, double d2, String str) {
        applyGpsFix(d, d2, str, true);
    }

    private void applyGpsFix(double d, double d2, String str, boolean z) {
        this.store.setLocation(str, d, d2);
        this.store.setLocationSource("gps");
        this.store.setWeatherCache("");
        this.weather = null;
        Region detect = Region.detect(d, d2);
        if (z) {
            Ui.toast(this, "Местоположение: " + str + " · регион: " + detect.displayName + ". Цены и сроки пересчитаны.");
        }
        m1lambda$showPendingTask$0$bycslgardenerMainActivity();
        loadWeather(true);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 77 && Geo.hasPermission(this)) {
            onGpsButton();
        }
    }

    void m0lambda$renderTasks$1$bycslgardenerMainActivity() {
        m1lambda$showPendingTask$0$bycslgardenerMainActivity();
    }

    public void m1lambda$showPendingTask$0$bycslgardenerMainActivity() {
        String str;
        TextView textView = (TextView) findViewById(R.id.location);
        TextView textView2 = (TextView) findViewById(R.id.weather_now);
        TextView textView3 = (TextView) findViewById(R.id.forecast);
        TextView textView4 = (TextView) findViewById(R.id.weather_updated);
        Region detect = Region.detect(this.store.lat(), this.store.lon());
        StringBuilder sb = new StringBuilder("📍 ");
        sb.append(this.store.city());
        sb.append(" · ");
        sb.append(detect.displayName);
        sb.append(" · ");
        sb.append(detect.currencyCode);
        sb.append("\n");
        sb.append(String.format(Locale.US, "%.4f, %.4f", Double.valueOf(this.store.lat()), Double.valueOf(this.store.lon())));
        sb.append(this.store.locationSource().equals("gps") ? " · GPS" : "");
        textView.setText(sb.toString());
        Weather weather = this.weather;
        if (weather == null || !weather.hasForecast()) {
            textView2.setText(this.loading ? "Загрузка прогноза…" : "Прогноз не загружен — нажмите «Обновить погоду»");
            textView3.setText("");
            textView3.setVisibility(View.GONE);
            textView4.setVisibility(View.GONE);
        } else {
            textView3.setVisibility(View.VISIBLE);
            textView4.setVisibility(View.VISIBLE);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(Weather.iconFor(this.weather.currentCode));
            sb2.append(" Сейчас: ");
            sb2.append(Double.isNaN(this.weather.currentTemp) ? "" : String.format(Locale.US, "%.1f °C", Double.valueOf(this.weather.currentTemp)));
            sb2.append(", ");
            sb2.append(Weather.textFor(this.weather.currentCode));
            textView2.setText(sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            int min = Math.min(5, this.weather.days.size());
            for (int i = 0; i < min; i++) {
                Weather.Day day = this.weather.days.get(i);
                if (i > 0) {
                    sb3.append('\n');
                }
                sb3.append(Dates.weekdayShort(day.year, day.month, day.day));
                sb3.append(' ');
                sb3.append(Dates.fmtShort(day.year, day.month, day.day));
                sb3.append(' ');
                sb3.append(day.icon());
                sb3.append(' ');
                sb3.append(String.format(Locale.US, "%.0f…%.0f°", Double.valueOf(day.tMin), Double.valueOf(day.tMax)));
                if (day.precipMm >= 0.5d) {
                    sb3.append(String.format(Locale.US, ", %.1f мм", Double.valueOf(day.precipMm)));
                }
            }
            textView3.setText(sb3.toString());
            textView4.setText("Обновлено " + Dates.time(this.weather.fetchedAt) + " · источник: Open-Meteo");
        }
        Planner planner = new Planner(this.store, this.weather);
        int[] stats = planner.stats();
        ((TextView) findViewById(R.id.stats)).setText(String.format(Locale.US, "Ближайшие 3 недели: %d работ, выполнено %d, на сегодня %d, отложено по погоде %d", Integer.valueOf(stats[0]), Integer.valueOf(stats[1]), Integer.valueOf(stats[2]), Integer.valueOf(stats[3])));
        Set<String> plants = this.store.plants();
        StringBuilder sb4 = new StringBuilder();
        for (Plant plant : Plant.all()) {
            if (plants.contains(plant.id)) {
                if (sb4.length() > 0) {
                    sb4.append(' ');
                }
                sb4.append(plant.icon);
            }
        }
        if (this.store.plantSize() == 0) {
            str = "молодые растения";
        } else {
            str = this.store.plantSize() == 2 ? "взрослые растения" : "плодоносящие растения";
        }
        ((TextView) findViewById(R.id.plants_summary)).setText("Отмечено культур: " + plants.size() + "  " + ((Object) sb4) + "\nДозы рассчитаны как «" + str + "»");
        renderTasks(planner);
    }

    private void renderTasks(Planner planner) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tasks_container);
        linearLayout.removeAllViews();
        List<Task> tasks = planner.tasks(21);
        Calendar calendar = Dates.today();
        Calendar plusDays = Dates.plusDays(calendar, 7);
        this.lastDay = null;
        int i = 0;
        for (Task task : tasks) {
            Calendar at = Dates.at(task.year, task.month, task.day);
            if (!at.before(calendar) && !at.after(plusDays)) {
                Calendar calendar2 = this.lastDay;
                if (calendar2 == null || Dates.diffDays(calendar2, at) != 0) {
                    Ui.section(linearLayout, this, headerFor(at, calendar));
                }
                this.lastDay = at;
                linearLayout.addView(Ui.taskCard(this, task, new Runnable() {
                    public final void run() {
                        MainActivity.this.m0lambda$renderTasks$1$bycslgardenerMainActivity();
                    }
                }));
                i++;
                if (i >= 12) {
                    break;
                }
            }
        }
        if (i == 0) {
            linearLayout.addView(Ui.text(this, "На ближайшие 7 дней работ нет. Добавьте растения или расширьте окно в «Календаре».", 14.0f, getResources().getColor(R.color.text_sub), false));
        }
            TasksWidgetProvider.refresh(this);
            Widgets.refreshAll(this);
    }

    private String headerFor(Calendar calendar, Calendar calendar2) {
        int diffDays = Dates.diffDays(calendar2, calendar);
        int i = calendar.get(1);
        int i2 = calendar.get(2) + 1;
        int i3 = calendar.get(5);
        if (diffDays == 0) {
            return "Сегодня, " + Dates.fmtShort(i, i2, i3);
        }
        if (diffDays == 1) {
            return "Завтра, " + Dates.fmtShort(i, i2, i3);
        }
        return Dates.weekday(i, i2, i3) + ", " + Dates.fmtShort(i, i2, i3);
    }

    private void loadWeather(final boolean z) {
        this.loading = true;
        m1lambda$showPendingTask$0$bycslgardenerMainActivity(); // показать «Загрузка прогноза…»
        final double lat = this.store.lat();
        final double lon = this.store.lon();
        final String city = this.store.city();
        new Thread(new Runnable() {
            public final void run() {
                MainActivity.this.m4lambda$loadWeather$8$bycslgardenerMainActivity(lat, lon, city, z);
            }
        }).start();
    }

    void m4lambda$loadWeather$8$bycslgardenerMainActivity(double d, double d2, String str, final boolean z) {
        final String[] strArr = new String[2];
        try {
            strArr[0] = Weather.fetch(d, d2, str).toJson();
        } catch (Exception e) {
            strArr[1] = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
        runOnUiThread(new Runnable() {
            public final void run() {
                MainActivity mainActivity = MainActivity.this;
                String[] strArr2 = strArr;
                mainActivity.m3lambda$loadWeather$7$bycslgardenerMainActivity(strArr2[0], z, strArr2[1]);
            }
        });
    }

    void m3lambda$loadWeather$7$bycslgardenerMainActivity(String str, boolean z, String str2) {
        this.loading = false;
        if (str == null) {
            if (z) {
                return;
            }
            Ui.toast(this, "Не удалось загрузить прогноз: " + str2);
            return;
        }
        this.store.setWeatherCache(str);
        this.weather = Weather.fromJson(str);
        m1lambda$showPendingTask$0$bycslgardenerMainActivity();
        Notifications.scheduleAll(this);
        if (z) {
            return;
        }
        Ui.toast(this, "Прогноз обновлён");
    }
}
