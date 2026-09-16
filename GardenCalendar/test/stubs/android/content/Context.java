package android.content;

import java.util.HashMap;
import java.util.Map;

/** Хостовая заглушка Context: выдаёт in-memory SharedPreferences по имени. */
public class Context {
    public java.io.File getFilesDir() { try { java.io.File d = java.io.File.createTempFile("garden", ""); d.delete(); d.mkdirs(); return d; } catch (Exception e) { return new java.io.File("."); } }

    private final Map<String, SharedPreferences> stores = new HashMap<>();
    private final android.content.res.Resources resources = new android.content.res.Resources();

    public SharedPreferences getSharedPreferences(String name, int mode) {
        SharedPreferences s = stores.get(name);
        if (s == null) {
            s = new SharedPreferences();
            stores.put(name, s);
        }
        return s;
    }

    public android.content.res.Resources getResources() {
        return resources;
    }

    /** Упрощённая копия контекста с переопределённой конфигурацией (хранилища общие). */
    public Context createConfigurationContext(android.content.res.Configuration cfg) {
        Context c = new Context();
        c.stores.putAll(this.stores);
        c.resources.getConfiguration().fontScale = cfg.fontScale;
        return c;
    }
}
