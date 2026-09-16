package by.csl.gardener;

import android.content.Context;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/** База справочника болезней: встроенный каталог + обновления из интернета. */
public final class DiseaseDb {
    private DiseaseDb() {}

    /** Компактный конструктор записи (используется файлами данных). */
    public static Disease d(String id, String plantId, String name, String kind, String symptoms,
                            String spring, String summer, String autumn, String image,
                            String[] mats, String... cure) {
        return new Disease(id, plantId, name, kind, symptoms, cure, spring, summer, autumn, mats, image);
    }

    private static List<Disease> bundled;

    private static synchronized List<Disease> bundled() {
        if (bundled == null) {
            List<Disease> list = new ArrayList<>();
            DiseaseDataA.fill(list);
            DiseaseDataB.fill(list);
            DiseaseDataC.fill(list);
            DiseaseDataPests.fill(list);
            bundled = list;
        }
        return bundled;
    }

    /** Все болезни: встроенные перекрыты/дополнены свежими из интернета. */
    public static synchronized List<Disease> all(Context ctx) {
        Map<String, Disease> map = new LinkedHashMap<>();
        for (Disease dz : bundled()) map.put(dz.id, dz);
        for (Disease dz : RemoteDiseases.cached(ctx)) map.put(dz.id, dz);
        return new ArrayList<>(map.values());
    }

    /** Болезни конкретной культуры. */
    public static List<Disease> forPlant(Context ctx, String plantId) {
        List<Disease> out = new ArrayList<>();
        for (Disease dz : all(ctx)) {
            if (plantId.equals(dz.plantId)) out.add(dz);
        }
        return out;
    }

    /** Экспорт в JSON — формат файла обновлений для размещения на сервере. */
    public static String toJson(List<Disease> list, int version) {
        try {
            JSONObject root = new JSONObject();
            root.put("version", version);
            root.put("app", "ZI Garden");
            JSONArray arr = new JSONArray();
            for (Disease dz : list) {
                JSONObject o = new JSONObject();
                o.put("id", dz.id);
                o.put("plant", dz.plantId);
                o.put("name", dz.name);
                o.put("kind", dz.kind);
                o.put("symptoms", dz.symptoms);
                JSONArray cure = new JSONArray();
                for (String step : dz.cure) cure.put(step);
                o.put("cure", cure);
                JSONObject prev = new JSONObject();
                prev.put("spring", dz.spring);
                prev.put("summer", dz.summer);
                prev.put("autumn", dz.autumn);
                o.put("prevention", prev);
                JSONArray mats = new JSONArray();
                for (String m : dz.mats) mats.put(m);
                o.put("mats", mats);
                if (dz.image != null) o.put("image", dz.image);
                arr.put(o);
            }
            root.put("diseases", arr);
            return root.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    /** Разбор файла обновлений. */
    public static List<Disease> fromJson(String json) {
        List<Disease> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONObject(json).optJSONArray("diseases");
            if (arr == null) return out;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String id = o.optString("id", "");
                String plant = o.optString("plant", "");
                if (id.length() == 0 || plant.length() == 0) continue;
                JSONArray cureArr = o.optJSONArray("cure");
                String[] cure = new String[cureArr == null ? 0 : cureArr.length()];
                for (int k = 0; k < cure.length; k++) cure[k] = cureArr.optString(k, "");
                JSONObject prev = o.optJSONObject("prevention");
                String spring = prev == null ? "" : prev.optString("spring", "");
                String summer = prev == null ? "" : prev.optString("summer", "");
                String autumn = prev == null ? "" : prev.optString("autumn", "");
                JSONArray matsArr = o.optJSONArray("mats");
                String[] mats = new String[matsArr == null ? 0 : matsArr.length()];
                for (int k = 0; k < mats.length; k++) mats[k] = matsArr.optString(k, "");
                String image = o.optString("image", "");
                out.add(new Disease(id, plant, o.optString("name", ""), o.optString("kind", ""),
                        o.optString("symptoms", ""), cure, spring, summer, autumn, mats,
                        image.length() == 0 ? null : image));
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    /** Имя ресурса фото → R.drawable.dz_* (рефлексия: новые фото подхватываются без правки кода). */
    public static int imageRes(String name) {
        if (name == null) return 0;
        try {
            return R.drawable.class.getField(name).getInt(null);
        } catch (Exception e) {
            return 0;
        }
    }
}
