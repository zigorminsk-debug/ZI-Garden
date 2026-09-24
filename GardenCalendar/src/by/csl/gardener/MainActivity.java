package by.csl.gardener;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
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
    private int themeAtCreate;

    /** Карточка «Сейчас в природе»: сезон и его приметы, тап — раздел фенологии. */
    private void setupPhenologyCard() {
        LinearLayout card = (LinearLayout) findViewById(R.id.phenology_card);
        String season = PhenologyGuide.seasonFor(
                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1);
        StringBuilder sb = new StringBuilder();
        for (PhenologyGuide.Sign s : PhenologyGuide.bySeason(season)) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(s.title);
        }
        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);
        card.addView(Ui.text(this,
                PhenologyGuide.seasonEmoji(season) + " Сейчас в природе: " + season.toLowerCase(),
                14.0f, cMain, true));
        TextView signs = Ui.text(this, sb.toString(), 12.0f, cSub, false);
        signs.setPadding(0, Ui.dp(this, 3.0f), 0, 0);
        card.addView(signs);
        card.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                PhenologyActivity.show(MainActivity.this);
            }
        });
    }

    /** Тихая подсказка от семьи: если на сервере данные новее наших — один раз сообщим. */
    private void checkFamilyUpdates() {
        final Storage storage = this.store == null ? new Storage(this) : this.store;
        if (!storage.syncLinked()) {
            return;
        }
        if (System.currentTimeMillis() - storage.syncLastTs() < 3600000L * 6
                && storage.syncLastTs() > 0) {
            return; // недавно синхронизировались — не дёргаем сервер
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    org.json.JSONObject pulled = SyncClient.pull(storage.syncServer(), storage.syncToken());
                    final long remoteTs = pulled.optLong("ts", 0);
                    if (remoteTs > storage.syncLastTs()) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Ui.toast(MainActivity.this,
                                        "👨‍👩‍👧 Семья прислала обновления сада — Настройки → 🔄 Синхронизировать");
                            }
                        });
                    }
                } catch (Exception ignored) {
                    // фон — молчим
                }
            }
        }, "family-hint").start();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        RemoteDiseases.refreshAsync(this);
        Ui.setContent(this, R.layout.activity_main);
        this.store = new Storage(this);
        this.themeAtCreate = this.store.themeMode();
        OnboardingActivity.showIfNeeded(this);
        setupPhenologyCard();
        Notifications.ensureChannel(this);
        checkFamilyUpdates();
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
        findViewById(R.id.btn_soil).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) SoilActivity.class));
            }
        });
        findViewById(R.id.btn_minerals).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) DeficiencyActivity.class));
            }
        });
        findViewById(R.id.btn_planting).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) PlantingActivity.class));
            }
        });
        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) SearchActivity.class));
            }
        });
        findViewById(R.id.btn_phenology).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) PhenologyActivity.class));
            }
        });
        findViewById(R.id.btn_journal).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) JournalActivity.class));
            }
        });
        findViewById(R.id.btn_moon).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                LunarActivity.show(MainActivity.this);
            }
        });
        findViewById(R.id.btn_pests).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.pickCulture("pest");
            }
        });
        findViewById(R.id.btn_diseases).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivity.this.pickCulture("disease");
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
        PlantsActivity.show(this);
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
        if (this.themeAtCreate != this.store.themeMode()) {
            this.themeAtCreate = this.store.themeMode();
            recreate();
            return;
        }
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

    /** Диалог выбора культуры с переходом сразу в раздел «вредители» или «болезни» справочника. */
    private void pickCulture(final String kind) {
        final java.util.List<Plant> all = Plant.all();
        final String[] names = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            names[i] = all.get(i).name;
        }
        new android.app.AlertDialog.Builder(this)
                .setTitle("pest".equals(kind) ? "🐛 Вредители: выберите культуру" : "🍂 Болезни: выберите культуру")
                .setItems(names, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        DiseaseActivity.show(MainActivity.this, all.get(which).id, kind);
                    }
                })
                .show();
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
            textView3.setText("Прогноз на 7 дней · столбик — температура · синий — осадки (мм) · оранжевая точка — погода мешает работам");
            textView4.setText("Обновлено " + Dates.time(this.weather.fetchedAt) + " · источник: Open-Meteo");
        }
        Planner planner = new Planner(this.store, this.weather);
        ForecastView chart = (ForecastView) findViewById(R.id.forecast_chart);
        if (this.weather != null && this.weather.hasForecast()) {
            chart.setVisibility(View.VISIBLE);
            chart.setCells(ForecastModel.build(this.weather, planner.tasks(21), 7));
        } else {
            chart.setVisibility(View.GONE);
        }
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
        final Runnable refresh = new Runnable() {
            public void run() {
                MainActivity.this.m0lambda$renderTasks$1$bycslgardenerMainActivity();
            }
        };
        final java.util.List<Task> archived = new java.util.ArrayList<>();
        int shown = 0;
        for (Task task : tasks) {
            Calendar at = Dates.at(task.year, task.month, task.day);
            if (at.before(calendar) || at.after(plusDays)) {
                continue;
            }
            if (task.done) {
                archived.add(task);
                continue;
            }
            if (shown < 12) {
                if (this.lastDay == null || Dates.diffDays(this.lastDay, at) != 0) {
                    Ui.section(linearLayout, this, headerFor(at, calendar));
                }
                this.lastDay = at;
                linearLayout.addView(Ui.taskCard(this, task, refresh));
                shown++;
            }
        }
        if (shown == 0 && archived.isEmpty()) {
            linearLayout.addView(Ui.text(this, "На ближайшие 7 дней работ нет. Добавьте растения или расширьте окно в «Календаре».", 14.0f, getResources().getColor(R.color.text_sub), false));
        }
        if (!archived.isEmpty()) {
            final LinearLayout archWrap = new LinearLayout(this);
            archWrap.setOrientation(LinearLayout.VERTICAL);
            final boolean openNow = this.store.archiveOpen();
            final Button archBtn = new Button(this);
            archBtn.setTextSize(14.0f);
            archBtn.setText("🗂 Архив выполненных работ (" + archived.size() + ")" + (openNow ? " — скрыть" : " — показать"));
            archWrap.setVisibility(openNow ? View.VISIBLE : View.GONE);
            if (openNow) {
                for (Task t : archived) {
                    archWrap.addView(Ui.taskCard(this, t, refresh));
                }
            }
            archBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View vv) {
                    boolean nowOpen = archWrap.getVisibility() != View.VISIBLE;
                    MainActivity.this.store.setArchiveOpen(nowOpen);
                    archWrap.setVisibility(nowOpen ? View.VISIBLE : View.GONE);
                    archWrap.removeAllViews();
                    if (nowOpen) {
                        for (Task t2 : archived) {
                            archWrap.addView(Ui.taskCard(MainActivity.this, t2, refresh));
                        }
                    }
                    archBtn.setText("🗂 Архив выполненных работ (" + archived.size() + ")" + (nowOpen ? " — скрыть" : " — показать"));
                }
            });
            linearLayout.addView(archBtn);
            linearLayout.addView(archWrap);
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
