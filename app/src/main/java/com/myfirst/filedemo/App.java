package com.myfirst.filedemo;

import android.app.Application;

import com.myfirst.filedemo.managers.FileManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FileManager.getInstance(this);
    }
}
