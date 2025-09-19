package com.einkscreensaver.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationData(
    val id: Int,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val isOngoing: Boolean,
    val isClearable: Boolean,
    val priority: NotificationPriority,
    val category: String,
    val icon: String? = null,
    val largeIcon: String? = null,
    val sound: String? = null,
    val vibration: Boolean = false,
    val ledColor: Int? = null,
    val actions: List<NotificationAction> = emptyList()
) : Parcelable

@Parcelize
data class NotificationAction(
    val title: String,
    val action: String,
    val icon: String? = null
) : Parcelable

enum class NotificationPriority {
    MIN, LOW, DEFAULT, HIGH, MAX
}

enum class NotificationCategory {
    CALL, MESSAGE, EMAIL, ALARM, SOCIAL, NEWS, TRANSPORT, SYSTEM, OTHER
}

fun String.toNotificationCategory(): NotificationCategory {
    return when (this.lowercase()) {
        "call" -> NotificationCategory.CALL
        "msg", "message", "sms" -> NotificationCategory.MESSAGE
        "email" -> NotificationCategory.EMAIL
        "alarm" -> NotificationCategory.ALARM
        "social" -> NotificationCategory.SOCIAL
        "news" -> NotificationCategory.NEWS
        "transport", "navigation" -> NotificationCategory.TRANSPORT
        "system", "device" -> NotificationCategory.SYSTEM
        else -> NotificationCategory.OTHER
    }
}

fun Int.toNotificationPriority(): NotificationPriority {
    return when (this) {
        -2 -> NotificationPriority.MIN
        -1 -> NotificationPriority.LOW
        0 -> NotificationPriority.DEFAULT
        1 -> NotificationPriority.HIGH
        2 -> NotificationPriority.MAX
        else -> NotificationPriority.DEFAULT
    }
}