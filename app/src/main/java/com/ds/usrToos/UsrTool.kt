package com.ds.usrToos

import android.graphics.Bitmap
import android_serialport_api.SerialPort
import com.ds.utils.HttpsUtils
import com.ds.utils.LightUtils
import com.ds.view.ControlFragment
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

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
            val result = LightUtils.PowerControl(num = 4, handle = 1)
            LightUtils.PowerControl(num = 5, handle = 1)
//            val result = serialPort?.write(Constant.openLightBytes) ?: false
            fragment.operateResult(Constant.SEND_OPEN_LIGHT, result)
            if (!result) reset()
        }
    }

    fun closeLight() {
        mainScope.launch(Dispatchers.IO) {
            val result = LightUtils.PowerControl(num = 5, handle = 0)
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
    companion object{
        fun writeLog(byteArray: String) {
            try {
                val dir = "/sdcard/ds/log"
                val fileDir = File(dir)
                if (!fileDir.exists() || !fileDir.isDirectory) fileDir.mkdirs()
                else if (fileDir.length() > 1024 * 1024 * 5) {
                    fileDir.delete()
                }
                val picPath = dir + File.separator + "usrTool.txt"
                val fos = FileWriter(picPath,true)
                fos.write(getTime()+"           ")
                fos.write(byteArray)
                fos.write("\r\n")
                fos.flush()
                fos.close()
            } catch (e: Throwable) {
            }
        }
        private fun getTime():String{
            val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val curDate = Date(System.currentTimeMillis())
            val str = formatter.format(curDate)
            return str
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
                    writeLog(newdis)
                }
            } catch (e: Throwable) {

            }
        }
    }

    var dis = ""
}