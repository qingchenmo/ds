package com.ds.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


object AppUtils {
    fun getVersionCode(context: Context) =
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode

    fun getVersionName(context: Context) =
            context.packageManager.getPackageInfo(context.packageName, 0).versionName

    fun hideSoft(context: Context?, v: EditText) {
        if (context != null) {
            val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    fun AppInstall(apkPath: String): Boolean {
        var dataOutputStream: DataOutputStream? = null
        var errorStream: BufferedReader? = null
        try {
            val process = Runtime.getRuntime().exec("su")
            dataOutputStream = DataOutputStream(process.outputStream)
            val command = "pm install -r $apkPath\n"
            dataOutputStream.write(command.toByteArray(Charset.forName("utf-8")))
            dataOutputStream.flush()
            dataOutputStream.writeBytes("exit\n")
            dataOutputStream.flush()
            process.waitFor()
            errorStream = BufferedReader(InputStreamReader(process.errorStream))
            var msg = ""
            var line: String = ""
            line = errorStream.readLine()
            while (line == null) {
                msg += line;
                line = errorStream.readLine()
            }
            if (!msg.contains("Failure")) {
                return true
            }
        } catch (e: Throwable) {

        } finally {
            try {
                dataOutputStream?.close()
                errorStream?.close()
            } catch (e: Throwable) {

            }
        }
        return false
    }
}