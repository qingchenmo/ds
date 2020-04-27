package com.ds.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.aiwinn.carbranddect.App
import com.aiwinn.carbranddect.CarBrandManager
import com.ds.R
import com.ds.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {
    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab != null) showFragment(tab.position)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private var mTl: TabLayout? = null

    var mAllowListBeans = arrayListOf<AllowListBean>()
    private val mPowerBroadcastReceiver = PowerBroadcastReceiver()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && permissions.size > 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                CarBrandManager.init(applicationContext, 0, null)
                App.isInit = true
                Toast.makeText(this, "权限请求成功", Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(this, "权限请求失败", Toast.LENGTH_SHORT).show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!PermissionUitl.checkSelfPermission(this)) {
            PermissionUitl.requestPermission(this)
        }
        SharPUtils.init(this)
        Log.i(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        val beans = SharPUtils.getData(Constant.KEY_CHEPAI_LIST, "")
        if (beans.isNotEmpty())
            mAllowListBeans = Gson().fromJson(beans, object : TypeToken<ArrayList<AllowListBean>>() {}.type)
        mTl = findViewById(R.id.tl)
        mTl?.addOnTabSelectedListener(this)
        showFragment(0)
        //注册接收器以获取电量信息
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        var battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (battery > 100) battery = 100 else if (battery < 0) battery = 0
        HttpsUtils.update(amount = battery)
        registerReceiver(mPowerBroadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    override fun onDestroy() {
        unregisterReceiver(mPowerBroadcastReceiver)
        super.onDestroy()
    }

    private var mControlFragment: ControlFragment? = null
    private var mAllowListFragment: AllowListFragment? = null

    private fun showFragment(position: Int) {
        val fragmentManager = fragmentManager
        val fragmentTransaction = supportFragmentManager
                .beginTransaction()
        if (position == 0) {
            if (null == mControlFragment) {
                mControlFragment = ControlFragment()
                fragmentTransaction.add(R.id.fl, mControlFragment)
            } else {
                fragmentTransaction.show(mControlFragment)
            }
            if (mAllowListFragment != null) fragmentTransaction.hide(mAllowListFragment)
        } else {
            if (null == mAllowListFragment) {
                mAllowListFragment = AllowListFragment()
                fragmentTransaction.add(R.id.fl, mAllowListFragment)
            } else {
                fragmentTransaction.show(mAllowListFragment)
            }
            if (mControlFragment != null) fragmentTransaction.hide(mControlFragment)
        }
        fragmentTransaction.commitAllowingStateLoss()
        fragmentManager.executePendingTransactions()
    }

    @SuppressLint("InflateParams")
    fun conferAllowInfo(bean: AllowListBean) {
        val view = LayoutInflater.from(this).inflate(R.layout.dia_add_layout, null, false)
        val name = view.findViewById<TextView>(R.id.name_et)
        val chepaiView = view.findViewById<TextView>(R.id.chepai_et)
        if (!TextUtils.isEmpty(bean.chepai)) chepaiView.text = bean.chepai
        if (!TextUtils.isEmpty(bean.name)) name.text = bean.name
        val builder = AlertDialog.Builder(this)
                .setTitle("修改白名单")
                .setView(view)
                .setPositiveButton("确定") { _, _ ->
                    if (TextUtils.isEmpty(name.text)) {
                        Toast.makeText(this@MainActivity, "请输入名称", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (TextUtils.isEmpty(chepaiView.text)) {
                        Toast.makeText(this@MainActivity, "请输入车牌", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    bean.name = name.text.toString()
                    bean.chepai = chepaiView.text.toString()
                    bean.count = 0
                    bean.isAllow = true
                    mAllowListFragment?.refreList()
                    SharPUtils.saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
                }
                .setNegativeButton("取消") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
        builder.create().show()
    }


    fun addAllowInfo() {
        startActivityForResult(Intent(this, AddAllowActivity::class.java), 10)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK && data != null) {
            val bean = AllowListBean()
            bean.name = data.getStringExtra("name")
            bean.chepai = data.getStringExtra("chepai")
            bean.count = 0
            bean.isAllow = true
            mAllowListBeans.add(bean)
            mAllowListFragment?.refreList()
            SharPUtils.saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
        }
    }

    fun removeAllowInfo(bean: AllowListBean) {
        val builder = AlertDialog.Builder(this)
                .setTitle("删除白名单")
                .setMessage("是否确定删除")
                .setPositiveButton("确定") { _, _ ->
                    if (mAllowListBeans.contains(bean)) {
                        mAllowListBeans.remove(bean)
                    }
                    Toast.makeText(this@MainActivity, "删除成功", Toast.LENGTH_SHORT).show()
                    mAllowListFragment?.refreList()
                    SharPUtils.saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
                }
                .setNegativeButton("取消") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
        builder.create().show()
    }

    fun referAllowListView() {
        mAllowListFragment?.refreList()
        SharPUtils.saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
    }

}
