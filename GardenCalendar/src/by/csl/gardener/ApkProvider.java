package by.csl.gardener;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Минимальный провайдер для выдачи скачанного APK установщику.
 * Заменяет androidx FileProvider: приложение собирается только на фреймворке,
 * а file:// URI с targetSdk 24+ запрещён (FileUriExposedException).
 */
public class ApkProvider extends ContentProvider {
    public static final String AUTHORITY = "by.csl.gardener.apk";
    public static final String FILE_NAME = "update.apk";
    public static final String MIME = "application/vnd.android.package-archive";

    public static Uri contentUri() {
        return Uri.parse("content://" + AUTHORITY + "/" + FILE_NAME);
    }

    static File targetFile(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return MIME;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Context context = getContext();
        if (context == null) {
            throw new FileNotFoundException("no context");
        }
        File file = targetFile(context);
        if (!file.isFile()) {
            throw new FileNotFoundException("apk not downloaded yet");
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Установщики спрашивают имя и размер — отвечаем честно.
        Context context = getContext();
        File file = context == null ? null : targetFile(context);
        MatrixCursor cursor = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
        cursor.addRow(new Object[]{"ZI-Garden-update.apk", file == null ? 0L : file.length()});
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Context context = getContext();
        File file = context == null ? null : targetFile(context);
        return file != null && file.delete() ? 1 : 0;
    }
}
