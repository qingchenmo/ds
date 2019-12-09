package com.ds.utils

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.ds.GlobalContext
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response


class HttpsUtils {
    companion object {
        private const val TAG = "HttpsUtils"
        private const val BASE_URL = "http://jztapi.zcym1688.com/app"

        fun parking(chepai: String, callBack: HttpUtilCallBack<ParkBean>) {
            Log.e(TAG, "$BASE_URL/licence/parking")
            OkGo.get<String>("$BASE_URL/licence/parking")
                    .params("dev_sn", android.os.Build.SERIAL/*"54203112230"*/)
                    .params("plate_number", chepai/*"湘A888888"*/)
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
                            Log.e(TAG, response?.message())
                            callBack.onFaile(0, "")
                        }
                    })
        }

        fun <T> update(lock_status: Int = 0, electric_quantity: Int = 0, network: Int = 0) {
            Log.e(TAG, "$BASE_URL/oplock/update")
            OkGo.post<String>("$BASE_URL/oplock/update")
                    .params("devSn", android.os.Build.SERIAL)
                    .params("lock_status", lock_status)
                    .params("electric_quantity", electric_quantity)
                    .execute(object : StringCallback() {
                        override fun onSuccess(response: Response<String>) {
                            Log.e(TAG, response.body())
                        }

                        override fun onError(response: Response<String>?) {
                            super.onError(response)
                            Log.e(TAG, response?.message())
                        }
                    })
        }

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
                            GlobalContext.getInstance().notifyDataChanged(Constant.LOG, "回调成功")
                        }

                        override fun onError(response: Response<String>?) {
                            super.onError(response)
                            Log.e(TAG, response?.message())
                        }
                    })
        }

        fun checkIfCanUnLock(callBack: HttpUtilCallBack<String>) {
            Log.e(TAG, "$BASE_URL/oplock/checkIfCanUnLock")
            OkGo.get<String>("$BASE_URL/oplock/checkIfCanUnLock")
                    .params("devSn", android.os.Build.SERIAL)
                    .execute(object : StringCallback() {
                        override fun onSuccess(response: Response<String>) {
                            try {
                                Log.e(TAG, response.body())
                                var s = JSON.parseObject(response.body(), object : TypeReference<HttpBean<String>>() {})
                                if (s.code == 200)
                                    callBack.onSuccess(s.data)
                                else callBack.onFaile(s.code, s.msg)
                            } catch (e: Exception) {
                                callBack.onFaile(-1, e.message!!)
                            }
                        }

                        override fun onError(response: Response<String>?) {
                            super.onError(response)
                            Log.e(TAG, response?.message())
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
                            Log.e(TAG, response?.message())
                        }
                    })
        }
    }

    interface HttpUtilCallBack<T> {
        fun onSuccess(t: T?)
        fun onFaile(errorCode: Int, errorMsg: String)
    }
}