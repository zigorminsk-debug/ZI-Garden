package by.csl.gardener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 📸 Фото-дневник культуры: снимки по сезонам — весна, цветение, урожай.
 * Хранится в памяти приложения; разовый системный выборщик не требует разрешений.
 */
public class PhotosActivity extends Activity {

    private static final int PICK_PHOTO = 4901;

    private String plantId;
    private String plantName;
    private LinearLayout grid;
    private TextView empty;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    public static void show(Context ctx, String plantId) {
        ctx.startActivity(new Intent(ctx, PhotosActivity.class).putExtra("plantId", plantId));
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.plantId = getIntent().getStringExtra("plantId");
        if (this.plantId == null || this.plantId.length() == 0) {
            finish();
            return;
        }
        Plant plant = null;
        for (Plant p : Plant.all()) {
            if (p.id.equals(this.plantId)) {
                plant = p;
                break;
            }
        }
        this.plantName = plant != null ? plant.name : this.plantId;
        setTitle("📸 Фото-дневник: " + this.plantName);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 16.0f));
        scroll.addView(root);
        Ui.setContent(this, scroll);

        int cMain = getResources().getColor(R.color.text_main);
        int cSub = getResources().getColor(R.color.text_sub);

        LinearLayout head = Ui.card(this);
        head.addView(Ui.text(this,
                (plant != null ? plant.icon : "📸") + " " + this.plantName, 15.0f, cMain, true));
        TextView hint = Ui.text(this,
                "Фотографируйте растение по сезонам: пробуждение, цветение, плодоношение, "
                        + "осень. Год спустя фото подскажут, как культура развивается. "
                        + "Тап — посмотреть, удержание — удалить.",
                12.0f, cSub, false);
        hint.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
        head.addView(hint);
        root.addView(head);

        Button add = new Button(this);
        add.setText("➕ Добавить фото из галереи");
        add.setAllCaps(false);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Выберите фото"), PICK_PHOTO);
                } catch (Exception e) {
                    Ui.toast(PhotosActivity.this, "Не нашлось приложения с фотографиями");
                }
            }
        });
        root.addView(add);

        this.empty = Ui.text(this,
                "Пока ни одного фото. Нажмите «➕ Добавить фото» и выберите снимок.",
                13.0f, cSub, false);
        this.empty.setPadding(0, Ui.dp(this, 8.0f), 0, 0);
        root.addView(this.empty);

        this.grid = new LinearLayout(this);
        this.grid.setOrientation(LinearLayout.VERTICAL);
        root.addView(this.grid);

        render();
    }

    /** Перерисовать сетку фото (2 в ряд, старые сверху). */
    private void render() {
        this.grid.removeAllViews();
        List<File> photos = PhotoDiary.photos(this, this.plantId);
        this.empty.setVisibility(photos.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        LinearLayout row = null;
        for (final File photo : photos) {
            if (row == null) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                this.grid.addView(row);
            }
            LinearLayout cell = Ui.card(this);

            ImageView img = new ImageView(this);
            Bitmap bm = decode(photo, 480);
            if (bm != null) {
                img.setImageBitmap(bm);
            }
            img.setAdjustViewBounds(true);
            img.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showPhoto(photo);
                }
            });
            img.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    askDelete(photo);
                    return true;
                }
            });
            cell.addView(img, new LinearLayout.LayoutParams(-1, -2));

            TextView date = Ui.text(this,
                    fmt.format(new Date(PhotoDiary.takenAt(photo))), 11.0f,
                    getResources().getColor(R.color.text_sub), true);
            date.setPadding(0, Ui.dp(this, 4.0f), 0, 0);
            date.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showPhoto(photo);
                }
            });
            date.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    askDelete(photo);
                    return true;
                }
            });
            cell.addView(date, new LinearLayout.LayoutParams(-1, -2));

            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, -2);
            cellParams.weight = 1.0f;
            cellParams.rightMargin = Ui.dp(this, 4.0f);
            row.addView(cell, cellParams);

            if (row.getChildCount() >= 2) {
                row = null;
            }
        }
    }

    /** Развернуть фото на весь экран. */
    private void showPhoto(File photo) {
        Bitmap bm = decode(photo, 1600);
        if (bm == null) {
            Ui.toast(this, "Не удалось открыть фото");
            return;
        }
        ImageView view = new ImageView(this);
        view.setImageBitmap(bm);
        view.setAdjustViewBounds(true);
        new AlertDialog.Builder(this)
                .setTitle("📸 " + this.plantName)
                .setView(view)
                .setPositiveButton("Закрыть", (DialogInterface.OnClickListener) null)
                .show();
    }

    /** Удаление с подтверждением. */
    private void askDelete(final File photo) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить это фото?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (PhotoDiary.delete(photo)) {
                            render();
                            Ui.toast(PhotosActivity.this, "Фото удалено");
                        }
                    }
                })
                .setNegativeButton("Отмена", (DialogInterface.OnClickListener) null)
                .show();
    }

    /** Декодирование с масштабированием под заданный размер. */
    private Bitmap decode(File f, int maxSide) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), bounds);
            int sample = 1;
            while (bounds.outWidth / (sample * 2) >= maxSide
                    && bounds.outHeight / (sample * 2) >= maxSide) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PHOTO || resultCode != RESULT_OK
                || data == null || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File saved = PhotoDiary.add(this, this.plantId, in);
            if (saved != null) {
                render();
                Ui.toast(this, "Фото добавлено в дневник");
            } else {
                Ui.toast(this, "Не удалось сохранить фото");
            }
        } catch (Exception e) {
            Ui.toast(this, "Не удалось открыть выбранное фото");
        }
    }
}
