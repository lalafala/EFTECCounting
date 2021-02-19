package com.example.efteccounting.activity;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    public static Context context;
    public static String url;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();

    }

}
