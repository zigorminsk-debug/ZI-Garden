package by.csl.gardener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

public class Weather {
    public static final int WORK_FROM = 8;
    public static final int WORK_TO = 20;
    public long fetchedAt;
    public double lat;
    public double lon;
    public List<Day> days = new ArrayList();
    public double currentTemp = Double.NaN;
    public int currentCode = -1;
    public String cityName = "";

    public static String iconFor(int i) {
        // WMO-коды: 80–82 — ливневый ДОЖДЬ (🌧️), 85–86 — снежный ливень (🌨️); раньше 80–82 ошибочно показывали снег
        return i < 0 ? "❔" : i == 0 ? "☀️" : i <= 2 ? "⛅" : i == 3 ? "☁️" : (i == 45 || i == 48) ? "🌫️" : (i < 51 || i > 57) ? (i < 61 || i > 67) ? (i < 71 || i > 77) ? (i < 80 || i > 82) ? (i == 85 || i == 86) ? "🌨️" : i >= 95 ? "⛈️" : "🌤️" : "🌧️" : "🌨️" : "🌧️" : "🌦️";
    }

    public static String textFor(int i) {
        return i < 0 ? "нет данных" : i == 0 ? "ясно" : i <= 2 ? "переменная облачность" : i == 3 ? "пасмурно" : (i == 45 || i == 48) ? "туман" : (i < 51 || i > 57) ? (i < 61 || i > 67) ? (i < 71 || i > 77) ? (i < 80 || i > 82) ? (i == 85 || i == 86) ? "снежный ливень" : i >= 95 ? "гроза" : "облачно" : "ливень" : "снег" : "дождь" : "морось";
    }

    public static class Day {
        public int code;
        public int day;
        public int month;
        public double precipMm;
        public double precipProb;
        public double humidity = Double.NaN;
        public double soilMoist = Double.NaN;
        public double soilT = Double.NaN;
        public double tMax;
        public double tMin;
        public double windMax;
        public double workPrecipMm;
        public double workPrecipProb;
        public int year;

        public String icon() {
            return Weather.iconFor(this.code);
        }

        public String summary() {
            return String.format(Locale.US, "%.0f…%.0f °C, %s", Double.valueOf(this.tMin), Double.valueOf(this.tMax), Weather.textFor(this.code));
        }

        public boolean rainExpected() {
            double d = this.workPrecipMm;
            if (d <= 0.0d) {
                d = this.precipMm;
            }
            double d2 = this.workPrecipProb;
            if (d2 <= 0.0d) {
                d2 = this.precipProb;
            }
            return d >= 1.0d || d2 >= 60.0d;
        }
    }

    public Day dayFor(int i, int i2, int i3) {
        for (Day day : this.days) {
            if (day.year == i && day.month == i2 && day.day == i3) {
                return day;
            }
        }
        return null;
    }

    public boolean hasForecast() {
        return !this.days.isEmpty();
    }

    public static String buildUrl(double d, double d2) {
        return "https://api.open-meteo.com/v1/forecast?latitude=" + d + "&longitude=" + d2 + "&current=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,wind_speed_10m_max,relative_humidity_2m_max,soil_moisture_0_to_7cm_max&hourly=temperature_2m,precipitation,precipitation_probability,soil_temperature_6cm&forecast_days=10&timezone=auto";
    }

    public static Weather fetch(double d, double d2, String str) throws Exception {
        String str2;
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(buildUrl(d, d2)).openConnection();
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(20000);
        httpURLConnection.setRequestProperty("User-Agent", "GardenCalendar/1.1 (csl.by)");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode != 200) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    sb.append(readLine);
                }
                bufferedReader.close();
            } catch (Exception unused) {
            }
            StringBuilder sb2 = new StringBuilder("HTTP ");
            sb2.append(responseCode);
            if (sb.length() > 0) {
                str2 = " — " + ((Object) sb);
            } else {
                str2 = "";
            }
            sb2.append(str2);
            throw new Exception(sb2.toString());
        }
        StringBuilder sb3 = new StringBuilder();
        BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
        while (true) {
            String readLine2 = bufferedReader2.readLine();
            if (readLine2 == null) {
                bufferedReader2.close();
                httpURLConnection.disconnect();
                return parse(sb3.toString(), d, d2, str);
            }
            sb3.append(readLine2);
        }
    }

    public static Weather parse(String str, double d, double d2, String str2) throws Exception {
        Weather weather = new Weather();
        weather.lat = d;
        weather.lon = d2;
        weather.cityName = str2;
        weather.fetchedAt = System.currentTimeMillis();
        JSONObject jSONObject = new JSONObject(str);
        if (jSONObject.has("current")) {
            JSONObject jSONObject2 = jSONObject.getJSONObject("current");
            weather.currentTemp = jSONObject2.optDouble("temperature_2m", Double.NaN);
            weather.currentCode = jSONObject2.optInt("weather_code", -1);
        }
        JSONObject jSONObject3 = jSONObject.getJSONObject("daily");
        JSONArray jSONArray = jSONObject3.getJSONArray("time");
        JSONArray jSONArray2 = jSONObject3.getJSONArray("temperature_2m_max");
        JSONArray jSONArray3 = jSONObject3.getJSONArray("temperature_2m_min");
        JSONArray jSONArray4 = jSONObject3.getJSONArray("precipitation_sum");
        JSONArray optJSONArray = jSONObject3.optJSONArray("precipitation_probability_max");
        JSONArray optJSONArray2 = jSONObject3.optJSONArray("wind_speed_10m_max");
        JSONArray optJSONArray3 = jSONObject3.optJSONArray("weather_code");
        JSONArray arrHum = jSONObject3.optJSONArray("relative_humidity_2m_max");
        JSONArray arrSoil = jSONObject3.optJSONArray("soil_moisture_0_to_7cm_max");
        char c = 0;
        int i = 0;
        while (i < jSONArray.length()) {
            int[] ymd = ymd(jSONArray.getString(i));
            Day day = new Day();
            day.year = ymd[c];
            day.month = ymd[1];
            day.day = ymd[2];
            day.tMax = jSONArray2.optDouble(i);
            day.tMin = jSONArray3.optDouble(i);
            day.precipMm = jSONArray4.optDouble(i);
            day.precipProb = optJSONArray == null ? 0.0d : optJSONArray.optDouble(i);
            day.windMax = optJSONArray2 != null ? optJSONArray2.optDouble(i) : 0.0d;
            day.code = optJSONArray3 == null ? -1 : optJSONArray3.optInt(i);
            day.humidity = arrHum == null ? Double.NaN : arrHum.optDouble(i, Double.NaN);
            day.soilMoist = arrSoil == null ? Double.NaN : arrSoil.optDouble(i, Double.NaN);
            weather.days.add(day);
            i++;
            c = 0;
        }
        applyHourly(weather, jSONObject.optJSONObject("hourly"));
        return weather;
    }

    private static void applyHourly(Weather weather, JSONObject jSONObject) throws Exception {
        int i;
        if (jSONObject == null) {
            return;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("time");
        JSONArray optJSONArray2 = jSONObject.optJSONArray("precipitation");
        JSONArray optJSONArray3 = jSONObject.optJSONArray("precipitation_probability");
        JSONArray optJSONArray4 = jSONObject.optJSONArray("soil_temperature_6cm");
        if (optJSONArray == null) {
            return;
        }
        for (int i2 = 0; i2 < optJSONArray.length(); i2++) {
            String[] split = optJSONArray.getString(i2).split("T");
            int[] ymd = ymd(split[0]);
            Day dayFor = weather.dayFor(ymd[0], ymd[1], ymd[2]);
            if (dayFor != null) {
                try {
                    i = Integer.parseInt(split[1].substring(0, 2));
                } catch (Exception unused) {
                    i = 12;
                }
                if (optJSONArray4 != null && Double.isNaN(dayFor.soilT)) {
                    dayFor.soilT = optJSONArray4.optDouble(i2);
                }
                if (i >= 8 && i < 20) {
                    if (optJSONArray2 != null) {
                        dayFor.workPrecipMm += Math.max(0.0d, optJSONArray2.optDouble(i2));
                    }
                    if (optJSONArray3 != null) {
                        dayFor.workPrecipProb = Math.max(dayFor.workPrecipProb, optJSONArray3.optDouble(i2));
                    }
                }
            }
        }
    }

    private static int[] ymd(String str) {
        String[] split = str.split("-");
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
    }

    public String toJson() {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("lat", this.lat);
            jSONObject.put("lon", this.lon);
            jSONObject.put("city", this.cityName);
            jSONObject.put("fetchedAt", this.fetchedAt);
            jSONObject.put("currentTemp", Double.isNaN(this.currentTemp) ? JSONObject.NULL : Double.valueOf(this.currentTemp));
            jSONObject.put("currentCode", this.currentCode);
            JSONArray jSONArray = new JSONArray();
            for (Day day : this.days) {
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("y", day.year);
                jSONObject2.put("m", day.month);
                jSONObject2.put("d", day.day);
                jSONObject2.put("tmin", day.tMin);
                jSONObject2.put("tmax", day.tMax);
                jSONObject2.put("p", day.precipMm);
                jSONObject2.put("pp", day.precipProb);
                jSONObject2.put("wp", day.workPrecipMm);
                jSONObject2.put("wpp", day.workPrecipProb);
                jSONObject2.put("w", day.windMax);
                jSONObject2.put("s", Double.isNaN(day.soilT) ? JSONObject.NULL : Double.valueOf(day.soilT));
                jSONObject2.put("h", Double.isNaN(day.humidity) ? JSONObject.NULL : Double.valueOf(day.humidity));
                jSONObject2.put("sm", Double.isNaN(day.soilMoist) ? JSONObject.NULL : Double.valueOf(day.soilMoist));
                jSONObject2.put("c", day.code);
                jSONArray.put(jSONObject2);
            }
            jSONObject.put("days", jSONArray);
            return jSONObject.toString();
        } catch (Exception unused) {
            return "";
        }
    }

    public static Weather fromJson(String str) {
        if (str != null && str.length() != 0) {
            try {
                JSONObject jSONObject = new JSONObject(str);
                Weather weather = new Weather();
                weather.lat = jSONObject.optDouble("lat");
                weather.lon = jSONObject.optDouble("lon");
                weather.cityName = jSONObject.optString("city", "");
                weather.fetchedAt = jSONObject.optLong("fetchedAt");
                weather.currentTemp = jSONObject.isNull("currentTemp") ? Double.NaN : jSONObject.optDouble("currentTemp", Double.NaN);
                weather.currentCode = jSONObject.optInt("currentCode", -1);
                JSONArray jSONArray = jSONObject.getJSONArray("days");
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                    Day day = new Day();
                    day.year = jSONObject2.getInt("y");
                    day.month = jSONObject2.getInt("m");
                    day.day = jSONObject2.getInt("d");
                    day.tMin = jSONObject2.optDouble("tmin");
                    day.tMax = jSONObject2.optDouble("tmax");
                    day.precipMm = jSONObject2.optDouble("p");
                    day.precipProb = jSONObject2.optDouble("pp");
                    day.workPrecipMm = jSONObject2.optDouble("wp");
                    day.workPrecipProb = jSONObject2.optDouble("wpp");
                    day.windMax = jSONObject2.optDouble("w");
                    day.soilT = jSONObject2.isNull("s") ? Double.NaN : jSONObject2.optDouble("s");
                    day.humidity = jSONObject2.isNull("h") ? Double.NaN : jSONObject2.optDouble("h");
                    day.soilMoist = jSONObject2.isNull("sm") ? Double.NaN : jSONObject2.optDouble("sm");
                    day.code = jSONObject2.optInt("c", -1);
                    weather.days.add(day);
                }
                return weather;
            } catch (Exception unused) {
            }
        }
        return null;
    }
}
