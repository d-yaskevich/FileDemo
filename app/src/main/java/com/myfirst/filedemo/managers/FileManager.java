package com.myfirst.filedemo.managers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.myfirst.filedemo.utils.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

public class FileManager {

    public static WeakReference<FileManager> instance;

    public static synchronized FileManager getInstance(Context context) {
        if (instance == null) instance = new WeakReference<>(new FileManager(context));
        return instance.get();
    }

    private final Context context;

    private FileManager(Context context) {
        this.context = context;
    }

    @Nullable
    public String getFileName(Uri uri) {
        // uri = "file://...", "content://..."
        String fileName = null;

        try (Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }

        return fileName;
    }

    public boolean deleteFile(@NonNull File file) throws SecurityException {
        return file.delete();
    }

    public boolean deleteFile(@NonNull Uri uri) throws FileNotFoundException {
        return DocumentsContract.deleteDocument(context.getContentResolver(), uri);
    }

    public String readContent(@NonNull File file) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream fileInputStream = getFileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        }

        return stringBuilder.toString();
    }

    private FileInputStream getFileInputStream(@NonNull File file) throws FileNotFoundException {
        boolean isCache = isCacheFile(file);
        boolean isInternalFile = isFile(file, Storage.Internal);

        if (isInternalFile && !isCache) {
            return context.openFileInput(file.getName());
        } else {
            return new FileInputStream(file);
        }
    }

    public String readContent(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        }

        return stringBuilder.toString();
    }

    @Nullable
    public File saveFile(@NonNull String name, //"name"
                         @NonNull String suffix, //".txt"
                         @NonNull String contents,
                         @NonNull Storage storage,
                         boolean cache) throws IOException {
        File file;

        if (cache) {
            File dir = getDir(storage, true);
            file = saveTempFile(name, suffix, dir, contents);
        } else {
            String fileName = name + suffix;

            file = saveFile(fileName, contents, storage);
        }

        return file;
    }

    private File saveTempFile(@NonNull String name,
                              @NonNull String suffix,
                              @NonNull File dir,
                              @NonNull String contents) throws IOException {
        File file = File.createTempFile(name, suffix, dir);
        writeFile(file, contents);

        return file;
    }

    @Nullable
    private File saveFile(@NonNull String fileName,
                          @NonNull String contents,
                          @NonNull Storage storage) throws IOException {
        File dir = getDir(storage, false);

        switch (storage) {
            case Internal:
                try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
                    fos.write(contents.getBytes());
                }

                return new File(dir, fileName);
            case External:
                File file = new File(dir, fileName);
                writeFile(file, contents);

                return file;
            case Shared:
                return null;
        }

        return null;
    }

    private void writeFile(@NonNull File file, @NonNull String contents) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(contents.getBytes());
        }
    }

    public File getDir(@NonNull Storage storage, boolean cache) {
        switch (storage) {
            case Internal:
                if (cache) {
                    return context.getCacheDir();
                } else {
                    return context.getFilesDir();
                }
            case External:
                if (cache) {
                    return context.getExternalCacheDir();
                } else {
                    return context.getExternalFilesDir(null);
                }
            case Shared:
                return null;
        }

        return null;
    }

    public String getDirPath(@NonNull Storage storage, boolean cache) {
        return getDir(storage, cache).getAbsolutePath();
    }

    public Storage getFileStorage(File file) {
        String absolutePath = file.getAbsolutePath();

        for (Storage storage : Storage.values()) {
            if (storage == Storage.Shared) continue;

            String dirPath = getDirPath(storage, false);
            String cacheDirPath = getDirPath(storage, true);

            boolean isFile = absolutePath.startsWith(dirPath)
                    || absolutePath.startsWith(cacheDirPath);

            if (isFile) return storage;
        }

        return Storage.Shared;
    }

    public boolean isFile(File file, Storage storage) {
        String absolutePath = file.getAbsolutePath();

        if (storage == Storage.Shared) {
            boolean isInternalFile = isFile(file, Storage.Internal);
            if (isInternalFile) return false;

            boolean isExternalFile = isFile(file, Storage.External);
            return !isExternalFile;
        }

        String dirPath = getDirPath(storage, false);
        String cacheDirPath = getDirPath(storage, true);

        return absolutePath.startsWith(dirPath)
                || absolutePath.startsWith(cacheDirPath);
    }

    public boolean isCacheFile(@NonNull File file) {
        String absolutePath = file.getAbsolutePath();

        String internalCacheDirPath = getDirPath(Storage.Internal, true);
        String externalCacheDirPath = getDirPath(Storage.External, true);

        return absolutePath.startsWith(internalCacheDirPath)
                || absolutePath.startsWith(externalCacheDirPath);
    }


}
