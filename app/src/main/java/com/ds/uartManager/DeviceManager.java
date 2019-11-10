package com.ds.uartManager;

import android.util.Log;

/**
 * Created by gongkan on 2019/8/3.
 */

public class DeviceManager {
    private USRManager usr;

    private MiniManager mini;

    public DeviceManager() {
        usr = new USRManager();
        mini = new MiniManager();

    }

    public USRManager getUsr() {
        return usr;
    }

    public MiniManager getMini() {
        return mini;
    }

    public void open() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.w("DeviceManager", "微波探测开启 = " + usr.open());
                Log.w("DeviceManager", "栏杆探测开启 = " + mini.open());
            }
        }.start();

    }

    public void close() {
        usr.close();
        mini.close();
    }
}
