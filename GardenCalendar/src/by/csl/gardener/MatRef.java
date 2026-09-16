package by.csl.gardener;

public class MatRef {
    public static final int FIXED = 3;
    public static final int PER_10L = 1;
    public static final int PER_M2 = 2;
    public static final int PER_PLANT = 0;
    public boolean alternative;
    public final String materialId;
    public final int mode;
    public final double rate;

    public MatRef(String str, double d, int i) {
        this.materialId = str;
        this.rate = d;
        this.mode = i;
    }

    public MatRef alt() {
        this.alternative = true;
        return this;
    }
}
