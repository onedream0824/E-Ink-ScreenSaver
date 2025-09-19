package com.einkscreensaver.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val chargingType: ChargingType,
    val temperature: Float,
    val voltage: Int,
    val health: BatteryHealth,
    val technology: String,
    val plugged: Boolean,
    val present: Boolean,
    val scale: Int,
    val status: BatteryStatus,
    val powerSaveMode: Boolean,
    val estimatedTimeRemaining: Long = -1,
    val estimatedTimeToFull: Long = -1
) : Parcelable {
    
    fun getBatteryColor(): Int {
        return when {
            level > 50 -> 0xFF4CAF50.toInt()
            level > 20 -> 0xFFFF9800.toInt()
            level > 10 -> 0xFFFF5722.toInt()
            else -> 0xFFD32F2F.toInt()
        }
    }
    
    fun getBatteryStatusText(): String {
        return when {
            isCharging -> "Charging"
            level > 20 -> "Good"
            level > 10 -> "Low"
            else -> "Critical"
        }
    }
    
    fun getFormattedTimeRemaining(): String {
        return when {
            isCharging && estimatedTimeToFull > 0 -> {
                val hours = estimatedTimeToFull / (1000 * 60 * 60)
                val minutes = (estimatedTimeToFull % (1000 * 60 * 60)) / (1000 * 60)
                "${hours}h ${minutes}m to full"
            }
            !isCharging && estimatedTimeRemaining > 0 -> {
                val hours = estimatedTimeRemaining / (1000 * 60 * 60)
                val minutes = (estimatedTimeRemaining % (1000 * 60 * 60)) / (1000 * 60)
                "${hours}h ${minutes}m remaining"
            }
            else -> ""
        }
    }
}

enum class ChargingType {
    NONE, AC, USB, WIRELESS, UNKNOWN
}

enum class BatteryHealth {
    UNKNOWN, GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, UNSPECIFIED_FAILURE, COLD
}

enum class BatteryStatus {
    UNKNOWN, CHARGING, DISCHARGING, NOT_CHARGING, FULL
}

fun Int.toChargingType(): ChargingType {
    return when (this) {
        1 -> ChargingType.AC
        2 -> ChargingType.USB
        4 -> ChargingType.WIRELESS
        else -> ChargingType.NONE
    }
}

fun Int.toBatteryHealth(): BatteryHealth {
    return when (this) {
        1 -> BatteryHealth.UNKNOWN
        2 -> BatteryHealth.GOOD
        3 -> BatteryHealth.OVERHEAT
        4 -> BatteryHealth.DEAD
        5 -> BatteryHealth.OVER_VOLTAGE
        6 -> BatteryHealth.UNSPECIFIED_FAILURE
        7 -> BatteryHealth.COLD
        else -> BatteryHealth.UNKNOWN
    }
}

fun Int.toBatteryStatus(): BatteryStatus {
    return when (this) {
        1 -> BatteryStatus.UNKNOWN
        2 -> BatteryStatus.CHARGING
        3 -> BatteryStatus.DISCHARGING
        4 -> BatteryStatus.NOT_CHARGING
        5 -> BatteryStatus.FULL
        else -> BatteryStatus.UNKNOWN
    }
}