package com.ds.usrToos

import android.util.Log
import android_serialport_api.SerialPort
import com.ds.view.ControlFragment
import kotlinx.coroutines.*

class UsrTool(private val fragment: ControlFragment) {
    private val mainScope = MainScope()
    private var serialPort: SerialPort? = null
    private var readBuffer = ByteArray(6)
    private var mSerialPortJob: Job? = null
    private var lockStatus = -2
    private var powerStatus = -1

    fun open() {
        mSerialPortJob?.cancel()
        mSerialPortJob = mainScope.launch(Dispatchers.IO) {
            serialPort = SerialPort("/dev/ttyMT1", 9600, 0)
            val openEd = serialPort?.open() ?: false
            fragment.operateResult(Constant.LOG, "串口初始化 ${if (openEd) "成功" else "失败"}")
            async { startRead() }
        }
    }

    fun release() {
        mainScope.cancel()
        serialPort?.release()
        serialPort = null
    }

    fun canFall() = lockStatus != 2 && lockStatus != 0

    private val riseBytes = byteArrayOf(0x5A, 0x00, 0x01, 0x01, 0x00, 0x57) //锁臂上升
    fun rise() {
        mainScope.launch(Dispatchers.IO) {
            if (lockStatus == 1 || lockStatus == 0) {
                fragment.operateResult(Constant.LOG, "锁臂已经上升或者移动中")
                return@launch
            }
            val result = serialPort?.write(riseBytes) ?: false
            fragment.operateResult(Constant.SEND_LOCK_RISE, result)
            if (!result) reset()
        }
    }

    private val fallBytes = byteArrayOf(0x5A, 0x00, 0x01, 0x02, 0x00, 0x57)   //锁臂下降
    fun fall() {
        mainScope.launch(Dispatchers.IO) {
            if (lockStatus == 2 || lockStatus == 0) {
                fragment.operateResult(Constant.LOG, "锁臂已经下降或者移动中")
                return@launch
            }
            val result = serialPort?.write(fallBytes) ?: false
            fragment.operateResult(Constant.SEND_LOCK_FALL, result)
            if (!result) reset()
        }
    }


    private val openLightBytes = byteArrayOf(0x5A, 0x02, 0x02, 0x33, 0x00, 0x57)
    fun openLight() {
        mainScope.launch(Dispatchers.IO) {
            val result = serialPort?.write(openLightBytes) ?: false
            fragment.operateResult(Constant.SEND_OPEN_LIGHT, result)
            if (!result) reset()
        }
    }

    private val closeLightBytes = byteArrayOf(0x5A, 0x02, 0x02, 0x34, 0x00, 0x57)
    fun closeLight() {
        mainScope.launch(Dispatchers.IO) {
            val result = serialPort?.write(closeLightBytes) ?: false
            fragment.operateResult(Constant.SEND_CLOSE_LIGHT, result)
            if (!result) reset()
        }
    }

    private suspend fun reset() {
        fragment.operateResult(Constant.LOG, "串口没用初始化,开始初始化")
        open()
    }


    private suspend fun startRead() {
        withContext(Dispatchers.IO) {
            while (isActive) {
                readBuffer = ByteArray(6)
                when (serialPort?.read(readBuffer) ?: -1) {
                    -1 -> reset()
                    0 -> {
                    }
                    else -> parseReadBuffer(readBuffer)
                }
                delay(1000)
            }
        }
    }

    private val riseFeedbackBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x01, 0x00, 0x57)
    private val riseSuccessBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x11, 0x00, 0x57)

    private val fallFeedbackBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x02, 0x00, 0x57)
    private val fallSuccessBytes = byteArrayOf(0x5A, 0x00, 0x10, 0x22, 0x00, 0x57)


    private val lockFallStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x00, 0x00, 0x57)
    private val lockRiseStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x01, 0x00, 0x57)
    private val lockMoveStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x06, 0x00, 0x57)
    private val lockFirstStatusBytes = byteArrayOf(0x5A, 0x00, 0x20, 0x08, 0x00, 0x57)

    private suspend fun parseReadBuffer(byteArray: ByteArray) {
        Log.e("parseReadBuffer", "byteArray == ${byteArray[0].toString(16)}" +
                " ${byteArray[1].toString(16)} ${byteArray[2].toString(16)}" +
                " ${byteArray[3].toString(16)} ${byteArray[4].toString(16)} ${byteArray[5].toString(16)}")
        when (String(byteArray)) {
            String(riseFeedbackBytes) -> {
                if (lockStatus != 0) {
                    fragment.operateResult(Constant.SEND_LOCK_RISE_FEED_BACK, "上升中")
                    lockStatus = 0
                }
            }
            String(riseSuccessBytes) -> {
                if (lockStatus != 1) {
                    fragment.operateResult(Constant.SEND_LOCK_RISE_SUCCESS, "上升")
                    lockStatus = 1
                }
            }
            String(fallFeedbackBytes) -> {
                if (lockStatus != 0) {
                    fragment.operateResult(Constant.SEND_LOCK_FALL_FEED_BACK, "下降中")
                    lockStatus = 0
                }
            }
            String(fallSuccessBytes) -> {
                if (lockStatus != 2) {
                    fragment.operateResult(Constant.SEND_LOCK_FALL_SUCCESS, "下降")
                    lockStatus = 2
                }
            }
            String(lockFallStatusBytes) -> {
                if (lockStatus != 2) {
                    fragment.operateResult(Constant.LOCK_FALL_STATUS, "下降")
                    lockStatus = 2
                }
            }
            String(lockRiseStatusBytes) -> {
                if (lockStatus != 1) {
                    fragment.operateResult(Constant.LOCK_RISE_STATUS, "上升")
                    lockStatus = 1
                }
            }
            String(lockMoveStatusBytes) -> {
                if (lockStatus != 0) {
                    fragment.operateResult(Constant.LOCK_MOVE_STATUS, "运动")
                    lockStatus = 0
                }
            }
            String(lockFirstStatusBytes) -> {
                if (lockStatus != -1) {
                    fragment.operateResult(Constant.LOCK_FIRST_STATUS, "初始")
                    lockStatus = -1
                }
            }
            else -> {
                if (byteArray[1].toInt() == 0x02 && byteArray[2].toInt() == 0x30) {
                    val dis = MathUtils.disMath(byteArray[3], byteArray[4]) * 0.01
                    fragment.operateResult(Constant.DISTANCE_INFO, dis)
                } else if (byteArray[1].toInt() == 0x00 && byteArray[2].toInt() == 0x30) {
                    val power = MathUtils.disPower(byteArray[3], byteArray[4])
                    if (powerStatus != power) {
                        powerStatus = power
                        fragment.operateResult(Constant.POWER_STATUS, powerStatus)
                    }
                }
            }
        }
    }
}