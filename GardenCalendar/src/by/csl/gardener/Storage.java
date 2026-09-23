package by.csl.gardener;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class Storage {
    private static final String DONE = "done_prefs";
    private static final String JOURNAL = "journal_prefs";

    private static final String PREFS = "garden_prefs";
    private final SharedPreferences doneP;
    private final SharedPreferences journalP;
    private final SharedPreferences p;

    public Storage(Context context) {
        this.p = context.getSharedPreferences(PREFS, 0);
        this.doneP = context.getSharedPreferences(DONE, 0);
        this.journalP = context.getSharedPreferences(JOURNAL, 0);
    }

    public Set<String> plants() {
        return new HashSet<>(this.p.getStringSet("plants", defaultPlants()));
    }

    public void setPlants(Set<String> set) {
        this.p.edit().putStringSet("plants", new HashSet<>(set)).apply();
    }

    /** Отмеченные пользователем болезни культуры за сезон (для профилактики в следующем). */
    public Set<String> plantDiseases(String plantId) {
        return new HashSet<>(this.p.getStringSet("dz_" + plantId, new HashSet<String>()));
    }

    public void setPlantDiseases(String plantId, Set<String> set) {
        this.p.edit().putStringSet("dz_" + plantId, new HashSet<>(set)).apply();
    }

    private Set<String> defaultPlants() {
        HashSet hashSet = new HashSet();
        hashSet.add("apple");
        hashSet.add("pear");
        hashSet.add("sweet_cherry");
        hashSet.add("cherry");
        hashSet.add("plum");
        hashSet.add("currant_black");
        hashSet.add("gooseberry");
        hashSet.add("raspberry");
        hashSet.add("strawberry");
        hashSet.add("tomato");
        hashSet.add("cucumber");
        hashSet.add("cabbage");
        hashSet.add("potato");
        hashSet.add("greenhouse");
        return hashSet;
    }

    public int plantSize() {
        return this.p.getInt("plant_size", 1);
    }

    public void setPlantSize(int i) {
        this.p.edit().putInt("plant_size", i).apply();
    }

    /** Размер шрифта интерфейса: 0 — мелкий, 1 — обычный, 2 — крупный, 3 — очень крупный. */
    public int fontSize() {
        return this.p.getInt("font_size", 1);
    }

    public void setFontSize(int i) {
        this.p.edit().putInt("font_size", i).apply();
    }

    /** Тема оформления: 0 — как в системе, 1 — светлая, 2 — тёмная. */
    public int themeMode() {
        return this.p.getInt("theme_mode", 0);
    }

    public void setThemeMode(int i) {
        this.p.edit().putInt("theme_mode", i).apply();
    }

    public String diseaseUpdateUrl() {
        return this.p.getString("disease_update_url", "https://csl.by/garden/diseases.json");
    }

    public void setDiseaseUpdateUrl(String str) {
        this.p.edit().putString("disease_update_url", str).apply();
    }

    public String city() {
        return this.p.getString("city", "Минск");
    }

    public double lat() {
        return Double.parseDouble(this.p.getString("lat", "53.9045"));
    }

    public double lon() {
        return Double.parseDouble(this.p.getString("lon", "27.5615"));
    }

    public String locationSource() {
        return this.p.getString("loc_src", "default");
    }

    public void setLocationSource(String str) {
        this.p.edit().putString("loc_src", str).apply();
    }

    public Set<String> varietyGroups(String str) {
        String string = this.p.getString("vars_" + str, "");
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        if (!string.isEmpty()) {
            for (String str2 : string.split(",")) {
                if (!str2.isEmpty()) {
                    linkedHashSet.add(str2);
                }
            }
        }
        if (linkedHashSet.isEmpty()) {
            String string2 = this.p.getString("var_" + str, "");
            if (!string2.isEmpty()) {
                linkedHashSet.add(string2);
            }
        }
        if (linkedHashSet.isEmpty()) {
            linkedHashSet.add("mid");
        }
        return linkedHashSet;
    }

    public void setVarietyGroups(String str, Set<String> set) {
        StringBuilder sb = new StringBuilder();
        String[] strArr = {"early", "mid", "late"};
        for (int i = 0; i < 3; i++) {
            String str2 = strArr[i];
            if (set.contains(str2)) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(str2);
            }
        }
        this.p.edit().putString("vars_" + str, sb.toString()).apply();
    }

    /** Три среза хранилища для резервной копии: [0]=настройки, [1]=отметки, [2]=журнал. */
    @SuppressWarnings("unchecked")
    public java.util.Map<String, ?>[] exportAll() {
        return new java.util.Map[] { this.p.getAll(), this.doneP.getAll(), this.journalP.getAll() };
    }

    public String familyEmail() {
        return this.p.getString("family_email", "");
    }

    public void setFamilyEmail(String value) {
        this.p.edit().putString("family_email", value).apply();
    }

    // ── Семейная синхронизация через сервер ──────────────────────────────

    public String syncServer() {
        String v = this.p.getString("sync_server", "");
        return v.length() == 0 ? SyncClient.DEFAULT_SERVER : v;
    }

    public void setSyncServer(String value) {
        this.p.edit().putString("sync_server", value.trim()).apply();
    }

    /** Есть ли активная учётная запись семьи. */
    public boolean syncLinked() {
        return this.p.getString("sync_token", "").length() > 0;
    }

    public String syncFamily() {
        return this.p.getString("sync_family", "");
    }

    public String syncLogin() {
        return this.p.getString("sync_login", "");
    }

    public String syncToken() {
        return this.p.getString("sync_token", "");
    }

    public void setSyncAccount(String family, String login, String token) {
        this.p.edit().putString("sync_family", family).putString("sync_login", login)
                .putString("sync_token", token).apply();
    }

    public void clearSyncAccount() {
        this.p.edit().putString("sync_family", "").putString("sync_login", "")
                .putString("sync_token", "").apply();
    }

    /** Дата (мс) последнего известного серверного/применённого состояния. */
    public long syncLastTs() {
        try {
            return Long.parseLong(this.p.getString("sync_last_ts", "0"));
        } catch (Exception e) {
            return 0;
        }
    }

    public void setSyncLastTs(long ts) {
        this.p.edit().putString("sync_last_ts", String.valueOf(ts)).apply();
    }

    /** Еженедельный дайджест-уведомление (воскресенье 10:00). По умолчанию включён. */
    public boolean weeklyDigestEnabled() {
        return !"0".equals(this.p.getString("weekly_digest", "1"));
    }

    public void setWeeklyDigest(boolean enabled) {
        this.p.edit().putString("weekly_digest", enabled ? "1" : "0").apply();
    }

    /** Архив выполненных работ на главном экране: раскрыт или свёрнут. По умолчанию свёрнут. */
    public boolean archiveOpen() {
        return "1".equals(this.p.getString("archive_open", "0"));
    }

    public void setArchiveOpen(boolean open) {
        this.p.edit().putString("archive_open", open ? "1" : "0").apply();
    }

    /**
     * Полностью заменяет данные трёх хранилищ содержимым карт (каждая секция сначала очищается).
     * Возвращает число записей, попавших в файл восстановления.
     */
    public int importAll(java.util.Map<String, Object> p, java.util.Map<String, Object> done,
                         java.util.Map<String, Object> journal) {
        return restoreSection(this.p, p) + restoreSection(this.doneP, done)
                + restoreSection(this.journalP, journal);
    }

    @SuppressWarnings("unchecked")
    private static int restoreSection(SharedPreferences prefs, java.util.Map<String, Object> data) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        int n = 0;
        for (java.util.Map.Entry<String, Object> e : data.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Boolean) {
                edit.putBoolean(e.getKey(), (Boolean) v);
            } else if (v instanceof Integer) {
                edit.putInt(e.getKey(), (Integer) v);
            } else if (v instanceof Long) {
                edit.putLong(e.getKey(), (Long) v);
            } else if (v instanceof java.util.Set) {
                edit.putStringSet(e.getKey(), (java.util.Set<String>) v);
            } else if (v != null) {
                edit.putString(e.getKey(), String.valueOf(v));
            }
            n++;
        }
        edit.commit();
        return n;
    }

    public static String groupsLabel(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String str : set) {
            if (sb.length() > 0) {
                sb.append("·");
            }
            sb.append("early".equals(str) ? "ранние" : "late".equals(str) ? "поздние" : "средние");
        }
        return sb.toString();
    }

    public void setLocation(String str, double d, double d2) {
        this.p.edit().putString("city", str).putString("lat", String.valueOf(d)).putString("lon", String.valueOf(d2)).apply();
    }

    public String weatherCache() {
        return this.p.getString("weather_cache", "");
    }

    public void setWeatherCache(String str) {
        this.p.edit().putString("weather_cache", str).apply();
    }

    public boolean notifyEnabled() {
        return this.p.getBoolean("notify", true);
    }

    public void setNotifyEnabled(boolean z) {
        this.p.edit().putBoolean("notify", z).apply();
    }

    public int notifyHour() {
        return this.p.getInt("notify_hour", 8);
    }

    public int notifyMinute() {
        return this.p.getInt("notify_min", 30);
    }

    public void setNotifyTime(int i, int i2) {
        this.p.edit().putInt("notify_hour", i).putInt("notify_min", i2).apply();
    }

    public int leadDays() {
        return this.p.getInt("lead_days", 1);
    }

    public void setLeadDays(int i) {
        this.p.edit().putInt("lead_days", i).apply();
    }

    // ── Режим участка и календарь посещения ──

    /** «permanent» — постоянное проживание; «dacha» — дача на выходные и в отпуск. */
    public String residenceMode() {
        return this.p.getString("residence_mode", "permanent");
    }

    public void setResidenceMode(String mode) {
        this.p.edit().putString("residence_mode", mode).apply();
    }

    /** Отмеченные дни присутствия на участке в формате «yyyy-MM-dd». */
    public Set<String> visitDays() {
        return new HashSet<>(this.p.getStringSet("visit_days", new HashSet<String>()));
    }

    public boolean isVisitDay(int year, int month, int day) {
        return visitDays().contains(visitKey(year, month, day));
    }

    /** Отметить/снять день присутствия в календаре посещения. */
    public void setVisitDay(int year, int month, int day, boolean present) {
        Set<String> set = visitDays();
        if (present) {
            set.add(visitKey(year, month, day));
        } else {
            set.remove(visitKey(year, month, day));
        }
        this.p.edit().putStringSet("visit_days", set).apply();
    }

    // ── Ручной перенос сроков работ ──

    /** Запомнить, что работу перенесли на новую дату (в пределах сезона). */
    public void setShifted(String taskId, int y, int m, int d) {
        this.p.edit().putString("shift_" + taskId, y + "-" + m + "-" + d).apply();
    }

    /** Дата переноса работы или null. */
    public int[] shiftedDate(String taskId) {
        String v = this.p.getString("shift_" + taskId, null);
        if (v == null) {
            return null;
        }
        String[] b = v.split("-");
        try {
            return new int[]{Integer.parseInt(b[0]), Integer.parseInt(b[1]), Integer.parseInt(b[2])};
        } catch (Exception e) {
            return null;
        }
    }

    public void clearShifted(String taskId) {
        this.p.edit().remove("shift_" + taskId).apply();
    }

    /** Снять все переносы (новый сезон). */
    public void clearShifts() {
        SharedPreferences.Editor edit = this.p.edit();
        for (String k : this.p.getAll().keySet()) {
            if (k.startsWith("shift_")) {
                edit.remove(k);
            }
        }
        edit.apply();
    }

    private static String visitKey(int year, int month, int day) {
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    public boolean isDone(String str, int i) {
        return this.doneP.getBoolean(str + "#" + i, false);
    }

    public void setDone(String str, int i, boolean z) {
        this.doneP.edit().putBoolean(str + "#" + i, z).apply();
    }

    public void clearDoneForCurrentYear(int i) {
        SharedPreferences.Editor edit = this.doneP.edit();
        for (String str : this.doneP.getAll().keySet()) {
            if (str.endsWith("#" + i)) {
                edit.remove(str);
            }
        }
        edit.apply();
    }

    public List<String> bought() {
        ArrayList arrayList = new ArrayList();
        try {
            JSONArray jSONArray = new JSONArray(this.p.getString("bought", "[]"));
            for (int i = 0; i < jSONArray.length(); i++) {
                arrayList.add(jSONArray.getString(i));
            }
        } catch (Exception unused) {
        }
        return arrayList;
    }

    public void setBought(List<String> list) {
        JSONArray jSONArray = new JSONArray();
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            jSONArray.put(it.next());
        }
        this.p.edit().putString("bought", jSONArray.toString()).apply();
    }

    /** Урожай по культуре и году: записи «сколько собрали» с единицей (кг/шт/л), в том числе вручную. */
    public void addHarvest(int year, String plantId, double amount, String unit) {
        String key = "harvest_" + year + "_" + plantId;
        JSONArray arr = new JSONArray();
        try {
            arr = new JSONArray(this.p.getString(key, "[]"));
        } catch (Exception unused) {
        }
        JSONObject entry = new JSONObject();
        try {
            entry.put("t", System.currentTimeMillis());
            entry.put("a", amount);
            entry.put("u", unit);
        } catch (Exception unused) {
        }
        arr.put(entry);
        this.p.edit().putString(key, arr.toString()).apply();
    }

    /** Сумма урожая культуры за год в одной единице. */
    public double harvestTotal(int year, String plantId, String unit) {
        try {
            JSONArray arr = new JSONArray(this.p.getString("harvest_" + year + "_" + plantId, "[]"));
            double sum = 0.0d;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject entry = arr.getJSONObject(i);
                if (unit.equals(entry.optString("u"))) {
                    sum += entry.optDouble("a", 0.0d);
                }
            }
            return sum;
        } catch (Exception unused) {
            return 0.0d;
        }
    }

    /** Единицы, в которых записывали урожай культуры за год (в порядке первых записей). */
    public List<String> harvestUnits(int year, String plantId) {
        List<String> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(this.p.getString("harvest_" + year + "_" + plantId, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                String u = arr.getJSONObject(i).optString("u", "");
                if (u.length() > 0 && !out.contains(u)) {
                    out.add(u);
                }
            }
        } catch (Exception unused) {
        }
        return out;
    }

    /** Годы, за которые есть записи урожая (по возрастанию). */
    public List<Integer> harvestYears() {
        java.util.TreeSet<Integer> years = new java.util.TreeSet<>();
        for (String key : this.p.getAll().keySet()) {
            if (key == null || !key.startsWith("harvest_")) {
                continue;
            }
            String rest = key.substring("harvest_".length());
            int cut = rest.indexOf('_');
            if (cut <= 0) {
                continue;
            }
            try {
                years.add(Integer.parseInt(rest.substring(0, cut)));
            } catch (NumberFormatException ignored) {
            }
        }
        return new ArrayList<Integer>(years);
    }

    /** Итог урожая за год в читаемом виде: «20.0 кг» или «20.0 кг, 5.0 шт»; пусто — «». */
    public String harvestSummary(int year, String plantId) {
        StringBuilder sb = new StringBuilder();
        for (String u : harvestUnits(year, plantId)) {
            double total = harvestTotal(year, plantId, u);
            if (total <= 0.0d) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(String.format(java.util.Locale.US, "%.1f %s", Double.valueOf(total), u));
        }
        return sb.toString();
    }

    /** Журнал сада: ровно одна запись на выполненную работу (операция, культура, название, материалы). */
    public void addJournal(String op, String plant, String title, String mats) {
        android.content.SharedPreferences prefs = this.journalP;
        android.content.SharedPreferences.Editor edit = prefs.edit();
        java.util.List<String> all = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            Object v = e.getValue();
            if (v instanceof String) all.add((String) v);
        }
        all.add(Journal.encode(op, plant, title, mats, System.currentTimeMillis()));
        java.util.List<String> kept = Journal.newest(all, Journal.MAX_ENTRIES);
        edit.clear();
        for (String encoded : kept) {
            String[] f = Journal.decode(encoded);
            if (f != null) {
                // ключ уникален даже при двух записях в одну миллисекунду
                edit.putString("j#" + Journal.when(f) + "#" + Long.toHexString(System.nanoTime()), encoded);
            }
        }
        edit.apply();
    }

    /** Журнал сада: закодированные записи, новые сверху. */
    public java.util.List<String> journal(int max) {
        android.content.SharedPreferences prefs = this.journalP;
        java.util.List<String> all = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            Object v = e.getValue();
            if (v instanceof String) all.add((String) v);
        }
        return Journal.newest(all, max);
    }
}
