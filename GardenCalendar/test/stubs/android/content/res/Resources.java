package android.content.res;

/** Хостовая заглушка Resources: отдаёт Configuration для тестов масштаба шрифта. */
public class Resources {

    private final Configuration configuration = new Configuration();

    public Configuration getConfiguration() {
        return configuration;
    }
}
