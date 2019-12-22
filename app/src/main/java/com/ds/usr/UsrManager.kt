package com.ds.usr

import com.ds.GlobalContext
import com.ds.utils.Constant
import com.ds.utils.HttpsUtils
import kotlinx.coroutines.*
import java.io.File

class UsrManager : IDevice, ParsePack {
    private val mainScope = MainScope()
    private var serialPort: android_serialport_api.SerialPort? = null
    private var mStatus = -1
    private var isOpen = false

    object API {
        val QUERY_STATUS = byteArrayOf(0x5A, 0x00, 0x02, 0x01, 0x00, 0x57) //查询锁臂状态
        val QUERY_POWER = byteArrayOf(0x5A, 0x00, 0X03, 0X03, 0x00, 0x57)   //查询地锁电量
        val START_CEJU = byteArrayOf(0x5A, 0x02, 0x02, 0x02, 0x00, 0x57) //开始微波测距
        val SHANGCHENG = byteArrayOf(0x5A, 0x00, 0x01, 0x01, 0x00, 0x57) //锁臂上升
        val XIAJIANG = byteArrayOf(0x5A, 0x00, 0x01, 0x02, 0x00, 0x57)   //锁臂下降
        val ZHUKONG_READY = byteArrayOf(0x5A, 0x02, 0x02, 0x32, 0x00, 0x57) //主控唤醒就绪


        val RUKU = byteArrayOf(0x5A, 0x01, 0x00, 0x16, 0x00, 0x57)
        val CHUKU = byteArrayOf(0x5A, 0x01, 0x00, 0x17, 0x00, 0x57)
        val RUKUFANKUI = byteArrayOf(0x5A, 0x01, 0x01, 0x16, 0x01, 0x57)
        val CHUKUFANKUI = byteArrayOf(0x5A, 0x01, 0x01, 0x17, 0x01, 0x57)


        val STATUS_XIAJIANG = byteArrayOf(0x5A, 0x00, 0x20, 0x00, 0x00, 0x57)
        val STATUS_SHANGSHENG = byteArrayOf(0x5A, 0x00, 0x20, 0x01, 0x00, 0x57)
        val STATUS_YUNDONG = byteArrayOf(0x5A, 0x00, 0x20, 0x06, 0x00, 0x57)
        val STATUS_CHUSHI = byteArrayOf(0x5A, 0x00, 0x20, 0x08, 0x00, 0x57)
        val STATUS_SANGSHENGZHONG = byteArrayOf(0x5A, 0x00, 0x10, 0x01, 0x00, 0x57)
        val STATUS_HASSHANGCHENG = byteArrayOf(0x5A, 0x00, 0x10, 0x11, 0x00, 0x57)
        val STATUS_XIAJIANGZHONG = byteArrayOf(0x5A, 0x00, 0x10, 0x02, 0x00, 0x57)
        val STATUS_HASXIAJIANG = byteArrayOf(0x5A, 0x00, 0x10, 0x22, 0x00, 0x57)
    }

    init {
        open()
    }

    fun open() {
        if (isOpen) return
        mainScope.launch(Dispatchers.IO) {
            GlobalContext.getInstance().notifyDataChanged(Constant.LOG, "开启副控开始")
            try {
                serialPort = android_serialport_api.SerialPort(File("/dev/ttyMT1"), 9600, 0)
//                serialPort = android_serialport_api.SerialPort(File("/dev/ttyS2"), 9600, 0)
                isOpen = serialPort?.open() ?: false
            } catch (e: Exception) {
                GlobalContext.getInstance().notifyDataChanged(Constant.LOG, "开启副控失败")
            }
            if (isOpen) {
                GlobalContext.getInstance().notifyDataChanged(Constant.LOG, "开启副控成功")
                async { startRead() }
                async { queryDeviceInfo() }
            } else {
                GlobalContext.getInstance().notifyDataChanged(Constant.LOG, "开启副控失败")
            }
        }
    }

    private suspend fun queryDeviceInfo() {
        withContext(Dispatchers.IO) {
            while (isActive) {
                write(API.QUERY_STATUS)
                delay(1000)
                write(API.QUERY_POWER)
                delay(1000 * 60 * 60)
            }
        }
    }


    /**
     * 上升
     */
    fun ris() {
        mainScope.launch {
            delay(5000)
            write(API.SHANGCHENG)
        }
    }

    /**
     * 下降
     */
    fun fall() {
        mainScope.launch {
            write(API.XIAJIANG)
        }
    }

    /**
     * 测距
     */
    fun ceju() {
        mainScope.launch {
            write(API.START_CEJU)
        }
    }


    private suspend fun startRead() {
        withContext(Dispatchers.IO) {
            val recv = ByteArray(6)
            var length: Int
            while (isActive) {
                try {
                    length = read(recv)
                    if (length > 0) parsePack(recv, length)
                    else delay(1000)
                } catch (e: Throwable) {
                    GlobalContext.getInstance().notifyDataChanged(Constant.LOG, e.message)
                }
            }
        }
    }

    private fun write(data: ByteArray) {
        if (isOpen) {
            mainScope.launch(Dispatchers.IO) {
                serialPort?.outputStream?.write(data)
            }
        }
    }

    override fun read(buf: ByteArray) = serialPort?.inputStream?.read(buf) ?: 0

    override fun close() {
        httpJOB?.cancel()
        mainScope.cancel()
        serialPort?.inputStream?.close()
        serialPort?.outputStream?.close()
        serialPort?.close()
    }

    @Throws
    override fun parsePack(recv: ByteArray, length: Int) {
        when (String(recv)) {
            String(API.RUKU) -> {
                write(API.RUKUFANKUI)
                GlobalContext.getInstance().notifyDataChanged(Constant.RUKU, "请求打开摄像机")
            }
            String(API.CHUKU) -> {
                write(API.CHUKUFANKUI)
                GlobalContext.getInstance().notifyDataChanged(Constant.CHUKU, "向后台发送通知")
            }
            String(API.STATUS_SHANGSHENG) -> GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 1)
            String(API.STATUS_XIAJIANG) -> GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 2)
            String(API.STATUS_YUNDONG) -> GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 3)
            String(API.STATUS_CHUSHI) -> GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 4)
            String(API.STATUS_SANGSHENGZHONG) -> GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 5)
            String(API.STATUS_XIAJIANGZHONG) -> GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 6)
            String(API.STATUS_HASSHANGCHENG) -> GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 1)
            String(API.STATUS_HASXIAJIANG) -> {
                GlobalContext.getInstance().notifyDataChanged(Constant.STATUS_LOCK, 2)
                GlobalContext.getInstance().notifyDataChanged(Constant.XIAJIANG_BACK, "下降锁成功")
            }
            else -> {
                if (recv[2].toInt() == 0x30) GlobalContext.getInstance().notifyDataChanged(Constant.POWER, recv[3].toInt())
            }
        }
    }

    fun checkIfCanUnLock() {
        httpJOB = mainScope.async {
            delay(3000)
            HttpsUtils.checkIfCanUnLock(object : HttpsUtils.HttpUtilCallBack<String> {
                override fun onSuccess(t: String?) {
                    GlobalContext.getInstance().notifyDataChanged(Constant.SCAL_OPEN, "")
                    checkIfCanUnLock()
                }

                override fun onFaile(errorCode: Int, errorMsg: String) {
                    GlobalContext.getInstance().notifyDataChanged(Constant.LOG, errorMsg)
                    checkIfCanUnLock()
                }
            })
        }
    }

    var httpJOB: Job? = null
}