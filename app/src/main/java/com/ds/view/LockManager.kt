package com.ds.view

import android.util.Log
import android_serialport_api.SerialPort
import com.ds.GlobalContext
import com.ds.utils.Constant
import com.ds.utils.StringUtil
import java.io.File

class LockManager : Runnable {
    companion object {
        private const val TAG = "LockManager"
    }

    var isOpen = false
        private val path = "/dev/ttyMT0"
//    private val path = "/dev/ttyS3"
    private var status = -1
    private var mthread: Thread? = null
    private var serialPort = SerialPort(File(path), 9600, 0)
    private var riseCmd = byteArrayOf(0x5A, 0x00, 0x01, 0x01, 0xff.toByte(), 0x57)
    private var fallCmd = byteArrayOf(0x5A, 0x00, 0x01, 0x02, 0xff.toByte(), 0x57)
    private var queryCmd = byteArrayOf(0x5A, 0x00, 0x02, 0x01, 0xff.toByte(), 0x57)
    private var cmd = queryCmd
    private val recv = ByteArray(32)


    fun open() {
        mthread?.join()
        serialPort = SerialPort(File(path), 9600, 0)
        if (mthread != null && mthread!!.isAlive) return
        mthread = Thread(this)
        mthread?.start()
        Log.e(TAG, "open")
    }

    fun queryStatus() {
        cmd = queryCmd
        open()
    }

    fun rise() {
        cmd = riseCmd
        queryStatus()
        Log.e(TAG, "rise start   status==$status")
        when (status) {
            0 -> {
                cmd = riseCmd
                open()
            }
            1 -> {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_RISE, "已经是上升状态")
                Log.e(TAG, "已经是上升状态")
            }
            -1 -> {
                open()
            }
            else -> {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "当前为运动状态")
                Log.e(TAG, "当前为运动状态")
            }
        }
    }

    fun fall() {
        cmd = fallCmd
        queryStatus()
        Log.e(TAG, "rise start   status==$status")
        when (status) {
            1 -> {
                cmd = fallCmd
                open()
            }
            0 -> {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_RISE, "已经是上升状态")
                Log.e(TAG, "已经是下降状态")
            }
            -1 -> {
                open()
            }
            else -> {
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "当前为运动状态")
                Log.e(TAG, "当前为运动状态")
            }
        }
    }

    fun close() {
        isOpen = false
        serialPort.inputStream?.close()
        serialPort.outputStream?.close()
        Log.e(TAG, "close")
    }

    override fun run() {
        isOpen = serialPort.open()
        try {
            while (isOpen) {
                var f = writeCmd(queryCmd)
                Log.e(TAG, "f==$f")

                if (!f) {
                    Thread.sleep(100)
                    continue
                }

                Log.e(TAG, "status==$status")

                if (status != 0 && status != 1) {
                    Thread.sleep(500)
                    continue
                }
                var g = writeCmd(cmd)
                Log.e(TAG, "g==$g")
                if (cmd == queryCmd) return
                if (!writeCmd(cmd)) {
                    Thread.sleep(100)
                    continue
                }
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "栏杆操作失败==${e.message}   cmd==$cmd")
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_CMD_MIMI_ERR, "栏杆操作失败 cmd==$cmd")
            open()
            return
        }
    }

    private fun writeCmd(cmd: ByteArray): Boolean {
        serialPort.outputStream?.write(cmd)
        var length = serialPort.inputStream?.read(recv)
        if (length == null || length <= 0) {
            return false
        }
        parsePack(recv, length)
        return true
    }

    private fun parsePack(recv: ByteArray, length: Int) {
        Log.w(TAG, "read = " + StringUtil.bytesToHexString(recv, length))
        if (recv[2].toInt() == 0x20) {//查询状态返回
            if (status != recv[3].toInt()) {
                status = recv[3].toInt()
                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_STATUS_CHANGE, status)
            }
        } else if (recv[2].toInt() == 0x10) {//栏杆升降返回
            when {
                recv[3].toInt() == 1 -> GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_RISE, "栏杆开始升起")
                recv[3].toInt() == 0x11 -> GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_END_RISE, "栏杆升起完成")
                recv[3].toInt() == 0x02 -> GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "栏杆开始下降")
                recv[3].toInt() == 0x12 -> GlobalContext.getInstance().notifyDataChanged(Constant.KEY_MIMI_START_FALL, "栏杆下降完成")
            }
        } else if (recv[2].toInt() == 0x30) {//电量查询返回
            Log.w(TAG, "当前电量为：" + recv[3])
        }
    }

}