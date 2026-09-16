package by.csl.gardener;

public class Rule {
    public final int decadeMask;
    public final MatRef[] mats;
    public final double maxTemp;
    public final double minTemp;
    public final int[] months;
    public final String op;
    public final String plantId;
    public final int priority;
    public final boolean rainBlocks;
    public final boolean scaleByPlant;
    public final double solutionL;
    public final String text;
    public final String title;

    public static int decade(int i) {
        if (i <= 10) {
            return 1;
        }
        return i <= 20 ? 2 : 3;
    }

    public Rule(String str, String str2, String str3, int[] iArr, int i, double d, double d2, boolean z, int i2, String str4, double d3, boolean z2, MatRef[] matRefArr) {
        this.plantId = str;
        this.op = str2;
        this.title = str3;
        this.months = iArr;
        this.decadeMask = i;
        this.minTemp = d;
        this.maxTemp = d2;
        this.rainBlocks = z;
        this.priority = i2;
        this.text = str4;
        this.solutionL = d3;
        this.scaleByPlant = z2;
        this.mats = matRefArr == null ? new MatRef[0] : matRefArr;
    }

    public boolean applies(int i, int i2) {
        boolean z;
        int i3 = 0;
        while (true) {
            int[] iArr = this.months;
            if (i3 >= iArr.length) {
                z = false;
                break;
            }
            if (iArr[i3] == i) {
                z = true;
                break;
            }
            i3++;
        }
        return z && (this.decadeMask & (1 << (i2 - 1))) != 0;
    }

    public String stableId() {
        StringBuilder sb = new StringBuilder(this.plantId);
        sb.append('|');
        sb.append(this.op);
        sb.append('|');
        sb.append(this.title);
        sb.append('|');
        int i = 0;
        while (true) {
            int[] iArr = this.months;
            if (i >= iArr.length) {
                sb.append(this.decadeMask);
                return Integer.toHexString(sb.toString().hashCode());
            }
            sb.append(iArr[i]);
            sb.append(',');
            i++;
        }
    }
}
