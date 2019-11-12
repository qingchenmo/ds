package com.ds.utils

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response

class HttpsUtils {
    companion object {
        private const val TAG = "HttpsUtils"
        private const val BASE_URL = "http://27n69300u1.qicp.vip:21842/app"

        fun <T> parking(chepai: String, callBack: HttpUtilCallBack<T>) {
            Log.e(TAG, "$BASE_URL/licence/parking")
            OkGo.get<String>("$BASE_URL/licence/parking")
                    .params("dev_sn", android.os.Build.SERIAL/*"54203112230"*/)
                    .params("plate_number", chepai/*"湘A888888"*/)
                    .execute(object : StringCallback() {
                        override fun onSuccess(response: Response<String>) {
                            Log.e(TAG, response.body())
                            var s = JSON.parseObject(response.body(), object : TypeReference<HttpBean<T>>(String::class.java) {})
                            if (s.code == 200)
                                callBack.onSuccess(s.data)
                            else callBack.onFaile(s.code, s.msg)
                        }

                        override fun onError(response: Response<String>?) {
                            super.onError(response)
                            Log.e(TAG, response?.message())
                            callBack.onFaile(0, "")
                        }
                    })
        }


    }

    interface HttpUtilCallBack<T> {
        fun onSuccess(t: T?)
        fun onFaile(errorCode: Int, errorMsg: String)
    }
}