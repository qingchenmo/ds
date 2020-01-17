package com.ds.usrToos

import android.util.Log
import java.util.regex.Pattern


object MathUtils {
    var mMaxDis = 3.6f
    var stopCarTime = 30
    fun disMath(byte1: Byte, byte2: Byte): Int {
        var b = (byte2.toInt())
        if (b < 0) b = (b and 0xff)
        var a = (byte1.toInt())
        if (a < 0) a = (a and 0xff)
        Log.e("aaaa", "a==$a    b == $b")
        return a.toString(2).toInt(2).shl(8) + b
    }

    fun disPower(byte1: Byte, byte2: Byte): Int {
        val result = byte1.toInt() * 16 * 16 + byte2.toInt()
        return ((result + 330.0) / 1024.0 * 3.0 / (51.0 / 261.0)).toInt()
    }

    fun isScope(dis: Double): Boolean {
        return dis <= mMaxDis
    }

    fun isCarNo(carNo: String): Boolean {
        val p = Pattern.compile("^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(?:(?![A-Z]{4})[A-Z0-9]){4}[A-Z0-9挂学警港澳]{1}$")
        return p.matcher(carNo).matches()
    }
}