package com.ds.uartManager;

import android.util.Log;

import java.io.IOException;

/**
 * Created by gongkan on 2019/8/3.
 */

public class ReadThread implements Runnable {

    private Thread thread;

    private IDevice device;

    private ParsePack pack;

    private boolean needRun = false;//
    private boolean needRead = false;

    public ReadThread(IDevice device, ParsePack pack) {
        this.device = device;
        this.pack = pack;
    }

    public void startMonitor() {
        stopMonitor();
        thread = new Thread(this);
        needRun = true;
        thread.start();
    }

    public void stopMonitor() {
        if (thread != null && thread.isAlive()) {
            try {
                needRun = false;
                thread.join();
            } catch (InterruptedException e) {
            }
        }

        thread = null;
    }

    @Override
    public void run() {
        byte[] recv = new byte[32];
        while (device.isOpen() && needRun) {
            Log.e("USRMANAGER", "ReadThread run");
            int length = 0;
            try {
                length = device.read(recv);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (length <= 0) {
                continue;
            } else {
                pack.parsePack(recv, length);
            }
        }
    }

}
