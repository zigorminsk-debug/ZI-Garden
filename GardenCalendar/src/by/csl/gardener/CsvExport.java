package by.csl.gardener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Экспорт данных сада в CSV. Разделитель «;» — файл открывается в Excel
 * (русская локаль) без настройки импорта. Доступно из журнала сада.
 */
public final class CsvExport {

    private CsvExport() {
    }

    /** Ячейка CSV: экранирование кавычек, разделителей и переносов строк. */
    static String cell(String value) {
        String s = value == null ? "" : value;
        if (s.indexOf('"') >= 0 || s.indexOf(';') >= 0 || s.indexOf(',') >= 0 || s.indexOf('\n') >= 0) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    /** Имя культуры по id (или сам id, если культура неизвестна). */
    static String plantName(String plantId) {
        if (plantId == null || plantId.length() == 0) {
            return "";
        }
        for (Plant p : Plant.all()) {
            if (p.id.equals(plantId)) {
                return p.name;
            }
        }
        return plantId;
    }

    /** Журнал выполненных работ: Дата;Культура;Операция;Работа;Материалы (старые сверху). */
    public static String journal(Storage store, int max) {
        StringBuilder sb = new StringBuilder("Дата;Культура;Операция;Работа;Материалы\n");
        List<String> rows = store.journal(max);
        for (int i = rows.size() - 1; i >= 0; i--) {
            String[] f = Journal.decode(rows.get(i));
            if (f == null) {
                continue;
            }
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Journal.when(f));
            String date = String.format(Locale.US, "%04d-%02d-%02d",
                    Integer.valueOf(c.get(Calendar.YEAR)),
                    Integer.valueOf(c.get(Calendar.MONTH) + 1),
                    Integer.valueOf(c.get(Calendar.DAY_OF_MONTH)));
            sb.append(cell(date)).append(';')
                    .append(cell(plantName(f[Journal.F_PLANT]))).append(';')
                    .append(cell(Operation.label(f[Journal.F_OP]))).append(';')
                    .append(cell(f[Journal.F_TITLE])).append(';')
                    .append(cell(f[Journal.F_MATS])).append('\n');
        }
        return sb.toString();
    }

    /** Урожай по годам и культурам: Год;Культура;Единица;Количество. */
    public static String harvest(Storage store) {
        StringBuilder sb = new StringBuilder("Год;Культура;Единица;Количество\n");
        for (Integer year : store.harvestYears()) {
            for (Plant plant : Plant.all()) {
                for (String unit : store.harvestUnits(year, plant.id)) {
                    double total = store.harvestTotal(year, plant.id, unit);
                    if (total <= 0.0d) {
                        continue;
                    }
                    sb.append(cell(String.valueOf(year))).append(';')
                            .append(cell(plant.name)).append(';')
                            .append(cell(unit)).append(';')
                            .append(cell(String.format(Locale.US, "%.1f", Double.valueOf(total)))).append('\n');
                }
            }
        }
        return sb.toString();
    }
}
