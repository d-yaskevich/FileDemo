package com.myfirst.filedemo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirst.filedemo.R;
import com.myfirst.filedemo.adapters.FileAdapter;

import java.io.File;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnItemClickListener {

    public static final String FILE_PATH_KEY = "FILE_PATH_KEY";
    public static final String FILE_URI_KEY = "FILE_URI_KEY";

    private static final int OPEN_FILE_REQUEST_CODE = 8;
    private static final int SELECT_FILE_REQUEST_CODE = 9;

    private FileAdapter filesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesAdapter = new FileAdapter(this, this);

        RecyclerView rvFiles = findViewById(R.id.files_recycler_view);
        rvFiles.setLayoutManager(new LinearLayoutManager(this));
        rvFiles.setAdapter(filesAdapter);

        findViewById(R.id.btn_select_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });

        findViewById(R.id.floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEditFile(null, null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetFiles();
    }

    private void resetFiles() {
        filesAdapter.clear();
        filesAdapter.addFiles(getCacheDir().listFiles());
        filesAdapter.addFiles(getFilesDir().listFiles());
        filesAdapter.addFiles(getExternalCacheDir().listFiles());
        filesAdapter.addFiles(getExternalFilesDir(null).listFiles());
    }

    @Override
    public void onItemClick(File file, Uri uri) {
        addEditFile(file, uri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case OPEN_FILE_REQUEST_CODE:
                case SELECT_FILE_REQUEST_CODE:
                    if (data != null) {
                        Uri uri = data.getData();

                        if (uri != null) {
                            filesAdapter.addReplaceUri(uri);
                        }
                    }
                    break;
            }
        }
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/txt");

        startActivityForResult(intent, SELECT_FILE_REQUEST_CODE);
    }

    private void addEditFile(File file, Uri uri) {
        Intent intent = new Intent(MainActivity.this, FileActivity.class);

        if (file != null) {
            intent.putExtra(FILE_PATH_KEY, file.getAbsolutePath());
        }

        if (uri != null) {
            intent.setData(uri);
        }

        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
    }
}