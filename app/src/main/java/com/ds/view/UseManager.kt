package com.ds.view

import android.util.Log
import android_serialport_api.SerialPort
import com.ds.GlobalContext
import com.ds.utils.Constant
import java.io.File

class UseManager : Runnable {
    companion object {
        private const val TAG = "UseManager"
    }

    var isOpen = false
    private var wakeRun = false
    private var cmd = byteArrayOf(0x41.toByte(), 0x54.toByte(), 0x2b.toByte(), 0x53.toByte(), 0x41, 0x0A)
    private var mthread: Thread? = null
    private val path = "/dev/ttyMT1"
//    private val path = "/dev/ttyS2"
    private var serialPort = SerialPort(File(path), 115200, 0)

    fun open() {
        if (mthread != null && mthread!!.isAlive) {
            return
        }
        serialPort = SerialPort(File(path), 115200, 0)
        mthread = Thread(this)
        mthread?.start()
        Log.e(TAG, "open")
    }

    fun wake_USR(): Boolean {
        wakeRun = false
        mthread?.join()
        wakeRun = true
        open()
        return wakeRun
    }

    fun close() {
        wakeRun = false
        serialPort.inputStream?.close()
        serialPort.outputStream?.close()
        Log.e(TAG, "close")
    }

    override fun run() {
        isOpen = serialPort.open()
        val recv = ByteArray(32)
        while (isOpen && wakeRun) {
            try {
                Log.e(TAG, "wakeRun== $wakeRun")
                serialPort.outputStream?.write(cmd)
                var length = serialPort.inputStream?.read(recv)
                if (length == null || length <= 0) continue
                else {
                    val distance = String(recv)
                    GlobalContext.getInstance().notifyDataChanged(Constant.KEY_USR_DISTANCE, distance)
                    Log.e(TAG, "雷达监测到距离==$distance")
                }
                Thread.sleep(1000)
            } catch (e: Exception) {
                Log.e(TAG, "雷达监测失败==${e.message}")
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_USR_ERR, "雷达监测失败")
                wake_USR()
                return
            }
        }
    }
}