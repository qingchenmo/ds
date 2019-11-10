package com.ds.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.aiwinn.carbranddect.CarBrandManager;
import com.aiwinn.carbranddect.able.DistinguishListener;
import com.aiwinn.carbranddect.able.ProbeCardListener;
import com.aiwinn.carbranddect.utils.LogUtils;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.util.List;

public class UVCCameraInterface {

    private static final String TAG = "UVCCameraInterface";
    private static final UVCCameraInterface instance = new UVCCameraInterface();
    public int mPreviewWidth = 1280;
    public int mPreviewHeight = 720;
    public boolean custom;
    private CameraViewInterface mPreviewView;
    private boolean isPreview;
    private boolean isRequest;
    private UVCCameraHelper mCameraHelper;
    private int openTime = 3;
    private Handler handler;

    private CameraViewInterface.Callback mCallback = new CameraViewInterface.Callback() {
        @Override
        public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
            if (!isPreview && mCameraHelper.isCameraOpened()) {
                LogUtils.d(TAG, "startPreview");
                mCameraHelper.startPreview(mPreviewView);
                isPreview = true;
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int i, int i1) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
            if (isPreview && mCameraHelper.isCameraOpened()) {
                LogUtils.d(TAG, "stopPreview");
                mCameraHelper.stopPreview();
                isPreview = false;
            }
        }
    };
    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            LogUtils.d(TAG, "onAttachDev");
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                Toast.makeText(mActivity, "没有连接到摄像头", Toast.LENGTH_SHORT).show();
                LogUtils.d(TAG, "check no usb camera");
                return;
            }
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    LogUtils.d(TAG, "requestPermission");
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            LogUtils.d(TAG, "onDettachDev");
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                Toast.makeText(mActivity, device.getDeviceName() + " is out", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            LogUtils.d(TAG, "onConnectDev");
            if (!isConnected) {
                Toast.makeText(mActivity, "fail to connect,please check resolution params", Toast.LENGTH_SHORT).show();
                isPreview = false;
            } else {
                isPreview = true;
                // need to wait UVCCamera initialize over
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            LogUtils.d(TAG, "onDisConnectDev");
            mCameraHelper.closeCamera();
            isRequest = false;
        }
    };
    private Activity mActivity;
    private DistinguishListener mListener;

    public static UVCCameraInterface getInstance() {
        return instance;
    }

    public void setOnDistinguishListener(DistinguishListener listener) {
        if (listener == null) {
            LogUtils.e("openCamera listener is null");
            return;
        }
        this.mListener = listener;
    }

    public void init(Activity activity, CameraViewInterface previewView) {
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
            mCameraHelper.stopPreview();
            mCameraHelper.closeCamera();
            mCameraHelper.release();
        }
        mActivity = activity;
        mPreviewView = previewView;
        mPreviewView.setCallback(mCallback);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultPreviewSize(mPreviewWidth, mPreviewHeight);
        mPreviewView.setAspectRatio(mPreviewWidth / (float) mPreviewHeight);
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        handler = new Handler() {
            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
                openTime++;
                handler.sendEmptyMessageDelayed(50, 1000);
            }
        };
    }

    public void open() {
        mCameraHelper.initUSBMonitor(mActivity, mPreviewView, listener);
        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                CarBrandManager.distinguishByte(nv21Yuv, mPreviewWidth, mPreviewHeight, 0, new ProbeCardListener() {

                    @Override
                    public void probeResult(Bitmap bitmap, boolean has) {
                        Log.e("onPreviewFrame", "probeResult  bitmap >> " + bitmap.isRecycled());
                        Log.e("onPreviewFrame", "probeResult has >> " + has);
                        if (has) {
                            CarBrandManager.distinguishBitmap(bitmap, mListener);
                        }
                    }
                });
            }
        });
        mCameraHelper.registerUSB();
    }

    public void close() {
        if (mCameraHelper != null) {
            mCameraHelper.closeCamera();
            mCameraHelper.unregisterUSB();
        }
    }

    public void release() {
        if (mCameraHelper != null) {
            mCameraHelper.closeCamera();
            mCameraHelper.release();
        }
    }


    public boolean isCameraOpened() {
        if (mCameraHelper != null && mCameraHelper.isCameraOpened() && isPreview) {
            return true;
        }
        return false;
    }

    public void registerUSB() {
        if (mCameraHelper != null) {
            Log.e("camera", "registerUSB==");
            mCameraHelper.registerUSB();
        }
    }

    public void resetTime() {
        openTime = 0;
        handler.removeMessages(50);
    }

    public void startTime() {
        openTime = 0;
        handler.removeMessages(50);
        handler.sendEmptyMessageDelayed(50, 1000);
    }

    public void releaseCamera() {
        if (mCameraHelper != null) {
            mPreviewView.onPause();
            mCameraHelper.getUSBMonitor().destroy();
            mCameraHelper.release();
        }
    }

    public void setPreViewSize(int var1, int var2) {
        Log.w(TAG, "width = " + var1 + ",height = " + var2);
        if (var1 != 0 && var2 != 0) {
            this.mPreviewWidth = var1;
            this.mPreviewHeight = var2;
            this.custom = true;
        } else {
            this.custom = false;
        }

    }

    public int getPreviewWidth() {
        return this.mPreviewWidth;
    }

    public int getPreviewHeight() {
        return this.mPreviewHeight;
    }

    public List<Size> getSupportedPreviewSizes() {
        if (!isCameraOpened()) {
            Toast.makeText(mActivity, "sorry, uvc camera open failed", Toast.LENGTH_SHORT).show();
            return null;
        }
        return mCameraHelper.getSupportedPreviewSizes();
    }

    public void updateResolution(int widht, int height) {
        if (!isCameraOpened()) {
            return;
        }
        mCameraHelper.updateResolution(widht, height);
    }

    public int getOpenTime() {
        return openTime;
    }
}
