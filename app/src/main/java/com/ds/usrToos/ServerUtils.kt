package com.ds.usrToos

import com.ds.utils.HttpsUtils
import com.ds.utils.ParkBean
import kotlinx.coroutines.*

class ServerUtils(val listener: CheckUnLockListener) {
    private val mainScope = MainScope()

    private var plateNumber = "" //开锁成功后服务器回传车牌号
    private var devType = "" //地锁类型,'operationalLock'和'dedicatedLock'两种,在开锁回调接口中需要这个
    private var mCheckJob: Job? = null


    fun parking(license: String) {
        HttpsUtils.parking(license, object : HttpsUtils.HttpUtilCallBack<ParkBean> {
            override fun onSuccess(t: ParkBean?) {
                if (t == null) {
                    onFaile(-1, "服务器数据错误")
                    return
                }
                listener.parkingResult(true, "校验 $license 成功")
                plateNumber = t.plate_number
                devType = t.dev_type
                MathUtils.stopCarTime = t.parkWaitTime
            }

            override fun onFaile(errorCode: Int, errorMsg: String) {
                listener.parkingResult(false, errorMsg)
            }
        })
    }

    fun level() {
        HttpsUtils.leave()
    }

    fun parkingCallBack() {
        if (plateNumber.isEmpty()) return
        HttpsUtils.parkingCallBack(plateNumber, devType, true)
        plateNumber = ""
        devType = ""
    }

    fun upStatus(lock_status: Int = 0, electric_quantity: Int = 0, electric_capacity: Int = 0) {
        HttpsUtils.update(lock_status, electric_quantity, electric_capacity)
    }

    fun startCheck() {
        mCheckJob?.cancel()
        mCheckJob = mainScope.launch {
            while (isActive) {
                checkCanUnLock()
                delay(3000)
            }
        }
    }

    private fun checkCanUnLock() {
        HttpsUtils.checkIfCanUnLock(object : HttpsUtils.HttpUtilCallBack<Int> {
            override fun onSuccess(t: Int?) {
                mCheckJob?.cancel()
                listener.needUnLock()
                MathUtils.stopCarTime = t ?: 30
            }

            override fun onFaile(errorCode: Int, errorMsg: String) {
            }
        })
    }


    interface CheckUnLockListener {
        fun needUnLock()
        fun parkingResult(isSuccess: Boolean, msg: String)
    }
}