package com.ds.usrToos

import android.util.Log
import android_serialport_api.SerialPort
import com.ds.view.ControlFragment
import kotlinx.coroutines.*

class LockTool(private val fragment: ControlFragment) {
    private val mainScope = MainScope()
    private var serialPort: SerialPort? = null
    private var readBuffer = ByteArray(6)
    private var mSerialPortJob: Job? = null
    var lockStatus = 0//车锁状态 -1：初始 0：运动 1：上升 2：下降
    private var powerStatus = -1
    var power1 = 0
    var power2 = 0

    private var mIsRise = false
    private var mIsFall = false

    fun open() {
        if (serialPort != null) return
        mSerialPortJob?.cancel()
        mSerialPortJob = mainScope.launch(Dispatchers.IO) {
            serialPort = SerialPort("/dev/ttyMT1", 9600, 0)
            val openEd = serialPort?.open() ?: false
            fragment.operateResult(Constant.LOG, "串口初始化 ${if (openEd) "成功" else "失败"}")
            async { startRead() }
            async { queryStatus() }
        }
    }

    private suspend fun startRead() {
        withContext(Dispatchers.IO) {
            while (isActive) {
                readBuffer = ByteArray(6)
                when (serialPort?.read(readBuffer) ?: 0) {
                    0 -> delay(500)
                    else -> parseReadBuffer(readBuffer)
                }
            }
        }
    }

    private suspend fun queryStatus() {
        withContext(Dispatchers.IO) {
            serialPort?.write(Constant.queryLockStatus)
            delay(2000)
            serialPort?.write(Constant.queryPowerStatus)
            delay(3600000)
            queryStatus()
        }
    }

    fun rise() {
        Log.e("bbb", "bbbbb  $mIsFall  $mIsRise")
        if (mIsRise || mIsFall) return
        mIsRise = true
        mainScope.launch(Dispatchers.IO) {
            if (lockStatus == 1 || lockStatus == 0) {
                fragment.operateResult(Constant.LOG, "锁臂已经上升或者移动中")
                return@launch
            }
            Log.e("bbb", "rise ")
            val result = serialPort?.write(Constant.riseBytes) ?: false
            fragment.operateResult(Constant.SEND_LOCK_RISE, result)
        }
    }


    fun release() {
        mainScope.cancel()
        serialPort?.release()
        serialPort = null
    }

    fun canFall() = lockStatus != 2 && lockStatus != 0 && !mIsFall && !mIsRise
    fun canRise() = lockStatus != 1 && lockStatus != 0 && !mIsFall && !mIsRise


    private val fallBytes = byteArrayOf(0x5A, 0x00, 0x01, 0x02, 0x00, 0x57)   //锁臂下降
    fun fall() {
        if (mIsFall || mIsFall) return
        mIsFall = true
        mainScope.launch(Dispatchers.IO) {
            if (lockStatus == 2 || lockStatus == 0) {
                fragment.operateResult(Constant.LOG, "锁臂已经下降或者移动中")
                return@launch
            }
            Log.e("bbb", "fall ")
            val result = serialPort?.write(fallBytes) ?: false
            fragment.operateResult(Constant.SEND_LOCK_FALL, result)
        }
    }

    private suspend fun parseReadBuffer(byteArray: ByteArray) {
        Log.e("parseReadBuffer", "byteArray == ${byteArray[0].toString(16)}" +
                " ${byteArray[1].toString(16)} ${byteArray[2].toString(16)}" +
                " ${byteArray[3].toString(16)} ${byteArray[4].toString(16)} ${byteArray[5].toString(16)}")
        when (String(byteArray)) {
            String(Constant.riseFeedbackBytes) -> {
                if (lockStatus != 0) {
                    fragment.operateResult(Constant.SEND_LOCK_RISE_FEED_BACK, "上升中")
                    lockStatus = 0
                }
            }
            String(Constant.riseSuccessBytes) -> {
                if (lockStatus != 1) {
                    fragment.operateResult(Constant.SEND_LOCK_RISE_SUCCESS, "上升")
                    lockStatus = 1
                    delay(2000)
                    mIsFall = false
                    mIsRise = false
                }
            }
            String(Constant.fallFeedbackBytes) -> {
                if (lockStatus != 0) {
                    fragment.operateResult(Constant.SEND_LOCK_FALL_FEED_BACK, "下降中")
                    lockStatus = 0
                }
            }
            String(Constant.fallSuccessBytes) -> {
                if (lockStatus != 2) {
                    fragment.operateResult(Constant.SEND_LOCK_FALL_SUCCESS, "下降")
                    lockStatus = 2
                    delay(2000)
                    mIsFall = false
                    mIsRise = false
                }
            }
            String(Constant.lockFallStatusBytes) -> {
                if (lockStatus != 2) {
                    fragment.operateResult(Constant.LOCK_FALL_STATUS, "下降")
                    lockStatus = 2
                    delay(2000)
                    mIsFall = false
                    mIsRise = false
                }
            }
            String(Constant.lockRiseStatusBytes) -> {
                if (lockStatus != 1) {
                    fragment.operateResult(Constant.LOCK_RISE_STATUS, "上升")
                    lockStatus = 1
                    delay(2000)
                    mIsFall = false
                    mIsRise = false
                }
            }
            String(Constant.lockMoveStatusBytes) -> {
                if (lockStatus != 0) {
                    fragment.operateResult(Constant.LOCK_MOVE_STATUS, "运动")
                    lockStatus = 0
                }
            }
            String(Constant.lockFirstStatusBytes) -> {
                if (lockStatus != -1) {
                    fragment.operateResult(Constant.LOCK_FIRST_STATUS, "初始")
                    lockStatus = -1
                }
            }
            else -> {
                if (byteArray[1].toInt() == 0x00 && byteArray[2].toInt() == 0x30) {
                    val power1 = byteArray[3].toInt()
                    val power2 = byteArray[4].toInt()
//                    val power = MathUtils.disPower(byteArray[3], byteArray[4])
                    if (this.power1 != power1 || this.power2 != power2) {
                        this.power1 = power1
                        this.power2 = power2
                        fragment.operateResult(Constant.POWER_STATUS, power1 + power2)
                    }
                }
            }
        }
    }
}