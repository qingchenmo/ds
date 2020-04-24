package com.ds.usrToos

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.util.Log
import com.aiwinn.carbranddect.CarBrandManager
import com.aiwinn.carbranddect.able.DistinguishListener
import com.aiwinn.carbranddect.able.ProbeCardListener
import com.ds.view.ControlFragment
import com.jiangdg.usbcamera.UVCCameraHelper
import com.serenegiant.usb.common.AbstractUVCCameraHandler
import com.serenegiant.usb.widget.CameraViewInterface
import kotlinx.coroutines.delay

class CameraUtils(private val listener: DistinguishListener, private val previewView: CameraViewInterface, val fragment: ControlFragment)
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
                    isPriview = true
                    mCameraHelper.stopPreview()
                    delay(700)
                    mCameraHelper.setOnPreviewFrameListener(this)
                    delay(500)
                    mCameraHelper.requestPermission(0)
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

    override fun onPreviewResult(p0: ByteArray?) {
        if (isPriview) {
            CarBrandManager.distinguishByte(p0, mPreviewWidth, mPreviewHeight, 0, ProbeCardListener { bitmap, has ->
                Log.e("onPreviewFrame", "probeResult  bitmap >> " + bitmap.isRecycled)
                Log.e("onPreviewFrame", "probeResult has >> $has")
                if (has) {
                    CarBrandManager.distinguishBitmap(bitmap, listener)
                }else{
                    val bit = Bitmap.createBitmap(bitmap)
                }
            })
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
}