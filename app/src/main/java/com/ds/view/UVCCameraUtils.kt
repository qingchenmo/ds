package com.ds.view

import android.app.Activity
import android.hardware.usb.UsbDevice
import android.util.Log
import android.view.Surface
import android.widget.Toast
import com.aiwinn.carbranddect.CarBrandManager
import com.aiwinn.carbranddect.able.DistinguishListener
import com.aiwinn.carbranddect.able.ProbeCardListener
import com.aiwinn.carbranddect.utils.LogUtils
import com.jiangdg.usbcamera.UVCCameraHelper
import com.serenegiant.usb.widget.CameraViewInterface
import com.serenegiant.usb.widget.UVCCameraTextureView

class UVCCameraUtils private constructor() {
    companion object {
        var instance = UVCCameraUtils()
        private const val TAG = "UVCCameraUtils"
    }

    var mPreviewWidth = 1280
    var mPreviewHeight = 720
    private var mPreviewView: UVCCameraTextureView? = null
    private var mCameraHelper: UVCCameraHelper? = null
    private var mActivity: Activity? = null
    private var mCameraInitSuccess = false
    private var mListener: DistinguishListener? = null
    private var isRequest: Boolean = false

    private val mCallback = object : CameraViewInterface.Callback {
        override fun onSurfaceCreated(cameraViewInterface: CameraViewInterface, surface: Surface) {
            if (mCameraHelper != null && mCameraHelper!!.isCameraOpened) {
                LogUtils.d(TAG, "startPreview")
                mCameraHelper?.startPreview(mPreviewView)
            }
        }

        override fun onSurfaceChanged(cameraViewInterface: CameraViewInterface, surface: Surface, i: Int, i1: Int) {

        }

        override fun onSurfaceDestroy(cameraViewInterface: CameraViewInterface, surface: Surface) {
            if (mCameraHelper != null && mCameraHelper!!.isCameraOpened) {
                LogUtils.d(TAG, "stopPreview")
                mCameraHelper?.stopPreview()
            }
        }
    }

    private val listener = object : UVCCameraHelper.OnMyDevConnectListener {
        override fun onAttachDev(device: UsbDevice) {
            LogUtils.d(TAG, "onAttachDev")
            /*if (!bingCamera()) {
                Toast.makeText(mActivity, "没有获取到相机", Toast.LENGTH_SHORT).show()
                return
            }*/

            if (!bingCamera()) {
                Toast.makeText(mActivity, "没有获取到相机", Toast.LENGTH_SHORT).show()
                return
            }
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    LogUtils.d(TAG, "requestPermission")
                    LogUtils.d(TAG, "requestPermission");
                    mCameraHelper?.requestPermission(0);
                }
            }

//            Toast.makeText(mActivity, "onAttachDev", Toast.LENGTH_SHORT).show()
//            LogUtils.d(TAG, "onAttachDev111111111")
//            mCameraHelper?.requestPermission(0)
        }

        override fun onDettachDev(device: UsbDevice) {
            LogUtils.d(TAG, "onDettachDev")
            if (isRequest) {
                isRequest = false
                mCameraHelper?.closeCamera()
                Toast.makeText(mActivity, device.deviceName + " is out", Toast.LENGTH_SHORT).show()
            }

        }

        override fun onConnectDev(device: UsbDevice, isConnected: Boolean) {
            LogUtils.d(TAG, "onConnectDev")
            if (!isConnected) {
                Toast.makeText(mActivity, "fail to connect,please check resolution params", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mActivity, "connecting", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onDisConnectDev(device: UsbDevice) {
            LogUtils.d(TAG, "onDisConnectDev")
            Toast.makeText(mActivity, "disconnecting", Toast.LENGTH_SHORT).show()
            mCameraHelper?.closeCamera()
            isRequest = false
        }
    }

    fun bingCamera(): Boolean {
        return mCameraHelper == null || mCameraHelper!!.usbDeviceCount > 0
    }

    fun init(activity: Activity, previewView: UVCCameraTextureView, cameraListener: DistinguishListener) {
        mCameraHelper?.unregisterUSB()
        mCameraHelper?.stopPreview()
        mCameraHelper?.closeCamera()
        mCameraHelper?.release()
        mActivity = activity
        mPreviewView = previewView
        mPreviewView?.setCallback(mCallback)
        mCameraHelper = UVCCameraHelper.getInstance()
        mCameraHelper?.setDefaultPreviewSize(mPreviewWidth, mPreviewHeight)
        mPreviewView?.aspectRatio = (mPreviewWidth / mPreviewHeight.toFloat()).toDouble()
        mCameraHelper?.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG)
        this.mListener = cameraListener
        mCameraHelper?.initUSBMonitor(mActivity, mPreviewView, listener)
        Toast.makeText(activity, "Camera init", Toast.LENGTH_SHORT).show()
    }

    fun open() {
//        if (bingCamera()) return
        Toast.makeText(mActivity, "onAttachDev", Toast.LENGTH_SHORT).show()
        mCameraHelper?.setOnPreviewFrameListener { nv21Yuv ->
            CarBrandManager.distinguishByte(nv21Yuv, mPreviewWidth, mPreviewHeight, 0, ProbeCardListener { bitmap, has ->
                Log.e("onPreviewFrame", "probeResult  bitmap >> " + bitmap.isRecycled)
                Log.e("onPreviewFrame", "probeResult has >> $has")
                if (has) {
                    CarBrandManager.distinguishBitmap(bitmap, mListener)
                }
            })
        }
        UVCCameraInterface.getInstance().registerUSB()
    }

    fun unregisterUSB() {
        Log.i(TAG, "close camera")
        /*if (bingCamera()) */mCameraHelper?.unregisterUSB()
        mCameraHelper?.stopPreview()
        mCameraHelper?.closeCamera()
        mCameraHelper?.release()
    }

}