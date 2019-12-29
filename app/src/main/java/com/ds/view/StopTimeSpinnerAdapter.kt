package com.ds.view

import android.view.View
import android.widget.AdapterView
import com.ds.usrToos.MathUtils
import com.ds.utils.Constant
import com.ds.utils.SharPUtils

class StopTimeSpinnerAdapter : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        val time = adapterView.getItemAtPosition(i).toString()
        MathUtils.stopCarTime = time.toInt()
        SharPUtils.saveIntData(Constant.KEY_WAIT_STOP_CAR_TIME, MathUtils.stopCarTime)
    }
}