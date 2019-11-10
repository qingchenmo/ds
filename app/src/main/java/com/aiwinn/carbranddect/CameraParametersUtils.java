package com.aiwinn.carbranddect;

import android.graphics.Point;
import android.hardware.Camera;

import com.aiwinn.carbranddect.utils.LogUtils;

import java.util.List;

/**
 * com.aiwinn.facelocksdk.Utils
 * 2017/09/19
 * Created by LeoLiu on User.
 */

public class CameraParametersUtils {

    public static Point getSupportParameters(boolean tag, Camera.Parameters parameters, Point mPoint) {
        boolean isSize = false;
        int mPreviewWidth = mPoint.y;
        int mPreviewHeight = mPoint.x;
        LogUtils.d("screen .width " + mPreviewWidth + "screen.height " + mPreviewHeight);
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
//        for (Camera.Size size : sizeList) {
//            if(size.width==size.height){
//                return new Point(size.width, size.height);
//            }
//        }






        if (sizeList.size() > 1) {
            if (tag) {
                for (int i = 0; i < sizeList.size() - 1; i++) {
                    for (int j = 0; j < sizeList.size() - 1 - i; j++) {
                        if (sizeList.get(j).width < sizeList.get(j + 1).width) {
                            Camera.Size cur = sizeList.get(j);
                            sizeList.get(j).width = sizeList.get(j + 1).width;
                            sizeList.get(j).height = sizeList.get(j + 1).height;
                            sizeList.get(j + 1).width = cur.width;
                            sizeList.get(j + 1).height = cur.height;
                        }
                    }
                }
                for (int i = 0; i < sizeList.size(); i++) {
                    if (i == 0) {
                        mPreviewWidth = sizeList.get(i).width;
                        mPreviewHeight = sizeList.get(i).height;
                    } else {
                        if (sizeList.get(i).width < mPreviewWidth && sizeList.get(i).width >= 320) {
                            mPreviewWidth = sizeList.get(i).width;
                            mPreviewHeight = sizeList.get(i).height;
                        }
                    }
                }
                isSize = true;
            } else {
                int width = Math.abs(sizeList.get(0).width - mPoint.y);
                int height = Math.abs(sizeList.get(0).height - mPoint.x);
                for (Camera.Size cur : sizeList) {
                    LogUtils.d("one  size() = 1 cur.width " + cur.width + "cur.height " + cur.height);
                    int w = Math.abs(cur.width - mPoint.y);
                    if (w <= width) {
                        width = w;
                        if (width == 0) {
                            mPreviewWidth = cur.width;
                            int h = Math.abs(cur.height - mPoint.x);
                            if (h <= height) {
                                isSize = true;
                                mPreviewHeight = cur.height;
                                height = h;
                                if (height == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!isSize) {
                     width = Math.abs(sizeList.get(0).width - mPoint.y);
                     height = Math.abs(sizeList.get(0).height - mPoint.x);
                    for (Camera.Size cur : sizeList) {
                        LogUtils.d("two  size() = 1 cur.width " + cur.width + "cur.height " + cur.height);
                        int h = Math.abs(cur.height - mPoint.x);
                        if (h <= height) {
                            height = h;
                            if (height == 0) {
                                mPreviewHeight = cur.height;
                                int w = Math.abs(cur.width - mPoint.y);
                                if (w <= width) {
                                    isSize = true;
                                    mPreviewWidth = cur.width;
                                    width = w;
                                    if (width == 0) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }


                if (!isSize) {
                    for (Camera.Size cur : sizeList) {
                        if (cur.width > mPreviewWidth) {
                            mPreviewWidth = cur.width;
                            mPreviewHeight = cur.height;
                            isSize = true;
                            break;
                        }
                    }
                    if (!isSize) {
                        if (sizeList.get(sizeList.size() - 1).width > sizeList.get(0).width) {
                            Camera.Size cur = sizeList.get(sizeList.size() - 1);
                            mPreviewWidth = cur.width;
                            mPreviewHeight = cur.height;
                        } else {
                            Camera.Size cur = sizeList.get(0);
                            mPreviewWidth = cur.width;
                            mPreviewHeight = cur.height;
                        }
                        isSize = true;
                    }
                }
            }
        } else if (sizeList.size() > 0) {
            Camera.Size cur = sizeList.get(0);
            LogUtils.d("fg size() = 1 cur.width " + cur.width + "cur.height " + cur.height);
            mPreviewWidth = cur.width;
            mPreviewHeight = cur.height;
            isSize = true;
        } else {
            isSize = false;
        }
        if (isSize) {
            return new Point(mPreviewWidth, mPreviewHeight);
        }
        return null;
    }

}
