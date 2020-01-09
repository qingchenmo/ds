package com.ds.view

import android.view.View
import android.widget.AdapterView
import com.ds.usrToos.MathUtils
import com.ds.utils.Constant
import com.ds.utils.SharPUtils

class DisSpinnerAdapter : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
        try {
            val disString = adapterView?.getItemAtPosition(i).toString()
            MathUtils.mMaxDis = disString.toFloat()
            SharPUtils.saveFloat(Constant.KEY_MINI_DOWN_DIS, MathUtils.mMaxDis)
        } catch (e: Exception) {

        }
    }
}