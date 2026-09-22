package by.csl.gardener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Данные погодной полосы на главной: до 7 дней с температурой, осадками и метками «погода мешает работам». */
final class ForecastModel {

    private ForecastModel() {
    }

    /** Один день полосы. */
    static class Cell {
        final String weekday;
        final int year;
        final int month;
        final int day;
        final String icon;
        final double tMin;
        final double tMax;
        final double precipMm;
        final boolean warn;

        Cell(String weekday, int year, int month, int day, String icon,
                double tMin, double tMax, double precipMm, boolean warn) {
            this.weekday = weekday;
            this.year = year;
            this.month = month;
            this.day = day;
            this.icon = icon;
            this.tMin = tMin;
            this.tMax = tMax;
            this.precipMm = precipMm;
            this.warn = warn;
        }
    }

    /**
     * Ячейки по дням прогноза (не больше count). День помечается, если на его дату
     * есть задача, отложенная по погоде (weatherState: дождь/холод/жара).
     */
    static List<Cell> build(Weather weather, List<Task> tasks, int count) {
        List<Cell> out = new ArrayList<>();
        if (weather == null || weather.days == null) {
            return out;
        }
        HashSet<String> warnDates = new HashSet<>();
        if (tasks != null) {
            for (Task t : tasks) {
                if (t != null && t.weatherState >= 1 && t.weatherState <= 3) {
                    warnDates.add(t.year + "-" + t.month + "-" + t.day);
                }
            }
        }
        int n = Math.min(count, weather.days.size());
        for (int i = 0; i < n; i++) {
            Weather.Day d = weather.days.get(i);
            out.add(new Cell(
                    Dates.weekdayShort(d.year, d.month, d.day),
                    d.year, d.month, d.day,
                    d.icon(),
                    d.tMin, d.tMax, d.precipMm,
                    warnDates.contains(d.year + "-" + d.month + "-" + d.day)));
        }
        return out;
    }

    /** Диапазон температур недели [мин, макс] — для высоты столбиков; пусто → {0, 1}. */
    static double[] tempRange(List<Cell> cells) {
        double lo = Double.MAX_VALUE;
        double hi = -Double.MAX_VALUE;
        for (Cell c : cells) {
            lo = Math.min(lo, c.tMin);
            hi = Math.max(hi, c.tMax);
        }
        if (lo > hi) {
            return new double[]{0.0d, 1.0d};
        }
        if (hi - lo < 6.0d) {
            hi = lo + 6.0d; // не даём столбикам «схлопнуться»
        }
        return new double[]{lo, hi};
    }

    /** Масштаб осадков: максимум недели, но не меньше 5 мм. */
    static double precipScale(List<Cell> cells) {
        double m = 0.0d;
        for (Cell c : cells) {
            m = Math.max(m, c.precipMm);
        }
        return Math.max(5.0d, m);
    }
}
