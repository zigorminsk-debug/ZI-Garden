package by.csl.gardener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/** Экспорт встроенного справочника в web/diseases.json — файл обновлений для размещения на сервере. */
public final class ExportDiseases {
    public static void main(String[] args) throws Exception {
        List<Disease> all = DiseaseDb.all(null);
        String json = DiseaseDb.toJson(all, 1);
        File dir = new File("web");
        dir.mkdirs();
        FileOutputStream out = new FileOutputStream(new File(dir, "diseases.json"));
        out.write(json.getBytes("UTF-8"));
        out.close();
        System.out.println("web/diseases.json: " + all.size() + " записей, " + json.length() + " байт");
    }
}
