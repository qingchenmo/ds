package com.ds.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import android.widget.Toast
import com.aiwinn.carbranddect.App

class PowerBroadcastReceiver : BroadcastReceiver() {
    private var Level: Int = 0
    override fun onReceive(p0: Context?, p1: Intent?) {
        val batteryLevel = p1?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 99
        val batteryScale = p1?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        Level = (batteryLevel * 100 / batteryScale)
        Log.e(PowerBroadcastReceiver::class.java.name, "Level==$Level")
        if (Level > 100) Level = 100 else if (Level < 0) Level = 0
        HttpsUtils.update(amount = Level)
    }
}