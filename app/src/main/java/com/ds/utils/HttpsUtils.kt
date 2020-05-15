package com.ds.utils

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.ds.usrToos.MathUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import java.io.File


object HttpsUtils {
    private const val TAG = "HttpsUtils"
    const val DOWN_URL = "http://api.jzt.zcymkj.com"
//    const val DOWN_URL = "http://jztapi.zcym1688.com"
    const val BASE_URL = "$DOWN_URL/app"

    /**
     * 根据地锁设备ID和车牌号校验是否开锁
     */
    fun parking(chepai: String, color: String, callBack: HttpUtilCallBack<ParkBean>) {
        Log.e(TAG, "$BASE_URL/licence/parking")
        OkGo.get<String>("$BASE_URL/licence/parking")
                .params("dev_sn", android.os.Build.SERIAL)
                .params("plate_number", chepai)
                .params("color", color)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        try {
                            Log.e(TAG, response.body())
                            val s = JSON.parseObject(response.body(), object : TypeReference<HttpBean<ParkBean>>() {})
                            if (s.code == 200)
                                callBack.onSuccess(s.data)
                            else callBack.onFaile(s.code, s.msg)
                        } catch (e: Exception) {
                            callBack.onFaile(-1, e.message!!)
                        }
                    }

                    override fun onError(response: Response<String>?) {
                        super.onError(response)
                        Log.e(TAG, response?.message() ?: "")
                        callBack.onFaile(0, "")
                    }
                })
    }

    /**
     * 地锁端根据地锁序列号对地锁状态(剩余电量,地锁锁臂状态,地锁初始化网络)进行更新
     * @param lock_status 地锁锁臂状态,1:下降/已开锁,2:上升/已上锁,3:运动,4:初始
     * @param electric_quantity 地锁剩余电量,0-100整数
     */
    fun update(lock_status: Int = 0, electric_quantity: Int = 0, electric_capacity: Int = 0, amount: Int = -1) {
        Log.e(TAG, "$BASE_URL/oplock/update")
        val post = OkGo.post<String>("$BASE_URL/oplock/update")
                .params("devSn", android.os.Build.SERIAL)
        if (lock_status != 0) post.params("lock_status", lock_status)
        post.params("electric_quantity", electric_quantity)
        post.params("electric_capacity", electric_capacity)
        if (amount != -1) post.params("amount", amount)
        post.execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>) {
                Log.e(TAG, response.body())
                try {
                    Log.e(TAG, response.body())
                    val s = JSON.parseObject(response.body(), object : TypeReference<HttpBean<UpDataBean>>() {})
                    if (s.code == 200) {
                        MathUtils.triggerDistance = s.data.triggerDistance
                        MathUtils.outboundCheckSeconds = s.data.outboundCheckSeconds
                        MathUtils.outboundWaitSeconds = s.data.outboundWaitSeconds
                        MathUtils.parkingWaitSeconds = s.data.parkingWaitSeconds
                    }
                } catch (e: Exception) {
                }
            }

            override fun onError(response: Response<String>?) {
                super.onError(response)
                Log.e(TAG, response?.message() ?: "")
            }
        })
    }

    /**
     * 开锁成功后的回调,在地锁端成功开锁后,回调此接口进行订单生成
     * @param plate_number 车牌号,在开锁校验接口中已回传
     * @param dev_type 地锁类型,在开锁校验接口中已回传
     * @param unlocked 是否开锁成功,true/false,开锁失败请重新识别
     */
    fun parkingCallBack(plate_number: String, dev_type: String, unlocked: Boolean) {
        Log.e(TAG, "$BASE_URL/licence/parking/callback")
        OkGo.post<String>("$BASE_URL/licence/parking/callback")
                .params("devSn", android.os.Build.SERIAL)
                .params("plate_number", plate_number)
                .params("dev_type", dev_type)
                .params("unlocked", unlocked)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        val str = response.body()
                        Log.e(TAG, str)
                    }

                    override fun onError(response: Response<String>?) {
                        super.onError(response)
                        Log.e(TAG, response?.message() ?: "")
                    }
                })
    }

    /**
     * 地锁每3秒一轮询此接口判断是否需要开锁
     */
    fun checkIfCanUnLock(callBack: HttpUtilCallBack<HttpBean<Int>>) {
        Log.e(TAG, "$BASE_URL/oplock/checkIfCanUnLock")
        OkGo.get<String>("$BASE_URL/oplock/checkIfCanUnLock")
                .params("devSn", android.os.Build.SERIAL)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        try {
                            Log.e(TAG, response.body())
                            var s = JSON.parseObject(response.body(), object : TypeReference<HttpBean<Int>>() {})
                            if (s.code == 200)
                                callBack.onSuccess(s)
                            else callBack.onFaile(s.code, s.msg)
                        } catch (e: Exception) {
                            callBack.onFaile(-1, e.message!!)
                        }
                    }

                    override fun onError(response: Response<String>?) {
                        super.onError(response)
                        Log.e(TAG, response?.message() ?: "")
                        callBack.onFaile(0, "")
                    }
                })

    }

    fun leave() {
        Log.e(TAG, "$BASE_URL/licence/leave")
        OkGo.post<String>("$BASE_URL/licence/leave")
                .params("devSn", android.os.Build.SERIAL)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        Log.e(TAG, response.body())
                    }

                    override fun onError(response: Response<String>?) {
                        super.onError(response)
                        Log.e(TAG, response?.message() ?: "")
                    }
                })
    }


    fun imageRecFailure(file: File) {
        Log.e(TAG, "$BASE_URL/upload/imageRecFailure")
        OkGo.post<String>("$BASE_URL/upload/imageRecFailure")
                .params("devSn", android.os.Build.SERIAL)
                .params("file", file)
                .isMultipart(true)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        Log.e(TAG, response.body())
                    }

                    override fun onError(response: Response<String>?) {
                        super.onError(response)
                        Log.e(TAG, response?.message() ?: "")
                    }
                })
    }

    fun latestVersion(
            callBack: HttpUtilCallBack<AppLastVersionBean>
    ) {
        val res = OkGo.get<String>("$BASE_URL/apk/latestVersion")
        res.execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>) {
                try {
                    Log.e(TAG, response.body())
                    val s = JSON.parseObject(
                            response.body(),
                            AppLastVersionBean::class.java
                    )
                    callBack.onSuccess(s)
                } catch (e: Exception) {
                    callBack.onFaile(-1, e.message!!)
                }
            }

            override fun onError(response: Response<String>?) {
                super.onError(response)
                Log.e(TAG, response?.message() ?: "")
                callBack.onFaile(0, response?.message() ?: "")
            }
        })
    }

    fun appDownload(url: String, callBack: HttpUtilCallBack<File>) {
        val res = OkGo.get<File>(url)
        res.execute(object : FileCallback("地锁.apk") {
            override fun onSuccess(response: Response<File>?) {
                if (response?.body()?.isFile == true) callBack.onSuccess(response.body())
                else callBack.onFaile(response?.code() ?: 0, response?.message() ?: "")
            }

            override fun downloadProgress(progress: Progress?) {
                super.downloadProgress(progress)

            }

            override fun onError(response: Response<File>?) {
                super.onError(response)
                callBack.onFaile(response?.code() ?: -1, response?.message() ?: "")
            }
        })
    }


    interface HttpUtilCallBack<T> {
        fun onSuccess(t: T?)
        fun onFaile(errorCode: Int, errorMsg: String)
    }
}