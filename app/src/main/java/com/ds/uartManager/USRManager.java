package com.ds.uartManager;

import android.text.TextUtils;
import android.util.Log;

import com.ds.GlobalContext;
import com.ds.utils.Constant;
import com.ds.utils.HttpsUtils;
import com.ds.utils.StringUtil;

import java.io.File;
import java.io.IOException;

import android_serialport_api.SerialPort;

/**
 * Created by gongkan on 2019/8/3.
 */

public class USRManager implements IDevice, ParsePack, Runnable {
    public static final String TAG = "USRManager";
    private SerialPort serialPort;
    private boolean isOpen = false;
    private ReadThread readThread;
    private Thread wakeThread;
    private boolean wake_thread_flag = false;
    private int status = -1;

    private Thread queryThread;
    private boolean query_thread_flag = true;
    private Thread powerThread;


    private Thread riseThread;
    private Thread fallThread;
    private boolean rise_thread_flag = false;
    private boolean fall_thread_flag = false;

    public USRManager() {
//        serialPort = new SerialPort(new File("/dev/ttyS2"), 115200, 0);//新串口
        serialPort = new SerialPort(new File("/dev/ttyMT1"), 9600, 0);//旧串口
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
                        byte[] cmd = new byte[]{(byte) 0x5A, (byte) 0x02, (byte) 0x02, (byte) 0x02, 0x00, 0x57};
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

    public void query_status() {
        query_thread_flag = true;
        queryThread = new Thread() {
            @Override
            public void run() {
                while (isOpen && query_thread_flag) {
                    byte[] status_cmd = new byte[]{0x5A, 0x00, 0x02, 0x01, 0x00, 0x57};
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

    private void queryPower() {
        if (powerThread == null) {
            powerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isOpen && query_thread_flag) {
                        byte[] status_cmd = new byte[]{0x5A, 0x00, 0X03, 0X03, 0x00, 0x57};
                        try {
                            write(status_cmd);
                        } catch (IOException e) {
                            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_MIMI_ERR, "查询车锁电量失败");
                        }
                    }
                }
            });
            powerThread.start();
        }
    }

    public void rise() {
        Log.e(TAG, "rise start");
        try {
            Log.e(TAG, "rise start   status==" + status);
            if (status == 0 || status == 8) {
                byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x01, 0x00, 0x57};
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

    public void fall(String fallChepai, String dev_type) {
        try {
            if (status == 0) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "已经是下降状态");
            } else if (status == 1 || status == 8) {
                byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x02, 0x00, 0x57};
                write(status_cmd);
            } else {
                startFall(fallChepai, dev_type);
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
                if (rise_thread_flag && (status == 0 || status == 8)) {
                    byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x01, 0x00, 0x57};
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

    private void startFall(final String fallChepai, final String dev_type) {
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
                    byte[] status_cmd = new byte[]{0x5A, 0x00, 0x01, 0x02, 0x00, 0x57};
                    try {
                        write(status_cmd);
                        if (!TextUtils.isEmpty(fallChepai))
                            HttpsUtils.Companion.parkingCallBack(fallChepai, dev_type, true);
                    } catch (IOException e) {
                        GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_MIMI_ERR, "下降栏杆通信失败");
                        if (!TextUtils.isEmpty(fallChepai))
                            HttpsUtils.Companion.parkingCallBack(fallChepai, dev_type, false);
                    }
                } else {
                    GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_STATUS_CHANGE, status);
                    if (!TextUtils.isEmpty(fallChepai))
                        HttpsUtils.Companion.parkingCallBack(fallChepai, dev_type, false);
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



    @Override
    public void parsePack(byte[] recv, int length) {
        if (recv[2] == 0x20) {
            //地锁状态反馈
            if (status != recv[3]) {
                status = recv[3];
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_STATUS_CHANGE, status);
            }
        } else if (recv[2] == 0x30) {
            //地锁电量反馈
        } else if (recv[2] == 0x10) {
            //地锁操作反馈
            if (recv[3] == 0x01) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_RISE, "栏杆开始升起");
            } else if (recv[3] == 0x11) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_END_RISE, "栏杆升起完成");
            } else if (recv[3] == 0x02) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "栏杆开始下降");
            } else if (recv[3] == 0x22) {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "栏杆下降完成");
            }
        } else if (recv[0] == 0x5B) {
            //发送车牌号反馈
        } else if (recv[1] == 0x02 && recv[2] == 0x30) {
            Log.w("USRManager", StringUtil.bytesToHexString(recv, length));
            String distance = new String(recv);
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_USR_DISTANCE, distance);
        }
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
