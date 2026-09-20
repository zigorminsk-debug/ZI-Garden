package by.csl.gardener;

/** Одна стадия развития вредителя: где развивается и какой вред наносит на этой стадии. Фото — своё на каждую стадию. */
public class PestStage {
    public final String title;   // «Яйцо», «Гусеница (личинка)», «Куколка», «Бабочка (имаго)»…
    public final String where;   // где развивается эта стадия
    public final String harm;    // какой вред наносит на этой стадии
    public final String image;   // имя ресурса dzs_* (своё фото стадии) или null — фото ещё в пути

    public PestStage(String title, String where, String harm, String image) {
        this.title = title;
        this.where = where;
        this.harm = harm;
        this.image = image;
    }
}
