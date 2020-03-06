package com.ds.usrToos

import android_serialport_api.SerialPort
import com.ds.utils.LightUtils
import com.ds.view.ControlFragment
import kotlinx.coroutines.*

class UsrTool(private val fragment: ControlFragment) {
    private val mainScope = MainScope()
    private var serialPort: SerialPort? = null
    private var readBuffer = ByteArray(6)
    private var mSerialPortJob: Job? = null
    var jiaoYanSuccess = false

    fun open() {
        if (serialPort != null) return
        mSerialPortJob?.cancel()
        mSerialPortJob = mainScope.launch(Dispatchers.IO) {
            serialPort = SerialPort("/dev/ttyMT3", 9600, 0)
            val openEd = serialPort?.open() ?: false
            fragment.operateResult(Constant.LOG, "串口初始化 ${if (openEd) "成功" else "失败"}")
            async { startRead() }
        }
    }

    fun jiaoZhun() {
        mainScope.launch(Dispatchers.IO) {
            val result = serialPort?.write(Constant.wireJiaoZhun) ?: false
            openLight()
            delay(500)
            closeLight()
            delay(500)
            openLight()
            delay(500)
            closeLight()
            delay(500)
            openLight()
            delay(500)
            closeLight()

        }
    }

    fun release() {
        mainScope.cancel()
        serialPort?.release()
        serialPort = null
    }

    fun openLight() {
        mainScope.launch(Dispatchers.IO) {
                val result = LightUtils.PowerControl(handle = 1)
//            val result = serialPort?.write(Constant.openLightBytes) ?: false
            fragment.operateResult(Constant.SEND_OPEN_LIGHT, result)
            if (!result) reset()
        }
    }

    fun closeLight() {
        mainScope.launch(Dispatchers.IO) {
            val result = LightUtils.PowerControl(handle = 0)
//            val result = serialPort?.write(Constant.closeLightBytes) ?: false
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
                        delay(600)
                    }
                    6 -> parseReadBuffer(readBuffer)
                }
            }
        }
    }

    private suspend fun parseReadBuffer(byteArray: ByteArray) {
        val newdis = String(byteArray)
        if (newdis.contains('m')) {
            dis = newdis
            try {
                val disArray = dis.split('m')
                if (disArray.isNotEmpty()) {
                    jiaoYanSuccess = true
                    fragment.operateResult(Constant.DISTANCE_INFO, disArray[0].toDouble())
                }
            } catch (e: Throwable) {

            }
        }
    }

    var dis = ""
}