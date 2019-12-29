package com.ds.usrToos

import android.app.Activity
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
    private var isPriview = false
    fun init(activity: Activity) {
        try {
            Log.e(tag, "init start")
            mCameraHelper.setDefaultPreviewSize(mPreviewWidth, mPreviewHeight)
            previewView.aspectRatio = (mPreviewWidth / mPreviewHeight.toFloat()).toDouble()
            mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG)
            mCameraHelper.initUSBMonitor(activity, previewView, this)

            mCameraHelper.registerUSB()
            Log.e(tag, "init end")
        } catch (e: Exception) {
            Log.e(tag, "init exception == ${e.message}")
        }
    }

    fun isOpen() = isPriview
    suspend fun start(): Boolean {
        return try {
            Log.e(tag, "start")
            if (!isPriview) {
                mCameraHelper.stopPreview()
                delay(500)
                mCameraHelper.setOnPreviewFrameListener(this)
                delay(500)
                mCameraHelper.requestPermission(0)
                isPriview = true
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "start exception == ${e.message}")
            false
        }
    }

    fun stop(): Boolean {
        return try {
            Log.e(tag, "stop")
            mCameraHelper.stopPreview()
            isPriview = false
            true
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
        CarBrandManager.distinguishByte(p0, mPreviewWidth, mPreviewHeight, 0, ProbeCardListener { bitmap, has ->
            Log.e("onPreviewFrame", "probeResult  bitmap >> " + bitmap.isRecycled)
            Log.e("onPreviewFrame", "probeResult has >> $has")
            if (has) {
                CarBrandManager.distinguishBitmap(bitmap, listener)
            }
        })
    }

    override fun onDisConnectDev(p0: UsbDevice?) {
        Log.e(tag, "onDisConnectDev")
    }

    override fun onAttachDev(p0: UsbDevice?) {
        Log.e(tag, "onAttachDev")
    }

    override fun onConnectDev(p0: UsbDevice?, p1: Boolean) {
        Log.e(tag, "onConnectDev")
    }

    override fun onDettachDev(p0: UsbDevice?) {
        Log.e(tag, "onDettachDev")
    }
}