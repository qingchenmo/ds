package com.aiwinn.carbranddect;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.aiwinn.carbranddect.able.DistinguishListener;
import com.aiwinn.carbranddect.able.ProbeCardListener;
import com.aiwinn.carbranddect.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZeQiu.Wang on 2017/12/29.
 */

public class CarBrandCameraPreview extends FrameLayout implements SurfaceHolder.Callback, Camera.PreviewCallback, View.OnTouchListener, Camera.AutoFocusCallback {
    private int mOrientation; // 旋转角度
    private int openCamera = Camera.CameraInfo.CAMERA_FACING_BACK; // 默认开启后置摄像头
    private int cameraRotate = 90; // 默认旋转90°
    private int mPreviewWidth;
    private int mPreviewHeight;

    private Context mContext;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera camera = null;
    private Point scWH;

    private Camera.Parameters mParameters;
    private DistinguishListener mListener;

    private boolean HorVer;

    public int getOpenCamera() {
        return openCamera;
    }

    public void setOpenCamera(int openCamera) {
        this.openCamera = openCamera;
    }

    public int getCameraRotate() {
        return cameraRotate;
    }

    public void setCameraRotate(int cameraRotate) {
        this.cameraRotate = cameraRotate;
    }

    public CarBrandCameraPreview(Context context) {
        this(context, null);
    }

    public CarBrandCameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarBrandCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContext = getContext();
        scWH = ScreenUtils.getScreenMetrics(mContext);
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.setOnTouchListener(this);
    }

    /**
     * 修改Surface大小
     * <p>
     * TODO  需要修改
     *
     * @param point
     */
    private void setSurfaceSize(Point point) {
        Resources resources = this.getResources();
        LayoutParams layoutParams = (LayoutParams) mSurfaceView.getLayoutParams();
//        if (HorVer) {
//            //竖屏
//            layoutParams.width = point.y;
//            layoutParams.height = (int) ((float) getMeasuredWidth() / point.y * point.x);
    //    }else {
            //横屏
            layoutParams.width = 1232;
         //   layoutParams.height = point.y;
     //   }
        mSurfaceView.setLayoutParams(layoutParams);
    }

    public void setOnDistinguishListener(DistinguishListener listener) {
        if (listener == null) {
            LogUtils.e("openCamera listener is null");
            return;
        }
        this.mListener = listener;
    }

    public void openCamera() {
        if(indexOfChild(mSurfaceView)==-1){
            setVisibility(VISIBLE);
            addView(mSurfaceView);
            initCamera();
        }
    }

    public void closeLockScreen() {
        setVisibility(GONE);
        removeView(mSurfaceView);
    }

    private void initCamera() {
        mHolder = mSurfaceView.getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);
    }

    /**
     * 处理异常
     *
     * @param exception
     */
    private void faceLockFindException(String where, String exception) {
        LogUtils.e(where + " / " + exception);
        closeLockScreen();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtils.d("slsu surfaceCreated");
        try {
            if (camera == null) {
                try {
                    LogUtils.d("slsu call camera.open method");
                    camera = Camera.open(openCamera);
                } catch (Exception e) {
                    e.printStackTrace();
                    faceLockFindException("slsu open camera", e.toString());
                }
            } else {
                LogUtils.d("slsu call camera.open method but camera is null");
                return;
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(openCamera, info);
            mOrientation = info.orientation;
            Configuration mConfiguration = this.getResources().getConfiguration();
            int ori = mConfiguration.orientation; //获取屏幕方向
            if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
                //横屏
                cameraRotate=0;
                HorVer=false;
            } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
                //竖屏
                cameraRotate=90;
                HorVer=true;
            }
            camera.setDisplayOrientation(cameraRotate);
            camera.setPreviewDisplay(holder);
            // 屏幕宽高

            //得到摄像头的参数
            mParameters = camera.getParameters();
            Point point = CameraParametersUtils.getSupportParameters(false, mParameters, scWH);
            if (point != null) {
                mPreviewWidth = point.x;
                mPreviewHeight = point.y;
            } else {
                mPreviewWidth = getMeasuredWidth();
                mPreviewHeight = getMeasuredHeight();
            }
            LogUtils.d("slsu size width is " + mPreviewWidth + " height is " + mPreviewHeight);
            setSurfaceSize(point);
            mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);//设置预览照片的大小\
            camera.setParameters(mParameters);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            faceLockFindException(" slsu surfaceCreated ", e.toString());
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtils.d("slsu surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtils.d("slsu surfaceDestroyed");
        releaseCamera();
    }

    public void releaseCamera() {
        if (camera != null) {
            mHolder.removeCallback(this);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        } else {
            LogUtils.e("slsu camera is null");
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        /**
         * mPreviewWidth 宽度
         * mPreviewHeight 高度
         * mOrientation 旋转角度
         * mListener 回掉
         */
        Configuration mConfiguration = this.getResources().getConfiguration();
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            //横屏
            mOrientation=0;
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
            mOrientation=90;
        }
        CarBrandManager.distinguishByte(data, mPreviewWidth, mPreviewHeight, mOrientation, new ProbeCardListener() {

            @Override
            public void probeResult(Bitmap bitmap, boolean has) {
                Log.e("onPreviewFrame", "probeResult  bitmap >> " + bitmap.isRecycled());
                Log.e("onPreviewFrame", "probeResult has >> " +has);
                if (has){
                    CarBrandManager.distinguishBitmap(bitmap,mListener);
                }
            }
        });
        CarBrandManager.distinguishByte(data, mPreviewWidth, mPreviewHeight, mOrientation, mListener);
    }

    private void focusOnTouch(int x, int y) {
        Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
        int left = rect.left * 2000 / mSurfaceView.getWidth() - 1000;
        int top = rect.top * 2000 / mSurfaceView.getHeight() - 1000;
        int right = rect.right * 2000 / mSurfaceView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / mSurfaceView.getHeight() - 1000;
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        focusOnRect(new Rect(left, top, right, bottom));
    }

    private void focusOnRect(Rect rect) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters(); // 先获取当前相机的参数配置对象
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            LogUtils.d("parameters.getMaxNumFocusAreas() : " + parameters.getMaxNumFocusAreas());
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(new Camera.Area(rect, 1000));
                parameters.setFocusAreas(focusAreas);
            }
            camera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
            camera.setParameters(parameters); // 一定要记得把相应参数设置给相机
            camera.autoFocus(this);
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        focusOnTouch((int) event.getX(), (int) event.getY());
        return false;
    }
}
