package com.ds.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import com.ds.GlobalContext
import com.ds.R
import com.ds.utils.Constant
import com.ds.utils.PermissionUitl
import com.ds.utils.Setting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity1 : AppCompatActivity(), TabLayout.OnTabSelectedListener {
    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab != null) showFragment(tab?.position)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private var mTl: TabLayout? = null

    var mAllowListBeans = arrayListOf<AllowListBean>()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && permissions != null && permissions.size > 1) {
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
        Log.i(TAG, "onCreate")
        GlobalContext.getInstance().context = this
        setContentView(R.layout.activity_main)
        try {
            val type = object : TypeToken<ArrayList<AllowListBean>>() {
            }.type
            mAllowListBeans = Gson().fromJson(Setting.instance().getData(Constant.KEY_CHEPAI_LIST, ""), type)
            if (mAllowListBeans == null) mAllowListBeans = arrayListOf()
        } catch (e: Exception) {
            if (mAllowListBeans == null) mAllowListBeans = arrayListOf()
        }
        mTl = findViewById(R.id.tl)
        mTl?.addOnTabSelectedListener(this)
        showFragment(0)
    }

    var mControlFragment: ControlFragment1? = null
    var mAllowListFragment: AllowListFragment? = null

    private fun showFragment(position: Int) {
        val fragmentManager = fragmentManager
        val fragmentTransaction = supportFragmentManager
                .beginTransaction()
        if (position == 0) {
            if (null == mControlFragment) {
                mControlFragment = ControlFragment1()
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

    fun confirAllowInfo(bean: AllowListBean) {
        var view = LayoutInflater.from(this).inflate(R.layout.dia_add_layout, null)
        var name = view.findViewById<TextView>(R.id.name_et)
        var chepaiView = view.findViewById<TextView>(R.id.chepai_et)
        if (!TextUtils.isEmpty(bean.chepai)) chepaiView.text = bean.chepai
        if (!TextUtils.isEmpty(bean.name)) name.text = bean.name
        var builder = AlertDialog.Builder(this)
                .setTitle("修改白名单")
                .setView(view)
                .setPositiveButton("确定") { _, _ ->
                    if (TextUtils.isEmpty(name.text)) {
                        Toast.makeText(this@MainActivity1, "请输入名称", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (TextUtils.isEmpty(chepaiView.text)) {
                        Toast.makeText(this@MainActivity1, "请输入车牌", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    bean.name = name.text.toString()
                    bean.chepai = chepaiView.text.toString()
                    bean.count = 0
                    bean.isAllow = true
                    mAllowListFragment?.refreList()
                    Setting.instance().saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
                }
                .setNegativeButton("取消") { dialogInterface, i ->
                    dialogInterface.dismiss()
                    mControlFragment?.mCanSHibie = true
                }
        builder.create().show()
    }


    fun addAllowInfo(chepai: String?) {
        /*var view = LayoutInflater.from(this).inflate(R.layout.dia_add_layout, null)
        var name = view.findViewById<TextView>(R.id.name_et)
        var chepaiView = view.findViewById<TextView>(R.id.chepai_et)
        if (!TextUtils.isEmpty(chepai)) chepaiView.text = chepai
        var builder = AlertDialog.Builder(this)
                .setTitle("添加白名单")
                .setView(view)
                .setPositiveButton("确定") { _, _ ->
                    if (TextUtils.isEmpty(name.text)) {
                        Toast.makeText(this@MainActivity1, "请输入名称", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (TextUtils.isEmpty(chepaiView.text)) {
                        Toast.makeText(this@MainActivity1, "请输入车牌", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    var bean = AllowListBean()
                    bean.name = name.text.toString()
                    bean.chepai = chepaiView.text.toString()
                    bean.count = 0
                    bean.isAllow = true
                    mAllowListBeans.add(bean)
                    mAllowListFragment?.refreList()
                    mControlFragment?.mCanSHibie = true
                    Setting.instance().saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))

                }
                .setNegativeButton("取消") { dialogInterface, i ->
                    dialogInterface.dismiss()
                    mControlFragment?.mCanSHibie = true
                }
        builder.create().show()*/
        startActivityForResult(Intent(this, AddAllowActivity::class.java), 10)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK && data != null) {
            var bean = AllowListBean()
            bean.name = data.getStringExtra("name")
            bean.chepai = data.getStringExtra("chepai")
            bean.count = 0
            bean.isAllow = true
            mAllowListBeans.add(bean)
            mAllowListFragment?.refreList()
            mControlFragment?.mCanSHibie = true
            Setting.instance().saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
        }
    }

    fun removeAllowInfo(bean: AllowListBean) {
        var builder = AlertDialog.Builder(this)
                .setTitle("删除白名单")
                .setMessage("是否确定删除")
                .setPositiveButton("确定") { _, _ ->
                    if (mAllowListBeans.contains(bean)) {
                        mAllowListBeans.remove(bean)
                    }
                    Toast.makeText(this@MainActivity1, "删除成功", Toast.LENGTH_SHORT).show()
                    mAllowListFragment?.refreList()
                    Setting.instance().saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
                }
                .setNegativeButton("取消") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
        builder.create().show()
    }

    fun refreAllowListView() {
        mAllowListFragment?.refreList()
        Setting.instance().saveData(Constant.KEY_CHEPAI_LIST, Gson().toJson(mAllowListBeans))
    }

}
