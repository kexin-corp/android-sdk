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
                getExternalCacheDir().getAbsolutePath(),
                false);
    }

}
