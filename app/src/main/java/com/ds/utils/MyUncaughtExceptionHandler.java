package com.ds.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aiwinn.carbranddect.App;
import com.ds.view.MainActivity;

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private App myApplication;
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

    public MyUncaughtExceptionHandler(App myApplication) {
        this.myApplication = myApplication;
        mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的异常处理器
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mUncaughtExceptionHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mUncaughtExceptionHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ex.printStackTrace();
            Log.e("uncaughtException",ex.getMessage());
            Intent intent = new Intent(myApplication.getApplicationContext(), MainActivity.class);
            //重启应用，得使用PendingIntent
            PendingIntent restartIntent = PendingIntent.getActivity(
                    myApplication.getApplicationContext(), 0, intent,
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            //退出程序
            AlarmManager mAlarmManager = (AlarmManager) myApplication.getSystemService(Context.ALARM_SERVICE);
            mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    restartIntent); // 1秒钟后重启应用

            Toast.makeText(myApplication.getApplicationContext(), "即将重启",
                    Toast.LENGTH_SHORT).show();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(myApplication.getApplicationContext(), "很抱歉,程序出现异常,即将重启.",
                        Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        return true;
    }
}
