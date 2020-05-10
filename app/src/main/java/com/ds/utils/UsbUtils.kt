package com.ds.utils

import android.util.Log

object UsbUtils {
    fun allowUsb() {
        command("setprop persist.sys.usb.config mtp,adb")
    }

    fun disAllowUsb() {
        command("setprop persist.sys.usb.config none")
    }


    private fun command(com: String) {
        try {
            Runtime.getRuntime().exec(com)
        } catch (e: Exception) {
            Log.e(UsbUtils::class.java.name, "command exception == ${e.message}")
        }
    }
}