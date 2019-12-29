package com.ds.usrToos

object MathUtils {
    var mMaxDis = 2.4f
    var stopCarTime = 30
    fun disMath(byte1: Byte, byte2: Byte): Double {
        return (byte1 * 256 + byte2) * 0.01
    }

    fun disPower(byte1: Byte): Int {
        return /*((byte1 + 330) / 1024 * 3 / (51 / 261)).toFloat()*/byte1.toInt()
    }

    fun isScope(dis: Double): Boolean {
        return dis <= mMaxDis
    }
}