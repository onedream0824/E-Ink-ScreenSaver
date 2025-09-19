package com.einkscreensaver.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.einkscreensaver.app.data.model.BatteryInfo
import com.einkscreensaver.app.data.model.ChargingType
import com.einkscreensaver.app.data.model.BatteryHealth
import com.einkscreensaver.app.data.model.BatteryStatus

class BatteryReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val batteryInfo = parseBatteryInfo(intent)
                broadcastBatteryUpdate(context, batteryInfo)
            }
        }
    }
    
    private fun parseBatteryInfo(intent: Intent): BatteryInfo {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = (level * 100 / scale.toFloat()).toInt()
        
        val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
        val chargingType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0
        val present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        
        val powerSaveMode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.isPowerSaveMode
        } else {
            false
        }
        
        return BatteryInfo(
            level = batteryPct,
            isCharging = isCharging,
            chargingType = chargingType.toChargingType(),
            temperature = temperature,
            voltage = voltage,
            health = health.toBatteryHealth(),
            technology = technology,
            plugged = plugged,
            present = present,
            scale = scale,
            status = status.toBatteryStatus(),
            powerSaveMode = powerSaveMode
        )
    }
    
    private fun broadcastBatteryUpdate(context: Context, batteryInfo: BatteryInfo) {
        val intent = Intent("com.einkscreensaver.app.BATTERY_UPDATE")
        intent.putExtra("battery_info", batteryInfo)
        context.sendBroadcast(intent)
    }
}