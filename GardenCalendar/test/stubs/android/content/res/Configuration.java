package android.content.res;

/** Хостовая заглушка Configuration: нужен только fontScale для тестов масштаба шрифта. */
public class Configuration {

    public float fontScale = 1.0f;

    public Configuration() {
    }

    public Configuration(Configuration other) {
        this.fontScale = other == null ? 1.0f : other.fontScale;
    }
}
