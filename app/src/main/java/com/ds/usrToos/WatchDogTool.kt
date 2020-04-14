package com.ds.usrToos

import android_serialport_api.SerialPort
import com.ds.utils.LightUtils
import com.ds.view.ControlFragment
import kotlinx.coroutines.*

class WatchDogTool {
    private val mainScope = MainScope()
    private var serialPort: SerialPort? = null
    private var readBuffer = ByteArray(6)
    private var mSerialPortJob: Job? = null

    fun open() {
        if (serialPort != null) return
        mSerialPortJob?.cancel()
        mSerialPortJob = mainScope.launch(Dispatchers.IO) {
            serialPort = SerialPort("/dev/ttyMT2", 9600, 0)
            val openEd = serialPort?.open() ?: false
            async { startWatch() }
        }
    }

    private suspend fun startWatch() {
        withContext(Dispatchers.IO) {
            serialPort?.write(Constant.watchDog)
            delay(3000)
            startWatch()
        }
    }

    fun release() {
        mainScope.cancel()
        serialPort?.release()
        serialPort = null
    }

    private suspend fun startRead() {
        withContext(Dispatchers.IO) {
            while (isActive) {
                readBuffer = ByteArray(6)
                when (serialPort?.read(readBuffer) ?: -1) {
                    0 -> {
                        delay(600)
                    }
                }
            }
        }
    }

    private suspend fun parseReadBuffer(byteArray: ByteArray) {
    }
}