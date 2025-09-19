package com.einkscreensaver.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Parcelize
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean,
    val location: String,
    val calendarName: String,
    val color: Int,
    val reminderMinutes: Int = 0,
    val isRecurring: Boolean = false,
    val recurrenceRule: String = ""
) : Parcelable {
    
    fun isToday(): Boolean {
        val today = Calendar.getInstance()
        val eventDate = Calendar.getInstance().apply { timeInMillis = startTime }
        return today.get(Calendar.YEAR) == eventDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == eventDate.get(Calendar.DAY_OF_YEAR)
    }
    
    fun isTomorrow(): Boolean {
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val eventDate = Calendar.getInstance().apply { timeInMillis = startTime }
        return tomorrow.get(Calendar.YEAR) == eventDate.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == eventDate.get(Calendar.DAY_OF_YEAR)
    }
    
    fun isThisWeek(): Boolean {
        val now = Calendar.getInstance()
        val eventDate = Calendar.getInstance().apply { timeInMillis = startTime }
        val daysDiff = (eventDate.timeInMillis - now.timeInMillis) / (1000 * 60 * 60 * 24)
        return daysDiff in 0..7
    }
    
    fun getTimeUntilEvent(): String {
        val now = System.currentTimeMillis()
        val diff = startTime - now
        
        return when {
            diff < 0 -> "Past"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
            else -> "${diff / (24 * 60 * 60 * 1000)}d"
        }
    }
    
    fun getFormattedTime(): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = startTime }
        return if (allDay) {
            "All Day"
        } else {
            String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        }
    }
    
    fun getFormattedDate(): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = startTime }
        return String.format("%02d/%02d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    }
}

enum class EventPriority {
    LOW, NORMAL, HIGH, URGENT
}

enum class EventType {
    MEETING, APPOINTMENT, BIRTHDAY, HOLIDAY, REMINDER, TASK, OTHER
}