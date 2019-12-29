package com.ds.utils

import android.content.Context
import android.content.SharedPreferences

object SharPUtils {
    var sharedPreferences: SharedPreferences? = null
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("ds", Context.MODE_PRIVATE)
    }

    fun saveBoolen(key: String, value: Boolean) {
        val editor = sharedPreferences?.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences?.getBoolean(key, defaultValue) ?: defaultValue
    }

    fun remove(key: String) {
        val editor = sharedPreferences?.edit()
        editor?.remove(key)
        editor?.apply()
    }

    fun saveData(key: String, value: String) {
        val editor = sharedPreferences?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun saveIntData(key: String, value: Int) {
        val editor = sharedPreferences?.edit()
        editor?.putInt(key, value)
        editor?.apply()
    }

    fun getData(key: String, defaultValue: String): String {
        return sharedPreferences?.getString(key, defaultValue) ?: defaultValue
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences?.getInt(key, defaultValue) ?: defaultValue
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences?.getFloat(key, defaultValue) ?: defaultValue
    }

    fun saveFloat(key: String, value: Float) {
        val editor = sharedPreferences?.edit()
        editor?.putFloat(key, value)
        editor?.apply()
    }
}