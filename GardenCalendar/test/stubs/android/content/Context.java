package android.content;

import java.util.HashMap;
import java.util.Map;

/** Хостовая заглушка Context: выдаёт in-memory SharedPreferences по имени. */
public class Context {
    public java.io.File getFilesDir() { try { java.io.File d = java.io.File.createTempFile("garden", ""); d.delete(); d.mkdirs(); return d; } catch (Exception e) { return new java.io.File("."); } }

    private final Map<String, SharedPreferences> stores = new HashMap<>();

    public SharedPreferences getSharedPreferences(String name, int mode) {
        SharedPreferences s = stores.get(name);
        if (s == null) {
            s = new SharedPreferences();
            stores.put(name, s);
        }
        return s;
    }
}
