package com.ds.view

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.aiwinn.carbranddect.able.DistinguishListener
import com.ds.R
import com.ds.usrToos.*
import com.ds.usrToos.Constant.DISTANCE_INFO
import com.ds.usrToos.Constant.LOCK_FALL_STATUS
import com.ds.usrToos.Constant.LOCK_FIRST_STATUS
import com.ds.usrToos.Constant.LOCK_MOVE_STATUS
import com.ds.usrToos.Constant.LOCK_RISE_STATUS
import com.ds.usrToos.Constant.POWER_STATUS
import com.ds.usrToos.Constant.SEND_CLOSE_LIGHT
import com.ds.usrToos.Constant.SEND_LOCK_FALL_FEED_BACK
import com.ds.usrToos.Constant.SEND_LOCK_FALL_SUCCESS
import com.ds.usrToos.Constant.SEND_LOCK_RISE_FEED_BACK
import com.ds.usrToos.Constant.SEND_LOCK_RISE_SUCCESS
import com.ds.usrToos.Constant.SEND_OPEN_LIGHT
import com.ds.utils.Constant
import com.ds.utils.SharPUtils
import com.serenegiant.usb.widget.UVCCameraTextureView
import kotlinx.coroutines.*


class ControlFragment : Fragment(), DistinguishListener, View.OnClickListener, ServerUtils.CheckUnLockListener {


    private val sTAG = "ControlFragment"
    private var mWbTime: TextView? = null
    private var mUVCCameraView: UVCCameraTextureView? = null
    private var mDsStatusView: TextView? = null
    private var mHasThingView: TextView? = null
    private var mThingDisanceView: TextView? = null
    private var mCameraStatusView: TextView? = null
    private var mShibieCount: TextView? = null
    private var mPower: TextView? = null
    private var mLog: TextView? = null
    private var mLightView: TextView? = null
    private var mDisSpinner: Spinner? = null
    private var mCameraFace: View? = null

    private var mActivity: MainActivity? = null

    private var mShibieNum = 0

    private val manager = UsrTool(this)
    private val lockManager = LockTool(this)
    private var cameraUtils: CameraUtils? = null
    private val serverUtils = ServerUtils(this)
    private var mStopTimeJob: Job? = null
    private var mOpenCameraJob: Job? = null
    private var mJiaoYanJob: Job? = null
    private var mCanRise = false
    private var mRiseDownJob: Job? = null
    private var mWatchDogTool = WatchDogTool()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.control_fragment, container, false)
        mWbTime = view?.findViewById(R.id.wb_time)
        mActivity = activity as MainActivity
        view?.findViewById<View>(R.id.wb_open)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.ds_up)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.ds_down)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.camera_open)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.camera_close)?.setOnClickListener(this)

        mDsStatusView = view?.findViewById(R.id.ds_status)
        mHasThingView = view?.findViewById(R.id.has_thing)
        mThingDisanceView = view?.findViewById(R.id.thing_disance)
        mCameraStatusView = view?.findViewById(R.id.camera_status)
//        mCameraFace = view?.findViewById(R.id.camera_face)

        mShibieCount = view?.findViewById(R.id.shibie_count)
        mPower = view?.findViewById(R.id.power)
        mLog = view?.findViewById(R.id.log)
        mLightView = view?.findViewById(R.id.light)
        mDisSpinner = view?.findViewById(R.id.distance_spinner)
        initSpanner()

        mUVCCameraView = view?.findViewById(R.id.uvc_car_camera)
        cameraUtils = CameraUtils(this, mUVCCameraView!!, this)

        initShiBieNum()
        mWatchDogTool.open()
        return view
    }

    private fun referShanieCount() {
        mShibieNum = SharPUtils.getInt(Constant.KEY_SHIBIE_COUNT, 0)
        mShibieNum++
        mShibieCount?.text = String.format("%s次", mShibieNum)
        mActivity?.referAllowListView()
        SharPUtils.saveIntData(Constant.KEY_SHIBIE_COUNT, mShibieNum)
    }

    private fun initShiBieNum() {
        mShibieNum = SharPUtils.getInt(Constant.KEY_SHIBIE_COUNT, 0)
        mShibieCount?.text = String.format("%s次", mShibieNum)
    }

    private fun initSpanner() {
        mDisSpinner?.onItemSelectedListener = DisSpinnerAdapter()
        setSpinnerItemSelectedByValue(mDisSpinner, SharPUtils.getFloat(Constant.KEY_MINI_DOWN_DIS, 3.6f).toString())
    }

    private fun setSpinnerItemSelectedByValue(spinner: Spinner?, value: String) {
        if (spinner == null) return
        val apsAdapter = spinner.adapter //得到SpinnerAdapter对象
        val k = apsAdapter.count
        for (i in 0 until k) {
            if (value == apsAdapter.getItem(i).toString()) {
                spinner.setSelection(i)// 默认选中项
                break
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.wb_open -> manager.jiaoZhun()
            R.id.ds_up -> rise()
            R.id.ds_down -> fall()
            R.id.camera_open -> openCamera()
            R.id.camera_close -> closeCamera()
        }
    }

    private fun rise() {
        if (timeout > 0) {
            return
        }
        lockManager.rise()
    }

    private fun fall() {
        if (timeout > 0) {
            return
        }
        serverUtils.stopCheck()
        lockManager.fall()
    }

    private fun openCamera() {
        /*mOpenCameraJob?.cancel()
        mOpenCameraJob = MainScope().launch {
            cameraUtils?.isPriview = true
            mCameraFace?.visibility = View.GONE
            mLog?.text = "打开摄像头"
            mCameraStatusView?.text = "打开"
            mCameraStatusView?.text = "打开"
            manager.openLight()
            delay(5000)
            if (cameraUtils?.isOpen() == true) closeCamera()
        }*/

        mOpenCameraJob?.cancel()
        mLog?.text = "打开摄像头"
        mOpenCameraJob = MainScope().launch {
            val result = cameraUtils?.start()
            if (result == true) {
                mCameraStatusView?.text = "打开"
                manager.openLight()
            } else {
                mCameraStatusView?.text = "关闭"
            }
            delay(5000)
            if (cameraUtils?.isOpen() == true) closeCamera()
        }
    }

    private fun closeCamera() {
//        mOpenCameraJob?.cancel()
//        cameraUtils?.isPriview = false
//        mCameraFace?.visibility = View.VISIBLE
        mOpenCameraJob?.cancel()
        cameraUtils?.stop()
        mCameraStatusView?.text = "关闭"
        manager.closeLight()
    }

    private var timeout = 0


    private fun downStopCarTime(isFall: Boolean) {
        MathUtils.stopCarTime = if (isFall) MathUtils.parkingWaitSeconds else MathUtils.outboundWaitSeconds
        mStopTimeJob?.cancel()
        mStopTimeJob = MainScope().launch {
            timeout = MathUtils.stopCarTime
            mWbTime?.visibility = View.VISIBLE
            while (isActive && timeout > 0) {
                mWbTime?.text = String.format("${if (isFall) "识别成功" else "出库等待"} %s秒", timeout)
                delay(1000)
                timeout--
            }
            timeout = 0
            mWbTime?.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        mUVCCameraView?.onPause()
        closeCamera()
        cameraUtils?.release()
    }

    override fun onResume() {
        super.onResume()
        mUVCCameraView?.onResume()
        manager.open()
        lockManager.open()
        cameraUtils?.init(activity)
    }

    override fun onDestroy() {
        mJiaoYanJob?.cancel()
        manager.release()
        lockManager.release()
        mWatchDogTool.release()
        super.onDestroy()
    }

    override fun needUnLock(data: Int) {
        if (data == 1) {
            //轮询到扫码开锁，打开地锁
            fall()
        } else if (data == 2) {
            rise()
        } else if (data == 3) {
            fall()
        } else if (data == 4) {
            try {
                val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
                val restartIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                val mgr = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, restartIntent)
                android.os.Process.killProcess(android.os.Process.myPid())
            } catch (e: Throwable) {

            }
        }
    }

    override fun parkingResult(isSuccess: Boolean, msg: String) {
        //校验车牌结果 提示
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        if (isSuccess) {
            fall()
            referShanieCount()
            closeCamera()
        }
    }

    override fun distinguishMessage(s: String) {
        try {
            if (s.isNotEmpty() && s.contains('_')) {
                val chepai = s.split("_")[0]
                val color = s.split('_')[1]
                MainScope().launch(Dispatchers.Main) {
                    Toast.makeText(activity, "检测到车牌 $chepai  颜色 $color", Toast.LENGTH_SHORT).show()
                    closeCamera()
                    if (lockManager.canFall()) serverUtils.parking(chepai, color)
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun jiaoYan() {
        mJiaoYanJob = MainScope().launch {
            if (!manager.jiaoYanSuccess && lockManager.canFall()) {
                manager.jiaoZhun()
            } else if (manager.jiaoYanSuccess) return@launch
            delay(2000)
            jiaoYan()
        }
    }

    @SuppressLint("SetTextI18n")
    suspend fun operateResult(type: Int, arg: Any) {
        Log.e("operateResult", " type == $type  arg== $arg")
        withContext(Dispatchers.Main) {
            mLog?.text = arg.toString()
            when (type) {
                SEND_LOCK_FALL_SUCCESS -> {
                    mDsStatusView?.text = arg.toString()
                    serverUtils.parkingCallBack()
                    downStopCarTime(true)
                    serverUtils.upStatus(1)
                }
                SEND_LOCK_RISE_SUCCESS -> {
                    jiaoYan()
                    mDsStatusView?.text = arg.toString()
                    serverUtils.level()
                    serverUtils.startCheck()
                    downStopCarTime(false)
                    serverUtils.upStatus(2)
                }
                SEND_LOCK_FALL_FEED_BACK,
                SEND_LOCK_RISE_FEED_BACK -> {
                    mDsStatusView?.text = arg.toString()
                    serverUtils.upStatus(3)
                }
                LOCK_FALL_STATUS -> {
                    mDsStatusView?.text = arg.toString()
                    serverUtils.upStatus(4)
                }
                LOCK_MOVE_STATUS -> {
                    mDsStatusView?.text = arg.toString()
                    serverUtils.upStatus(3)
                }
                LOCK_FIRST_STATUS -> {
                    mDsStatusView?.text = arg.toString()
                    serverUtils.startCheck()
                }
                LOCK_RISE_STATUS -> {
                    jiaoYan()
                    mDsStatusView?.text = arg.toString()
                    serverUtils.startCheck()
                }

                DISTANCE_INFO -> {
                    mThingDisanceView?.text = "${arg}米"
                    val dis = arg as Double
                    mHasThingView?.text = "无"
                    if (dis > 0 && dis <= MathUtils.triggerDistance && cameraUtils?.isOpen() == false && lockManager.canFall() && timeout <= 0) {
                        mRiseDownJob?.cancel()
                        mCanRise = false
                        openCamera()
                    } else if (dis in 0.05..0.9) {
                        mHasThingView?.text = "有"
                        mRiseDownJob?.cancel()
                        mCanRise = false
                    } else if ((dis < 0.05 || dis > 0.9) && timeout <= 0 && cameraUtils?.isOpen() == false && lockManager.canRise()) {
                        if (mCanRise) {
                            rise()
                            mCanRise = false
                        } else if (mRiseDownJob == null || mRiseDownJob?.isActive == false) {
                            mRiseDownJob = MainScope().async {
                                delay(MathUtils.outboundCheckSeconds.toLong() * 1000)
                                mCanRise = true
                            }
                        }
                    }
                }
                POWER_STATUS -> {
//                    mPower?.text = arg.toString()
                    serverUtils.upStatus(electric_quantity = lockManager.power1, electric_capacity = lockManager.power2)
                }
                SEND_OPEN_LIGHT -> {
                    mLightView?.text = "打开"
                }
                SEND_CLOSE_LIGHT -> {
                    mLightView?.text = "关闭"
                }
            }
        }
    }

    private suspend fun downTimeRise() {
        delay(3000)
        mCanRise = false

    }
}