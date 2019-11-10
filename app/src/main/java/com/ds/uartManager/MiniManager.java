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

public class MiniManager implements IDevice, ParsePack {
    private static final String TAG = "MiniManager";
    private SerialPort serialPort;
    private boolean isOpen = false;
    private Thread riseThread;
    private Thread fallThread;
    private boolean rise_thread_flag = false;
    private boolean fall_thread_flag = false;
    private int status = -1;
    private ReadThread readThread;
    private Thread queryThread;
    private boolean query_thread_flag = true;

    public MiniManager() {
        serialPort = new SerialPort(new File("/dev/ttyS3"), 9600, 0);//新串口
//        serialPort = new SerialPort(new File("/dev/ttyMT0"), 9600, 0);//旧串口
        readThread = new ReadThread(this, this);
    }


    @Override
    public boolean open() {
        if (!isOpen) {
            isOpen = serialPort.open();
        }
        if (isOpen) {
            readThread.startMonitor();
            query_status();
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MINI_OPEN_SUCCESS, "开启栏杆成功");
        } else {
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MINI_OPEN_FAILED, "开启栏杆失败");
        }
        return isOpen;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() {
        serialPort.close();
        stopFall();
        stopRise();
        stopQuery();
        isOpen = false;
        readThread.stopMonitor();
    }

    public void write(final byte[] data) throws IOException {
        Log.w(TAG, "write = " + StringUtil.bytesToHexString(data));
        serialPort.getOutputStream().write(data);
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return serialPort.getInputStream().read(buf);
    }


    public void rise() {
        Log.e(TAG, "rise start");
        try {
            Log.e(TAG, "rise start   status==" + status);
            if (status == 0 || status == 8) {
                byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x01, (byte) 0xff, 0x57};
                write(status_cmd);
            } else if (status == 1) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_RISE, "已经是上升状态");
            } else {
                startRise();
            }
        } catch (Exception e) {
            Log.e(TAG, "rise start   error==" + status);
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_MIMI_ERR, "通信失败");
        }
    }

    public void fall() {
        try {
            if (status == 0) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "已经是下降状态");
            } else if (status == 1 || status == 8) {
                byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x02, (byte) 0xff, 0x57};
                write(status_cmd);
            } else {
                startFall();
            }
        } catch (Exception e) {
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_MIMI_ERR, "通信失败");
        }
    }

    private void startRise() {
        stopRise();
        stopFall();
        rise_thread_flag = true;
        riseThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (rise_thread_flag && (status == 6)) {
                    try {
                        Thread.sleep(2000);
                        GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "当前为运动状态，等待2s");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (rise_thread_flag && (status == 1 || status == 8)) {
                    byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x01, (byte) 0xff, 0x57};
                    try {
                        write(status_cmd);
                    } catch (IOException e) {
                        GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_MIMI_ERR, "上升栏杆通信失败");
                    }
                }
            }
        };
        // thread.setDaemon(true);
        riseThread.start();
    }

    private void stopRise() {
        rise_thread_flag = false;
        if (riseThread != null && riseThread.isAlive()) {
            try {
                riseThread.join();
            } catch (InterruptedException e) {
            }
        }
        riseThread = null;
    }

    private void startFall() {
        stopRise();
        stopFall();
        fall_thread_flag = true;
        fallThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (fall_thread_flag && (status == 6)) {
                    try {
                        GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "当前为运动状态，等待2s");
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (fall_thread_flag && (status == 1 || status == 8)) {
                    byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x02, (byte) 0xff, 0x57};
                    try {
                        write(status_cmd);
                    } catch (IOException e) {
                        GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_MIMI_ERR, "下降栏杆通信失败");
                    }
                } else {
                    GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_STATUS_CHANGE, status);
                }
            }
        };

        // thread.setDaemon(true);
        fallThread.start();
    }

    private void stopFall() {
        fall_thread_flag = false;
        if (fallThread != null && fallThread.isAlive()) {
            try {
                fallThread.join();
            } catch (InterruptedException e) {
            }
        }
        fallThread = null;
    }

    public void query_status() {
        query_thread_flag = true;
        queryThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (isOpen && query_thread_flag) {
                    byte[] status_cmd = new byte[]{0x5A, 0x00, 0x02, 0x01, (byte) 0xff, 0x57};
                    try {
                        write(status_cmd);
                        sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        queryThread.start();
    }

    private void stopQuery() {
        query_thread_flag = false;
        if (queryThread != null && queryThread.isAlive()) {
            try {
                queryThread.join();
            } catch (InterruptedException e) {
            }
        }
        queryThread = null;
    }

    @Override
    public void parsePack(byte[] recv, int length) {
        Log.w(TAG, "read = " + StringUtil.bytesToHexString(recv, length));
        if (recv[2] == 0x20) {//查询状态返回
            if (status != recv[3]) {
                status = recv[3];
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_STATUS_CHANGE, status);
            }
        } else if (recv[2] == 0x10) {//栏杆升降返回
            if (recv[3] == 1) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_RISE, "栏杆开始升起");
            } else if (recv[3] == 0x11) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_END_RISE, "栏杆升起完成");
            } else if (recv[3] == 0x02) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "栏杆开始下降");
            } else if (recv[3] == 0x12) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "栏杆下降完成");
            }
        } else if (recv[2] == 0x30) {//电量查询返回
            Log.w(TAG, "当前电量为：" + recv[3]);
        }
    }
}
