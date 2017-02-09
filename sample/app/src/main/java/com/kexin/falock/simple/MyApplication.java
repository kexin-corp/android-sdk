package com.kexin.falock.simple;

import android.app.Application;

import com.kexin.sdk.net.KexinNet;

/**
 * Created by wyt on 2017/2/8.
 * app context
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        KexinNet.initialize("app_xbed", "http://app.fastboot.net.cn", "http://120.24.182.70:9000");
        KexinNet.setDebug(true);
    }
}
