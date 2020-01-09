package com.ds.utils

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response


object HttpsUtils {
    private const val TAG = "HttpsUtils"
    private const val BASE_URL = "http://jztapi.zcym1688.com/app"

    /**
     * 根据地锁设备ID和车牌号校验是否开锁
     */
    fun parking(chepai: String, callBack: HttpUtilCallBack<ParkBean>) {
        Log.e(TAG, "$BASE_URL/licence/parking")
        OkGo.get<String>("$BASE_URL/licence/parking")
                .params("dev_sn", android.os.Build.SERIAL)
                .params("plate_number", chepai)
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
    fun update(lock_status: Int = 0, electric_quantity: Int = 0, electric_capacity: Int = 0) {
        Log.e(TAG, "$BASE_URL/oplock/update")
        OkGo.post<String>("$BASE_URL/oplock/update")
                .params("devSn", android.os.Build.SERIAL)
                .params("lock_status", lock_status)
                .params("electric_quantity", electric_quantity)
                .params("electric_capacity", electric_capacity)
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

    /**
     * 开锁成功后的回调,在地锁端成功开锁后,回调此接口进行订单生成
     * @param plate_number 车牌号,在开锁校验接口中已回传
     * @param dev_type 地锁类型,在开锁校验接口中已回传
     * @param unlocked 是否开锁成功,true/false,开锁失败请重新识别
     */
    fun parkingCallBack(plate_number: String, dev_type: String, unlocked: Boolean) {
        Log.e(TAG, "$BASE_URL/licence/parking/callback")
        OkGo.post<String>("$BASE_URL/licence/parking/callback")
                .params("dev_sn", android.os.Build.SERIAL)
                .params("plate_number", plate_number)
                .params("dev_type", dev_type)
                .params("unlocked", unlocked)
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

    /**
     * 地锁每3秒一轮询此接口判断是否需要开锁
     */
    fun checkIfCanUnLock(callBack: HttpUtilCallBack<Int>) {
        Log.e(TAG, "$BASE_URL/oplock/checkIfCanUnLock")
        OkGo.get<String>("$BASE_URL/oplock/checkIfCanUnLock")
                .params("devSn", android.os.Build.SERIAL)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        try {
                            Log.e(TAG, response.body())
                            var s = JSON.parseObject(response.body(), object : TypeReference<HttpBean<Int>>() {})
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

    interface HttpUtilCallBack<T> {
        fun onSuccess(t: T?)
        fun onFaile(errorCode: Int, errorMsg: String)
    }
}