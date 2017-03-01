package com.kexin.falock.simple;

import android.app.Application;

import com.kexin.sdk.net.KexinHttp;

/**
 * Created by wyt on 2017/2/8.
 * app context
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        KexinHttp.initialize("app_xbed",
                "https://app.fastboot.net.cn",
//                "http://172.13.31.163:30080",
                getExternalCacheDir().getAbsolutePath(),
                false);
    }

}
