package com.example.xjl.mydemo.tools;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;

/**
 * Created by xjl on 2018/9/11.
 * 用途：静态获取context
 * 工具类
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override public void onCreate() {
        super.onCreate();
//        Fresco.initialize(this);//Fresco初始化
        MyApplication.mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.mContext;
    }

    public static String getAndroidId() {
        return Settings.Secure.getString(
                getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
