package com.ds.usrToos

import android.app.Activity
import android.graphics.*
import android.hardware.usb.UsbDevice
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.ds.utils.HttpsUtils
import com.ds.view.ControlFragment
import com.jiangdg.usbcamera.UVCCameraHelper
import com.serenegiant.usb.common.AbstractUVCCameraHandler
import com.serenegiant.usb.widget.CameraViewInterface
import kotlinx.coroutines.delay
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
    var mPhotoByte: ByteArray? = null
    override fun onPreviewResult(p0: ByteArray?) {
        if (isPriview && p0 != null) {
            mPhotoByte = p0
            listener.cameraBytesListener(p0)
        }
    }

    private fun yuv2Bitmap(yuv: ByteArray, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val bos = ByteArrayOutputStream(yuv.size)
            val yuvImage = YuvImage(yuv, ImageFormat.NV21, width, height, null)
            val success = yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, bos)
            if (success) {
                val buffer = bos.toByteArray()
                bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
            }
            bos.flush()
            bos.close()
        } catch (e: Throwable) {

        }
        return bitmap;
    }

    fun compressImage() {
        if (mPhotoByte == null) return
        val bitmap = yuv2Bitmap(mPhotoByte!!, mPreviewWidth, mPreviewHeight)
        mPhotoByte = null
        try {
            Toast.makeText(fragment.activity, "bitmap == ", Toast.LENGTH_SHORT).show()
            val dir = Environment.getExternalStorageDirectory().absolutePath + "/ds/pic"
            val fileDir = File(dir)
            if (!fileDir.exists() || !fileDir.isDirectory) fileDir.mkdirs()
            val picPath = dir + File.separator + "cameraPic.jpg"
            val file = File(picPath)
            val fos = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle();
            }
            fos.flush();
            fos.close();
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