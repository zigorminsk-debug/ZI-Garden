package by.csl.gardener;

import android.app.Activity;
import android.os.Bundle;

/**
 * Приёмник выбора фото культуры: открывает системный файловый диалог (SAF, image/*),
 * сохраняет выбранное в PlantPhotos для plantId, ожидающего в PlantPhotos.pendingPlantId.
 */
public final class PhotoPickActivity extends Activity {
    private static final int REQ_PICK = 71;

    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        String plantId = PlantPhotos.pendingPlantId;
        if (plantId == null) {
            Ui.toast(this, "Сначала откройте карточку культуры и нажмите «фото»");
            finish();
            return;
        }
        android.content.Intent pick = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory("android.intent.category.OPENABLE");
        pick.setType("image/*");
        try {
            startActivityForResult(pick, REQ_PICK);
        } catch (Exception e) {
            Ui.toast(this, "Файловый диалог недоступен на этом устройстве");
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String plantId = PlantPhotos.pendingPlantId;
        PlantPhotos.pendingPlantId = null;
        if (requestCode == REQ_PICK && resultCode == RESULT_OK && data != null
                && data.getData() != null && plantId != null) {
            final String id = plantId;
            final android.net.Uri uri = data.getData();
            Ui.toast(this, "Сохраняем фото…");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean ok = PlantPhotos.saveFromUri(PhotoPickActivity.this, id, uri);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Ui.toast(PhotoPickActivity.this, ok
                                    ? "✅ Фото культуры сохранено — откройте карточку ещё раз"
                                    : "Не получилось сохранить фото — попробуйте другое");
                            finish();
                        }
                    });
                }
            }, "photo-save").start();
            return;
        }
        finish();
    }
}
