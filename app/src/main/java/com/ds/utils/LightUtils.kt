package com.ds.utils

import java.io.File
import java.io.FileWriter

object LightUtils {
    fun PowerControl(num: Int = 1, handle: Int): Boolean {
        val brf: Any? = null
        var w_file: File? = null
        var cmdStirng: String? = null

        return try {
            w_file = File("/sys/class/misc/mtgpio/pin")
            if (num == 1) {
                if (handle == 1) {
                    cmdStirng = "-wdout 15 1"
                } else {
                    cmdStirng = "-wdout 15 0"
                }
            } else if (num == 2) {
                if (handle == 1) {
                    cmdStirng = "-wdout 8 1"
                } else {
                    cmdStirng = "-wdout 8 0"
                }
            } else if (num == 3) {
                if (handle == 1) {
                    cmdStirng = "-wdout 21 1"
                } else {
                    cmdStirng = "-wdout 21 0"
                }
            } else if (num == 4) {
                if(handle == 1){
                    cmdStirng = "-wdir 87 1"
                }else {
                    cmdStirng = "-wdir 87 0"
                }
            }else if (num==5){
                if(handle == 1){
                    cmdStirng = "-wdout 87 1"
                }else {
                    cmdStirng = "-wdout 87 0"
                }
            }

            val e = FileWriter(w_file)
            e.write(cmdStirng)
            e.flush()
            e.close()
            true
        } catch (var6: Exception) {
            var6.printStackTrace()
            false
        }

    }


}