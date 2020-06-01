package com.ds.utils

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.aiwinn.carbranddect.App
import com.ice.entity.PlateRecognitionParameter
import com.ice.iceplate.ActivateService
import com.ice.iceplate.RecogService

class CarRecogUtils(context: Context) {
    var acBinder: ActivateService.ActivateBinder? = null
    val acConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            acBinder = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            if (p1 is ActivateService.ActivateBinder) acBinder = p1
            if (login()) bindRecogService(context)
        }
    }

    private fun bindLoginService(context: Context) {
        try {
            val actiIntent = Intent(context, ActivateService::class.java)
            context.bindService(actiIntent, acConnection, Service.BIND_AUTO_CREATE)
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e(CarRecogUtils::class.java.name, "bindLoginService exception == ${e.message}")
        }
    }

    fun unBindLoginService(context: Context) {
        context.unbindService(acConnection)
    }

    fun login(): Boolean {
        val recof = SharPUtils.getData("recog", "LVEZOUIDHAYWKMSR")
        val acResult = acBinder?.login(recof)
//        val acResult = acBinder?.login("XAKO7MCQHMYWLNAV")
        Log.e(CarRecogUtils::class.java.name, "acResult == $acResult")
        when (acResult) {
            0 -> Toast.makeText(App.context, "恭喜,程序激活成功!", Toast.LENGTH_SHORT).show()
            1795 -> Toast.makeText(App.context, "程序激活失败,激活的机器数量已达上限，授权码不能在更多的机器 上使用", Toast.LENGTH_SHORT).show()
            1793 -> Toast.makeText(App.context, "程序激活失败,授权码已过期", Toast.LENGTH_SHORT).show()
            276 -> Toast.makeText(App.context, "程序激活失败,没有找到相应的本地授权许可数据文件", Toast.LENGTH_SHORT).show()
            284 -> Toast.makeText(App.context, "程序激活失败,授权码输入错误，请检查授权码拼写是否正确", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(App.context, "程序激活失败,错误码为：$acResult", Toast.LENGTH_SHORT).show()
        }
        return acResult == 0
    }

    var recogBinder: RecogService.MyBinder? = null
    var iInitPlateIDSDK: Int? = null
    val recogConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            recogBinder = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            if (p1 is RecogService.MyBinder) recogBinder = p1
            iInitPlateIDSDK = recogBinder?.initPlateIDSDK
            if (iInitPlateIDSDK == null || iInitPlateIDSDK != 0) {
                val str = arrayOf("" + iInitPlateIDSDK)
                Toast.makeText(App.context, "识别服务开启 $iInitPlateIDSDK", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(App.context, "识别服务开启成功 $iInitPlateIDSDK", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun bindRecogService(context: Context) {
        val authIntent = Intent(context, RecogService::class.java)
        context.bindService(authIntent, recogConn, Service.BIND_AUTO_CREATE)
    }

    fun unBindRecogService(context: Context) {
        context.unbindService(recogConn)
    }

    fun getRecogResult(byteArray: ByteArray): RecogDetailBean? {
        val prop = PlateRecognitionParameter()
        prop.height = 720
        prop.width = 1280
        prop.picByte = byteArray
        prop.plateIDCfg.left = 0
        prop.plateIDCfg.top = 0
        prop.plateIDCfg.bottom = 720
        prop.plateIDCfg.right = 1280
        prop.plateIDCfg.bRotate = 0
        val fieldvalue = recogBinder?.doRecogDetail(prop)
        return if (fieldvalue != null) {
            RecogDetailBean(fieldvalue[0], fieldvalue[1])
        } else null
    }

    data class RecogDetailBean(val plate_number: String, val plate_color: String)

    init {
        bindLoginService(context)
    }
}