package com.aiwinn.carbranddect;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.aiwinn.adv.library.AdSdkManager;
import com.aiwinn.adv.library.SettingManager;
import com.aiwinn.carbranddect.utils.LogUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

/**
 * Created by User on 2018/1/4.
 */

public class App extends Application {
    public static boolean isInit;
    public static SharedPreferences sp;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "20abbf813d", true);
        OkGo.getInstance().init(this);
        initHttp();
        authorizationInit(this);
        context = this;
        sp = getSharedPreferences("CarBrand_SP", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkStoragePermission()) {
                Log.e("App", "有权限 初始化");
                CarBrandManager.init(getApplicationContext(), 0, new InitListener() {
                    @Override
                    public void succ() {
                        LogUtils.e("CarBrandManager init succ ");
                    }

                    @Override
                    public void fail(int code, String msg) {
                        LogUtils.e("CarBrandManager init fail code is " + code + "  msg is " + msg);
                    }
                });
                isInit = true;
            }
        } else {
            CarBrandManager.init(getApplicationContext(), 0, new InitListener() {
                @Override
                public void succ() {
                    LogUtils.e("CarBrandManager init succ ");
                }

                @Override
                public void fail(int code, String msg) {
                    LogUtils.e("CarBrandManager init fail code is " + code + "  msg is " + msg);
                }
            });
            isInit = true;
        }
    }

    private void initHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
//log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
//log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);

        //全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
//全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
//全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);

        OkGo.getInstance().init(this)                       //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3);                             //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0

    }

    private void authorizationInit(Context context) {
        //初始化授权包接口对象, 需要在算法初始化之前
        AdSdkManager.setServiceAddress("http://sdk.ahytwin.com/router");
        AdSdkManager.setServiceFindAddress("http://url.ai-winn.com/router",
                "http://url.aiwinwin.com/router",
                "http://url.ai2winn.com/router");
        AdSdkManager.setAddFindAddress("http://sh.ahyt-win.com/router");
        AdSdkManager.setUseVerifyLicense(true);
        AdSdkManager.setAdvActiveOnly(true);
        AdSdkManager.setFacelockSdk(false);
        AdSdkManager.init(context);
        AdSdkManager.setDebugEnabled(SettingManager.DEBUG);
    }

    public boolean checkStoragePermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
