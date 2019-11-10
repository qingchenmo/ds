package com.ds.view

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class CameraView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    companion object {
        private const val TAG = "CameraView"
    }


    private var mHolder: SurfaceHolder? = null
    private val mCamera: Camera? = null


    init {
        //得到SurfaceHolder对象
        mHolder = holder
        //添加回调，得到Surface的三个声明周期方法
        mHolder?.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        if (holder.surface == null) {
            return
        }
        //停止预览效果
        mCamera?.stopPreview()
        //重新设置预览效果
        try {
            mCamera?.setPreviewDisplay(mHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mCamera?.startPreview()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        try {
            //设置预览方向
            mCamera?.setDisplayOrientation(90)
            //把这个预览效果展示在SurfaceView上面
            mCamera?.setPreviewDisplay(holder)
            //开启预览效果
            mCamera?.startPreview()
        } catch (e: IOException) {
            Log.e(TAG, "surfaceCreated exception==${e.message}")
        }

    }
}