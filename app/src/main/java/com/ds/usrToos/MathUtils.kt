package com.ds.usrToos

import kotlin.math.abs

object MathUtils {
    var mMaxDis = 2.4f
    var stopCarTime = 30
    fun disMath(byte1: Byte, byte2: Byte): Int {
        return abs(byte1.toInt() * 256 + byte2.toInt())
    }

    fun disPower(byte1: Byte, byte2: Byte): Int {
        val result = byte1.toInt() * 16 * 16 + byte2.toInt()
        return ((result + 330.0) / 1024.0 * 3.0 / (51.0 / 261.0)).toInt()
    }

    fun isScope(dis: Double): Boolean {
        return dis <= mMaxDis
    }
}