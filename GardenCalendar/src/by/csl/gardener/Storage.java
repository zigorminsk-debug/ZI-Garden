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

public class Storage {
    private static final String DONE = "done_prefs";
    private static final String PREFS = "garden_prefs";
    private final SharedPreferences doneP;
    private final SharedPreferences p;

    public Storage(Context context) {
        this.p = context.getSharedPreferences(PREFS, 0);
        this.doneP = context.getSharedPreferences(DONE, 0);
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
}
