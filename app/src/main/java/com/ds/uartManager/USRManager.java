package com.ds.uartManager;

import android.util.Log;

import com.ds.GlobalContext;
import com.ds.utils.Constant;
import com.ds.utils.StringUtil;

import java.io.File;
import java.io.IOException;

import android_serialport_api.SerialPort;

/**
 * Created by gongkan on 2019/8/3.
 */

public class USRManager implements IDevice, ParsePack,Runnable {
    public static final String TAG = "USRManager";
    private SerialPort serialPort;
    private boolean isOpen = false;
    private ReadThread readThread;
    private Thread wakeThread;
    private boolean wake_thread_flag = false;

    public USRManager() {
//        serialPort = new SerialPort(new File("/dev/ttyS2"), 115200, 0);//新串口
        serialPort = new SerialPort(new File("/dev/ttyMT1"), 115200, 0);//旧串口
        readThread = new ReadThread(this, this);
    }


    @Override
    public boolean open() {
        if (!isOpen) {
            isOpen = serialPort.open();
        }
        if (isOpen) {
            readThread.startMonitor();
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_USR_OPEN_SUCCESS, "开启检测雷达成功");
        } else {
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_USR_OPEN_FAILED, "开启检测雷达失败");
        }
        return isOpen;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public void write(final byte[] data) throws IOException {
        Log.w(TAG, "USR write:" + StringUtil.bytesToHexString(data));
        serialPort.getOutputStream().write(data);
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return serialPort.getInputStream().read(buf);
    }

    @Override
    public void close() {
        try {
            serialPort.close();
            isOpen = false;
            destroy_wake();
            readThread.stopMonitor();
        } catch (Exception e) {

        }
    }

    public void destroy_wake() {
        wake_thread_flag = false;
        if (wakeThread != null && wakeThread.isAlive()) {
            try {
                wakeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        wakeThread = null;
    }

    public void wake_USR() {
        wake_thread_flag = true;
        if (wakeThread != null && wakeThread.isAlive()) {
            return;
        } else {
            wakeThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (wake_thread_flag && isOpen) {
                        byte[] cmd = new byte[]{(byte) 0x41, (byte) 0x54, (byte) 0x2b, (byte) 0x53, 0x41, 0x0A};
                        try {
                            sleep(1000);
                            write(cmd);
                        } catch (Exception e) {
                            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_USR_ERR, "开启检测雷达失败");
                        }
                    }
                }
            };
            wakeThread.start();
        }
    }

    @Override
    public void parsePack(byte[] recv, int length) {
        Log.w("USRManager", StringUtil.bytesToHexString(recv, length));
//        if (recv[1] == 7){
        String distance = new String(recv);
        GlobalContext.getInstance().notifyDataChanged(Constant.KEY_USR_DISTANCE, distance);
//        }
    }

    @Override
    public void run() {
        if (!isOpen) {
            isOpen = serialPort.open();
        }
        if (isOpen) {
            readThread.startMonitor();
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_USR_OPEN_SUCCESS, "开启检测雷达成功");
        } else {
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_USR_OPEN_FAILED, "开启检测雷达失败");
        }
    }
}
