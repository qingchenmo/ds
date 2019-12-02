package com.ds.view

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.aiwinn.carbranddect.App
import com.aiwinn.carbranddect.able.DistinguishListener
import com.ds.GlobalContext
import com.ds.R
import com.ds.common.IDataObserver
import com.ds.utils.Constant
import com.ds.utils.HttpsUtils
import com.ds.utils.ParkBean
import com.ds.utils.Setting
import com.serenegiant.usb.widget.UVCCameraTextureView


class ControlFragment1 : Fragment(), IDataObserver, DistinguishListener, View.OnClickListener {

    private val TAG = "ControlFragment"
    private val WIDTH = "WIDTH"
    private val HEIGHT = "HEIGHT"
    private var hander: Handler? = null
    private var mWbTime: TextView? = null
    private var mUVCCameraView: UVCCameraTextureView? = null
    private var mDsStatusView: TextView? = null
    private var mHasThingView: TextView? = null
    private var mThingDisanceView: TextView? = null
    private var mCameraStatusView: TextView? = null
    private var mShibieCount: TextView? = null

    private var mActivity: MainActivity1? = null


    private var mini_status = -1//地锁栏杆状态

    private var mShibieNum = 0

    var mCanSHibie = true

    private var mOpenCloseCamera = false

    private var mHasUpdata = false

    private var mFallChepai: String? = null
    private var mDevType: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.control_fragment, container, false)
        mWbTime = view?.findViewById(R.id.wb_time)
        mActivity = activity as MainActivity1
        view?.findViewById<View>(R.id.wb_open)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.ds_up)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.ds_down)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.camera_open)?.setOnClickListener(this)
        view?.findViewById<View>(R.id.camera_close)?.setOnClickListener(this)

        mDsStatusView = view?.findViewById(R.id.ds_status)
        mHasThingView = view?.findViewById(R.id.has_thing)
        mThingDisanceView = view?.findViewById(R.id.thing_disance)
        mCameraStatusView = view?.findViewById(R.id.camera_status)

        mShibieCount = view?.findViewById(R.id.shibie_count)
        mShibieCount?.text = "${Setting.instance().getInt(Constant.KEY_SHIBIE_COUNT, 0)}次"

        val dis_spinner = view?.findViewById<Spinner>(R.id.distance_spinner)
        dis_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val dis_string = adapterView.getItemAtPosition(i).toString()
                Setting.instance().saveFloat(Constant.KEY_MINI_DOWN_DIS, dis_string.toFloat())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        val stop_car_time_spanner = view?.findViewById<Spinner>(R.id.stop_car_time_spanner)
        stop_car_time_spanner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val dis_string = adapterView.getItemAtPosition(i).toString()
                Setting.instance().saveIntData(Constant.KEY_WAIT_STOP_CAR_TIME, dis_string.toInt())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        setSpinnerItemSelectedByValue(dis_spinner!!, Setting.instance().getFloat(Constant.KEY_MINI_DOWN_DIS, 2.5f).toString())
        setSpinnerStopCarTimeSelectedByValue(stop_car_time_spanner!!, Setting.instance().getInt(Constant.KEY_WAIT_STOP_CAR_TIME, 30).toString())


        mUVCCameraView = view.findViewById(R.id.uvc_car_camera)

        UVCCameraInterface.getInstance().setOnDistinguishListener(this)
        val width = App.sp.getInt(WIDTH, 1280)
        val height = App.sp.getInt(HEIGHT, 720)
        UVCCameraInterface.getInstance().setPreViewSize(width, height)
        UVCCameraInterface.getInstance().init(activity, mUVCCameraView)
        initHandler()

        if (mActivity != null && mActivity?.mAllowListBeans != null) {
            for (i in 0 until mActivity!!.mAllowListBeans.size) {
                mShibieNum += mActivity!!.mAllowListBeans[i].count
            }
        }
        mShibieCount?.text = "${mShibieNum}次"
        return view
    }

    private fun refreShibieCount() {
        mShibieNum++
        mShibieCount?.text = "${mShibieNum}次"
        mActivity?.refreAllowListView()
    }

    private fun setSpinnerItemSelectedByValue(spinner: Spinner, value: String) {
        val apsAdapter = spinner.adapter //得到SpinnerAdapter对象
        val k = apsAdapter.count
        for (i in 0 until k) {
            if (value == apsAdapter.getItem(i).toString()) {
                spinner.setSelection(i)// 默认选中项
                break
            }
        }
    }

    private fun setSpinnerStopCarTimeSelectedByValue(spinner: Spinner, value: String) {
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
            R.id.wb_open -> wakeUse()
            R.id.ds_up -> dsUp()
            R.id.ds_down -> dsFall("", "")
            R.id.camera_open -> {
                mOpenCloseCamera = true
                openCamera()

//                GlobalContext.getInstance().notifyDataChanged(Constant.KEY_LICENSE_PLATE_CHECKED, "")
            }
            R.id.camera_close -> {
                mOpenCloseCamera = false
                closeCamera()
            }
        }
    }

    private var count_open_time = 0
    private fun initHandler() {
        hander = Handler(Handler.Callback { msg ->
            when (msg?.what) {
                10 -> downStopCarTime()
                11 -> if (UVCCameraInterface.getInstance().isCameraOpened) {
                    count_open_time = 0
                    isOpenling = false
                } else {
                    if (count_open_time > 3) {
                        count_open_time = 0
                        isOpenling = false
                    } else {
                        count_open_time++
                        hander?.sendEmptyMessageDelayed(11, 1000)
                    }
                }

                12 -> {
                    Log.e(TAG, "rise start")
                    GlobalContext.getInstance().deviceManager.usr.rise()
                    Log.e(TAG, "rise end")
                }
                13 -> {
                    Log.e(TAG, "fall start")
                    GlobalContext.getInstance().deviceManager.usr.fall(mFallChepai,mDevType)
                    Log.e(TAG, "fall end")
                }
                14 -> {
                    Log.e(TAG, "wake_USR start")
                    GlobalContext.getInstance().deviceManager.usr.wake_USR()
                    Log.e(TAG, "wake_USR end")
                }
                15 -> {
                    Log.e(TAG, "openCamera start")
                    openCamera()
                    Log.e(TAG, "openCamera end")
                }
                16 -> {
                    Log.e(TAG, "closeCamera start")
                    closeCamera()
                    Log.e(TAG, "closeCamera end")
                }
            }
            true
        })
    }

    private var timeout = 0

    private fun dsUp() {
        Log.e(TAG, "dsUp")
        hander?.removeMessages(13)
        hander?.removeMessages(12)
        hander?.sendEmptyMessage(12)
    }

    private fun dsFall(plate_number: String?, dev_type: String?) {
        mFallChepai = plate_number
        mDevType = dev_type
        Log.e(TAG, "dsFall")
        hander?.removeMessages(12)
        hander?.removeMessages(13)
        hander?.sendEmptyMessage(13)
    }

    private fun wakeUse() {
        Log.e(TAG, "wakeUse")
        hander?.removeMessages(14)
        hander?.sendEmptyMessage(14)
    }

    private fun downStopCarTime() {
        if (timeout == 0) timeout = Setting.instance().getInt(Constant.KEY_WAIT_STOP_CAR_TIME, 30)
        else timeout--
        mWbTime?.text = "识别成功 $timeout 秒"
        if (mWbTime?.visibility != View.VISIBLE) mWbTime?.visibility = View.VISIBLE
        if (timeout > 0) {
            hander?.removeMessages(10)
            hander?.sendEmptyMessageDelayed(10, 1000)
        } else {
            mWbTime?.visibility = View.INVISIBLE
        }
    }


    @Synchronized
    private fun openCamera() {
        if (UVCCameraInterface.getInstance().isCameraOpened) {
            Log.w(TAG, "opened")
            mUVCCameraView?.visibility = View.VISIBLE
            mCameraStatusView?.text = "打开"
        } else {
            Log.w(TAG, "registerUSB")
            if (!isOpenling && UVCCameraInterface.getInstance().openTime >= 2) {
                UVCCameraInterface.getInstance().resetTime()
                UVCCameraInterface.getInstance().startTime()
                isOpenling = true
                Toast.makeText(activity, "打开摄像头", Toast.LENGTH_SHORT).show()
                UVCCameraInterface.getInstance().open()
                hander?.sendEmptyMessageDelayed(11, 1500)
                mUVCCameraView?.visibility = View.VISIBLE
                mCameraStatusView?.text = "打开"
            }
        }
    }

    private fun closeCamera(isPause: Boolean = false) {
        if (mOpenCloseCamera) return
        if (UVCCameraInterface.getInstance().openTime >= 2 || isPause) {
            if (UVCCameraInterface.getInstance().isCameraOpened) {
                UVCCameraInterface.getInstance().resetTime()
                UVCCameraInterface.getInstance().startTime()
                Log.i(TAG, "****unregisterUSB")
                Toast.makeText(activity, "关闭摄像头", Toast.LENGTH_SHORT).show()
                UVCCameraInterface.getInstance().close()
                mCameraStatusView?.text = "关闭"
                mUVCCameraView?.visibility = View.GONE
                isOpenling = false
            }
        }
    }


    override fun update(key: Int, o: Any) {
        Log.e(TAG, " key==$key   o==$o")
        when (key) {
            Constant.KEY_LICENSE_PLATE_CHECK_NOT -> return
            Constant.KEY_LICENSE_PLATE_CHECKED -> {
                if (mActivity == null || mActivity?.mAllowListBeans == null || !mCanSHibie) return
                closeCamera()
                val license = o as String
                Log.e(TAG, " license==$license")

                HttpsUtils.parking(license, object : HttpsUtils.HttpUtilCallBack<ParkBean> {
                    override fun onFaile(errorCode: Int, errorMsg: String) {
                        for (i in 0 until mActivity!!.mAllowListBeans.size) {
                            if (TextUtils.equals(license, mActivity!!.mAllowListBeans[i].chepai)) {
                                if (mini_status == 1 && mActivity!!.mAllowListBeans[i].isAllow) {
                                    var count = Setting.instance().getInt(Constant.KEY_SHIBIE_COUNT, 0)
                                    count++
                                    mShibieCount?.text = "$count 次"
                                    Setting.instance().saveIntData(Constant.KEY_SHIBIE_COUNT, count)
                                    downStopCarTime()
                                    dsFall("", "")
                                    mActivity!!.mAllowListBeans[i].count = mActivity!!.mAllowListBeans[i].count + 1
                                    refreShibieCount()
                                    Toast.makeText(activity, "识别车牌 $license 成功", Toast.LENGTH_SHORT).show()
                                }
                                return
                            }
                        }
                        Toast.makeText(activity, "识别车牌 $license 失败", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(t: ParkBean?) {
                        if (t == null) onFaile(-1, "服务器数据错误")
                        Toast.makeText(activity, "识别车牌 $license 成功", Toast.LENGTH_SHORT).show()
                        dsFall(t?.plate_number, t?.dev_type)
                        downStopCarTime()
                        var count = Setting.instance().getInt(Constant.KEY_SHIBIE_COUNT, 0)
                        count++
                        mShibieCount?.text = "$count 次"
                        Setting.instance().saveIntData(Constant.KEY_SHIBIE_COUNT, count)

                        for (i in 0 until mActivity!!.mAllowListBeans.size) {
                            if (TextUtils.equals(license, mActivity!!.mAllowListBeans[i].chepai)) {
                                if (mini_status == 1 && mActivity!!.mAllowListBeans[i].isAllow) {
                                    mActivity!!.mAllowListBeans[i].count = mActivity!!.mAllowListBeans[i].count + 1
                                    refreShibieCount()
                                }
                                return
                            }
                        }
                    }
                })


            }
            Constant.KEY_USR_DISTANCE -> {
                val dis = o.toString().split("m".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                mThingDisanceView?.text = "$dis 米"
                try {
                    Log.w(TAG, "USR_dis = $o,dis = $dis")
                    var remote_distance = dis.toDouble()
                    if (remote_distance < 0.5) {
                        waittimes = 0
                        mHasThingView?.text = "有"
                        closeCamera()
                    } else if (remote_distance > Setting.instance().getFloat(Constant.KEY_MINI_DOWN_DIS, 2.5f)) {
                        mHasThingView?.text = "无"
                        if (mini_status == 0 && timeout <= 0) {
                            waittimes++
                            if (waittimes >= 3) dsUp()
                        } else waittimes = 0
                        closeCamera()
                    } else if (remote_distance < Setting.instance().getFloat(Constant.KEY_MINI_DOWN_DIS, 2.5f) && mini_status == 1) {
                        waittimes = 0
                        openCamera()
                    }
                } catch (e: Exception) {
                    mHasThingView?.text = "无"
                }
            }
            Constant.KEY_MIMI_STATUS_CHANGE -> {
                mini_status = o as Int
                if (!mHasUpdata) {
                    mHasUpdata = true
                    HttpsUtils.update<String>(when (mini_status) {
                        1, 0x02 -> 3
                        0x11 -> 2
                        0x12 -> 1
                        else -> 4
                    }, null, null)
                }

                if (mini_status == 0) {
                    mDsStatusView?.text = "降落"
                    closeCamera()
                } else if (mini_status == 1) {
                    mDsStatusView?.text = "升起"
                }
            }
            Constant.KEY_QUERY_POWER_SUCCESS -> {
                Log.e(TAG, "查询到电量为 $o")
            }

            else -> {
                Toast.makeText(activity, "${o}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private var isOpenling = false

    private var waittimes = 0
    override fun onResume() {
        super.onResume()
        registObsever()
        GlobalContext.getInstance().deviceManager.open()
    }


    override fun onPause() {
        GlobalContext.getInstance().deviceManager.close()
        unRegistObserver()
        mOpenCloseCamera = false
        closeCamera(true)
        super.onPause()
    }

    override fun onDestroyView() {
        UVCCameraInterface.getInstance().release()
        super.onDestroyView()
    }

    private fun unRegistObserver() {
        GlobalContext.getInstance().unRegestObser(Constant.KEY_CMD_USR_ERR, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_USR_DISTANCE, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_USR_NO_CHECK_OBJECT, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_USR_OPEN_FAILED, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_USR_OPEN_SUCCESS, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_MIMI_STATUS_CHANGE, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_MIMI_START_RISE, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_MIMI_START_FALL, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_MIMI_END_RISE, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_MIMI_END_FALL, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_MINI_OPEN_FAILED, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_MINI_OPEN_SUCCESS, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_CMD_MIMI_ERR, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_LICENSE_PLATE_CHECKED, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_LICENSE_PLATE_CHECK_NOT, this)
        GlobalContext.getInstance().unRegestObser(Constant.KEY_QUERY_POWER_SUCCESS, this)
    }

    private fun registObsever() {
        GlobalContext.getInstance().registObserver(Constant.KEY_CMD_USR_ERR, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_USR_DISTANCE, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_USR_NO_CHECK_OBJECT, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_USR_OPEN_FAILED, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_USR_OPEN_SUCCESS, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_MIMI_STATUS_CHANGE, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_MIMI_START_RISE, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_MIMI_START_FALL, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_MIMI_END_RISE, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_MIMI_END_FALL, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_MINI_OPEN_FAILED, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_MINI_OPEN_SUCCESS, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_CMD_MIMI_ERR, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_LICENSE_PLATE_CHECKED, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_LICENSE_PLATE_CHECK_NOT, this)
        GlobalContext.getInstance().registObserver(Constant.KEY_QUERY_POWER_SUCCESS, this)
    }

    override fun distinguishMessage(s: String) {
        if (TextUtils.isEmpty(s)) {
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_LICENSE_PLATE_CHECK_NOT, s)
        } else {
            GlobalContext.getInstance().notifyDataChanged(Constant.KEY_LICENSE_PLATE_CHECKED, s)
        }
    }
}