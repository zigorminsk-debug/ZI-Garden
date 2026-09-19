package android.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Хостовая заглушка: хранит значения в памяти, чтобы тестировать Storage на JVM. */
public class SharedPreferences {

    final Map<String, Object> values = new HashMap<>();

    public String getString(String key, String def) {
        Object v = values.get(key);
        return v == null ? def : (String) v;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key, Set<String> def) {
        Object v = values.get(key);
        return v == null ? def : new HashSet<>((Set<String>) v);
    }

    public int getInt(String key, int def) {
        Object v = values.get(key);
        return v == null ? def : (Integer) v;
    }

    public long getLong(String key, long def) {
        Object v = values.get(key);
        return v == null ? def : (Long) v;
    }

    public boolean getBoolean(String key, boolean def) {
        Object v = values.get(key);
        return v == null ? def : (Boolean) v;
    }

    public Map<String, ?> getAll() {
        return new HashMap<>(values);
    }

    public Editor edit() {
        return new Editor(this);
    }

    public static class Editor {
        private boolean cleared = false;
        private final SharedPreferences prefs;
        private final Map<String, Object> pending = new HashMap<>();
        private final Set<String> removed = new HashSet<>();

        Editor(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        public Editor putString(String key, String v) { pending.put(key, v); return this; }

        public Editor putStringSet(String key, Set<String> v) { pending.put(key, v); return this; }

        public Editor putInt(String key, int v) { pending.put(key, v); return this; }

        public Editor putLong(String key, long v) { pending.put(key, v); return this; }

        public Editor putBoolean(String key, boolean v) { pending.put(key, v); return this; }

        public Editor remove(String key) { removed.add(key); return this; }
        public Editor clear() { cleared = true; return this; }

        public void apply() { commit(); }

        public boolean commit() {
            if (cleared) { prefs.values.clear(); cleared = false; }
            for (String k : removed) prefs.values.remove(k);
            removed.clear();
            prefs.values.putAll(pending);
            pending.clear();
            return true;
        }
    }
}
