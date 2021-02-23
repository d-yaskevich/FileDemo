package com.myfirst.filedemo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.myfirst.filedemo.R;
import com.myfirst.filedemo.managers.FileManager;
import com.myfirst.filedemo.utils.FileUtils;
import com.myfirst.filedemo.utils.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileActivity extends AppCompatActivity {

    private static final String FILE_SAVED_MSG = "SAVED: ";
    private static final String FILE_DELETED_MSG = "DELETED: ";
    private static final String FILE_NOT_DELETED_MSG = "NOT DELETED: ";
    private static final String FILE_SUFFIX = ".txt";

    public static final int CREATE_FILE_REQUEST_CODE = 5;

    private FileManager fileManager = FileManager.instance.get();

    private File file;
    private Uri uri;

    private String fileName;
    private String fileContents;
    private boolean isCacheFile;
    private Storage fileStorage;

    private EditText etFileName;
    private EditText etFileContents;
    private TextView tvFileExtension;
    private Switch swCacheFile;
    private RadioButton rbInternalStorage;
    private RadioButton rbExternalStorage;
    private RadioButton rbSharedStorage;
    private Button btnSave;
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        String filePath = getIntent().getStringExtra(MainActivity.FILE_PATH_KEY);
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                this.file = file;
                this.fileName = file.getName();
                try {
                    this.fileContents = fileManager.readContent(file);
                } catch (IOException e) {
                    showError(e.getMessage());
                }
                this.isCacheFile = fileManager.isCacheFile(file);
                this.fileStorage = fileManager.getFileStorage(file);
            }
        }
        Uri fileUri = getIntent().getData();
        if (fileUri != null) {
            this.uri = fileUri;
            this.fileName = fileManager.getFileName(fileUri);
            try {
                this.fileContents = fileManager.readContent(fileUri);
            } catch (IOException e) {
                showError(e.getMessage());
            }
            this.isCacheFile = false;
            this.fileStorage = Storage.Shared;
        }

        etFileName = findViewById(R.id.et_file_name);
        etFileName.setText(FileUtils.getFileNameWithoutSuffix(fileName));
        etFileName.addTextChangedListener(textWatcher);

        tvFileExtension = findViewById(R.id.tv_file_extension);
        String suffix = FileUtils.getFileSuffix(fileName);
        if (suffix == null || suffix.isEmpty()) {
            suffix = FILE_SUFFIX;
        }
        tvFileExtension.setText(suffix);

        etFileContents = findViewById(R.id.et_file_contents);
        etFileContents.setText(fileContents);
        etFileContents.addTextChangedListener(textWatcher);

        swCacheFile = findViewById(R.id.sw_cache_file);
        swCacheFile.setChecked(isCacheFile);
        swCacheFile.setOnCheckedChangeListener(swCacheFileListener);

        rbInternalStorage = findViewById(R.id.rb_internal_storage);
        rbExternalStorage = findViewById(R.id.rb_external_storage);
        rbSharedStorage = findViewById(R.id.rb_shared_storage);

        rbInternalStorage.setChecked(fileStorage == Storage.Internal);
        rbExternalStorage.setChecked(fileStorage == Storage.External);
        rbSharedStorage.setChecked(fileStorage == Storage.Shared);

        btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setEnabled(file != null || uri != null);

        btnSave = findViewById(R.id.btn_save);
        btnSave.setEnabled(false);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateSaveEnabled();
        }
    };

    private final CompoundButton.OnCheckedChangeListener swCacheFileListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            rbSharedStorage.setEnabled(!isChecked);
            updateSaveEnabled();
        }
    };

    public void onRadioButtonClicked(View view) {
        updateSaveEnabled();
    }

    public void onBtnClick(View view) {
        switch (view.getId()) {
            case R.id.btn_delete:
                if (deleteFileIfExisted()) {
                    deleteFileIfExisted();
                    setOkResult();
                }
                break;
            case R.id.btn_save:
                deleteFileIfExisted();
                saveFile();
                setOkResult();
                break;
        }
    }

    private void updateSaveEnabled() {
        btnSave.setEnabled(isFileChanged());
    }

    private boolean isFileChanged() {
        boolean changed = false;

        String fileName = etFileName.getText().toString();
        String fileContents = etFileContents.getText().toString();

        if (file != null || uri != null) {

            if (!fileName.equals(this.fileName)) {
                changed = true;
            }

            if (!fileContents.equals(this.fileContents)) {
                changed = true;
            }

            if (isCacheFile != swCacheFile.isChecked()) {
                changed = true;
            }

            if (fileStorage == Storage.Internal && !rbInternalStorage.isChecked()) {
                changed = true;
            }

            if (fileStorage == Storage.External && !rbExternalStorage.isChecked()) {
                changed = true;
            }

            if (fileStorage == Storage.Shared && !rbSharedStorage.isChecked()) {
                changed = true;
            }

        } else {

            changed = !fileName.isEmpty();

            if (!rbInternalStorage.isChecked()
                    && !rbExternalStorage.isChecked()
                    && (!rbSharedStorage.isEnabled() || !rbSharedStorage.isChecked())) {
                changed = false;
            }
        }

        return changed;
    }

    private void saveFile() {
        String name = etFileName.getText().toString();
        String suffix = tvFileExtension.getText().toString();
        fileContents = etFileContents.getText().toString();

        if (rbInternalStorage.isChecked()) {
            fileStorage = Storage.Internal;
        } else if (rbExternalStorage.isChecked()) {
            fileStorage = Storage.External;
        } else {
            fileStorage = Storage.Shared;
        }

        if (fileStorage == Storage.Shared) {
            saveSharedFile(name, suffix);
            return;
        }

        boolean isCacheFile = swCacheFile.isChecked();

        try {
            file = fileManager.saveFile(name, suffix, fileContents, fileStorage, isCacheFile);
            if (file != null) {
                fileName = file.getName();
                showInfo(FILE_SAVED_MSG + fileName);
            }
        } catch (IOException e) {
            showError(e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean deleteFileIfExisted() {
        try {
            if ((file != null && fileManager.deleteFile(file))
                    || (uri != null && fileManager.deleteFile(uri))) {
                showInfo(FILE_DELETED_MSG + fileName);

                file = null;
                uri = null;

                return true;
            } else {
                showError(FILE_NOT_DELETED_MSG + fileName);
            }
        } catch (FileNotFoundException e) {
            showError(e.getMessage());
        }

        return false;
    }

    private void saveSharedFile(String name, String suffix) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/" + suffix.substring(1));
        intent.putExtra(Intent.EXTRA_TITLE, name + suffix);

        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CREATE_FILE_REQUEST_CODE) {
                if (data != null) {
                    uri = data.getData();
                    fileName = fileManager.getFileName(uri);

                    showInfo(FILE_SAVED_MSG + fileName);

                    setOkResult();
                }
            }
        }
    }

    private void setOkResult() {
        Intent intent = null;

        if (file != null) {
            intent = new Intent();
            intent.putExtra(MainActivity.FILE_PATH_KEY, file.getAbsolutePath());
        }
        if (uri != null) {
            intent = new Intent();
            intent.putExtra(MainActivity.FILE_URI_KEY, uri);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void showInfo(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.root_view), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}