package by.csl.gardener;

/** Одно заболевание из справочника: симптомы, пошаговое лечение, профилактика по сезонам. */
public class Disease {
    public final String id;
    public final String plantId;
    public final String name;
    public final String kind;      // гриб / бактерия / вирус / физиологическое
    public final String symptoms;  // как распознать (для определения по фото)
    public final String[] cure;    // пошаговое лечение
    public final String spring;    // профилактика по сезонам
    public final String summer;
    public final String autumn;
    public final String[] mats;    // id препаратов из каталога
    public final String image;     // имя ресурса dz_* или null

    public Disease(String id, String plantId, String name, String kind, String symptoms,
                   String[] cure, String spring, String summer, String autumn,
                   String[] mats, String image) {
        this.id = id;
        this.plantId = plantId;
        this.name = name;
        this.kind = kind;
        this.symptoms = symptoms;
        this.cure = cure;
        this.spring = spring;
        this.summer = summer;
        this.autumn = autumn;
        this.mats = mats;
        this.image = image;
    }

    public int imageRes() {
        return image == null ? 0 : DiseaseDb.imageRes(image);
    }
}
