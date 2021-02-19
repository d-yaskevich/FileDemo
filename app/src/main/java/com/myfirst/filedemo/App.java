package com.myfirst.filedemo;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.myfirst.filedemo.managers.FileManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FileManager.getInstance(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // some actions
    }
}
