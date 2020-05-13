package com.ds.usrToos

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.util.Log
import com.aiwinn.carbranddect.CarBrandManager
import com.aiwinn.carbranddect.able.DistinguishListener
import com.aiwinn.carbranddect.able.ProbeCardListener
import com.ds.utils.HttpsUtils
import com.ds.view.ControlFragment
import com.ds.view.MainActivity
import com.jiangdg.usbcamera.UVCCameraHelper
import com.serenegiant.usb.common.AbstractUVCCameraHandler
import com.serenegiant.usb.widget.CameraViewInterface
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class CameraUtils(private val listener: OnCameraBytesListener, private val previewView: CameraViewInterface, val fragment: ControlFragment)
    : UVCCameraHelper.OnMyDevConnectListener, AbstractUVCCameraHandler.OnPreViewResultListener {
    private val tag = "CameraUtils"
    private val mCameraHelper = UVCCameraHelper.getInstance()
    private val mPreviewWidth = 1280
    private val mPreviewHeight = 720
    var isPriview = false
    private var onAttachDev = false
    fun init(activity: Activity) {
        try {
            Log.e(tag, "init start")
            mCameraHelper.setDefaultPreviewSize(mPreviewWidth, mPreviewHeight)
            previewView.aspectRatio = (mPreviewWidth / mPreviewHeight.toFloat()).toDouble()
            mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG)
            mCameraHelper.initUSBMonitor(activity, previewView, this)
            mCameraHelper.setOnPreviewFrameListener(this)
            mCameraHelper.registerUSB()
            Log.e(tag, "init end")
        } catch (e: Exception) {
            Log.e(tag, "init exception == ${e.message}")
        }
    }

    fun isOpen() = isPriview

    @Synchronized
    suspend fun start(): Boolean {
        return try {
            Log.e(tag, "start")
            if (onAttachDev) {
                if (!isPriview) {
                    mCanUp = true
                    isPriview = true
                    mCameraHelper.stopPreview()
                    delay(700)
                    mCameraHelper.setOnPreviewFrameListener(this)
                    delay(500)
                    mCameraHelper.requestPermission(0)
                    mCanUp = true
                }
                true
            } else false
        } catch (e: Exception) {
            Log.e(tag, "start exception == ${e.message}")
            false
        }
    }

    @Synchronized
    fun stop(): Boolean {
        return try {
            if (!onAttachDev) false
            else {
                isPriview = false
                Log.e(tag, "stop")
                mCameraHelper.stopPreview()
                true
            }
        } catch (e: Exception) {
            Log.e(tag, "stop exception == ${e.message}")
            false
        }
    }

    fun release() {
        try {
            mCameraHelper.closeCamera()
            mCameraHelper.unregisterUSB()
            mCameraHelper.release()
        } catch (e: Exception) {

        }
    }

    var mCanUp = false
    override fun onPreviewResult(p0: ByteArray?) {
        if (isPriview && p0 != null) {
            listener.cameraBytesListener(p0)
            /*CarBrandManager.distinguishByte(p0, mPreviewWidth, mPreviewHeight, 0, ProbeCardListener { bitmap, has ->
                Log.e("onPreviewFrame", "probeResult  bitmap >> " + bitmap.isRecycled)
                Log.e("onPreviewFrame", "probeResult has >> $has")
                if (has && mCanUp) {
                    mCanUp = false
                    CarBrandManager.distinguishBitmap(bitmap, listener)
                    MainScope().async(Dispatchers.IO) {
                        try {
                            val bit = Bitmap.createBitmap(bitmap)
                            val tempBitmap = bit.copy(Bitmap.Config.ARGB_8888, false)
                            val dir = "/sdcard/aiwinn/ds/pic"
                            val file = File(dir)
                            if (!file.exists() || !file.isDirectory) file.mkdirs()
                            val picPath = dir + "cameraPic.jpg"
                            compressImage(tempBitmap)
                        }catch (e:Throwable){

                        }
                    }
                }
            })*/
        }
    }

    private fun compressImage(bitmap: Bitmap) {
        try {
            val baos = ByteArrayOutputStream()
            var options = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
            while (baos.toByteArray().size / 1024 > 100) {
                baos.reset()
                options -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos)
            }
            val dir = "/sdcard/aiwinn/ds/pic"
            val fileDir = File(dir)
            if (!fileDir.exists() || !fileDir.isDirectory) fileDir.mkdirs()
            else if (fileDir.length() > 1024 * 1024 * 20) {
                fileDir.delete()
            }
            val picPath = dir + File.separator + System.currentTimeMillis() + "cameraPic.jpg"
            val file = File(picPath)
            val fos = FileOutputStream(file)
            fos.write(baos.toByteArray())
            fos.flush()
            fos.close()
            bitmap.recycle()
            HttpsUtils.imageRecFailure(file)
        } catch (e: Throwable) {
        }
    }

    override fun onDisConnectDev(p0: UsbDevice?) {
        Log.e(tag, "onDisConnectDev")
    }

    override fun onAttachDev(p0: UsbDevice?) {
        Log.e(tag, "onAttachDev")
        if (mCameraHelper.usbDeviceCount > 0) {
            mCameraHelper.requestPermission(0)
            onAttachDev = true
        }
    }

    override fun onConnectDev(p0: UsbDevice?, p1: Boolean) {
        Log.e(tag, "onConnectDev")
    }

    override fun onDettachDev(p0: UsbDevice?) {
        Log.e(tag, "onDettachDev")
    }

    interface OnCameraBytesListener {
        fun cameraBytesListener(byteArray: ByteArray)
    }
}