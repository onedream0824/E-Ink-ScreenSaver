package com.einkscreensaver.app.ui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.einkscreensaver.app.data.model.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ScreenSaverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var settings: ScreenSaverSettings? = null
    private var timeString: String = ""
    private var batteryInfo: BatteryInfo? = null
    private var weatherData: WeatherData? = null
    private var calendarEvents: List<CalendarEvent> = emptyList()
    private var notifications: List<NotificationData> = emptyList()
    private var customImagePath: String = ""
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var clockFont: Typeface? = null
    private var customImage: Bitmap? = null
    
    private val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        paint.apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        backgroundPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        
        outlinePaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        centerX = w / 2f
        centerY = h / 2f
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        settings?.let { settings ->
            drawBackground(canvas, settings)
            drawCustomImage(canvas, settings)
            drawTimeAndDate(canvas, settings)
            drawBatteryInfo(canvas, settings)
            drawWeatherInfo(canvas, settings)
            drawCalendarEvents(canvas, settings)
            drawNotifications(canvas, settings)
        }
    }
    
    private fun drawBackground(canvas: Canvas, settings: ScreenSaverSettings) {
        val backgroundColor = when {
            settings.blackBackground -> Color.BLACK
            settings.invertColors -> Color.BLACK
            else -> Color.WHITE
        }
        
        backgroundPaint.color = backgroundColor
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), backgroundPaint)
        
        paint.color = if (backgroundColor == Color.BLACK) Color.WHITE else Color.BLACK
        outlinePaint.color = if (backgroundColor == Color.BLACK) Color.BLACK else Color.WHITE
    }
    
    private fun drawCustomImage(canvas: Canvas, settings: ScreenSaverSettings) {
        if (customImage != null && !settings.imageFillsScreen) {
            val image = customImage!!
            val imageWidth = image.width
            val imageHeight = image.height
            
            val scale = minOf(
                viewWidth.toFloat() / imageWidth,
                viewHeight.toFloat() / imageHeight
            ) * 0.3f
            
            val scaledWidth = imageWidth * scale
            val scaledHeight = imageHeight * scale
            
            val left = centerX - scaledWidth / 2
            val top = centerY - scaledHeight / 2 - 200
            
            canvas.drawBitmap(
                image,
                Rect(0, 0, imageWidth, imageHeight),
                RectF(left, top, left + scaledWidth, top + scaledHeight),
                paint
            )
        } else if (customImage != null && settings.imageFillsScreen) {
            canvas.drawBitmap(
                customImage!!,
                null,
                RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat()),
                paint
            )
        }
    }
    
    private fun drawTimeAndDate(canvas: Canvas, settings: ScreenSaverSettings) {
        val textColor = if (settings.blackBackground || settings.invertColors) Color.WHITE else Color.BLACK
        
        paint.color = textColor
        paint.textSize = 72f
        paint.typeface = clockFont ?: Typeface.DEFAULT_BOLD
        
        if (settings.textOutline) {
            outlinePaint.color = if (textColor == Color.WHITE) Color.BLACK else Color.WHITE
        }
        
        val timeY = when (settings.screenLayout) {
            ScreenLayout.CENTERED -> centerY - 100
            ScreenLayout.LEFT_ALIGNED -> centerY - 100
            ScreenLayout.RIGHT_ALIGNED -> centerY - 100
            ScreenLayout.FULL_SCREEN -> centerY - 100
        }
        
        val dateY = timeY + 100
        
        paint.textAlign = when (settings.screenLayout) {
            ScreenLayout.CENTERED -> Paint.Align.CENTER
            ScreenLayout.LEFT_ALIGNED -> Paint.Align.LEFT
            ScreenLayout.RIGHT_ALIGNED -> Paint.Align.RIGHT
            ScreenLayout.FULL_SCREEN -> Paint.Align.CENTER
        }
        
        val textX = when (settings.screenLayout) {
            ScreenLayout.CENTERED -> centerX
            ScreenLayout.LEFT_ALIGNED -> 50f
            ScreenLayout.RIGHT_ALIGNED -> viewWidth - 50f
            ScreenLayout.FULL_SCREEN -> centerX
        }
        
        if (settings.textOutline) {
            canvas.drawText(timeString, textX, timeY, outlinePaint)
        }
        canvas.drawText(timeString, textX, timeY, paint)
        
        paint.textSize = 24f
        paint.typeface = Typeface.DEFAULT
        
        val currentDate = dateFormat.format(Date())
        if (settings.textOutline) {
            canvas.drawText(currentDate, textX, dateY, outlinePaint)
        }
        canvas.drawText(currentDate, textX, dateY, paint)
    }
    
    private fun drawBatteryInfo(canvas: Canvas, settings: ScreenSaverSettings) {
        batteryInfo?.let { battery ->
            val textColor = if (settings.blackBackground || settings.invertColors) Color.WHITE else Color.BLACK
            paint.color = textColor
            paint.textSize = 18f
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Paint.Align.CENTER
            
            val batteryText = "${battery.level}%"
            val batteryStatus = battery.getBatteryStatusText()
            val batteryY = centerY + 150
            
            if (settings.textOutline) {
                outlinePaint.color = if (textColor == Color.WHITE) Color.BLACK else Color.WHITE
                canvas.drawText(batteryText, centerX, batteryY, outlinePaint)
                canvas.drawText(batteryStatus, centerX, batteryY + 30, outlinePaint)
            }
            
            canvas.drawText(batteryText, centerX, batteryY, paint)
            canvas.drawText(batteryStatus, centerX, batteryY + 30, paint)
            
            if (battery.isCharging) {
                val chargingText = "Charging"
                if (settings.textOutline) {
                    canvas.drawText(chargingText, centerX, batteryY + 60, outlinePaint)
                }
                canvas.drawText(chargingText, centerX, batteryY + 60, paint)
            }
        }
    }
    
    private fun drawWeatherInfo(canvas: Canvas, settings: ScreenSaverSettings) {
        if (!settings.showWeather) return
        
        weatherData?.let { weather ->
            val textColor = if (settings.blackBackground || settings.invertColors) Color.WHITE else Color.BLACK
            paint.color = textColor
            paint.textSize = 16f
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Paint.Align.LEFT
            
            val weatherY = 100f
            val weatherX = 50f
            
            val weatherText = "${weather.temperature}${weather.temperatureUnit} ${weather.condition}"
            val locationText = weather.location
            
            if (settings.textOutline) {
                outlinePaint.color = if (textColor == Color.WHITE) Color.BLACK else Color.WHITE
                canvas.drawText(weatherText, weatherX, weatherY, outlinePaint)
                canvas.drawText(locationText, weatherX, weatherY + 25, outlinePaint)
            }
            
            canvas.drawText(weatherText, weatherX, weatherY, paint)
            canvas.drawText(locationText, weatherX, weatherY + 25, paint)
        }
    }
    
    private fun drawCalendarEvents(canvas: Canvas, settings: ScreenSaverSettings) {
        if (!settings.showCalendar || calendarEvents.isEmpty()) return
        
        val textColor = if (settings.blackBackground || settings.invertColors) Color.WHITE else Color.BLACK
        paint.color = textColor
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.LEFT
        
        val startY = viewHeight - 200f
        val eventX = 50f
        var currentY = startY
        
        calendarEvents.take(settings.calendarMaxEvents).forEach { event ->
            val eventText = "${event.getFormattedTime()} - ${event.title}"
            
            if (settings.textOutline) {
                outlinePaint.color = if (textColor == Color.WHITE) Color.BLACK else Color.WHITE
                canvas.drawText(eventText, eventX, currentY, outlinePaint)
            }
            
            canvas.drawText(eventText, eventX, currentY, paint)
            currentY += 25
        }
    }
    
    private fun drawNotifications(canvas: Canvas, settings: ScreenSaverSettings) {
        if (!settings.showNotifications || notifications.isEmpty()) return
        
        val textColor = if (settings.blackBackground || settings.invertColors) Color.WHITE else Color.BLACK
        paint.color = textColor
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.RIGHT
        
        val notificationX = viewWidth - 50f
        val startY = 100f
        var currentY = startY
        
        notifications.take(5).forEach { notification ->
            val notificationText = "${notification.appName}: ${notification.title}"
            
            if (settings.textOutline) {
                outlinePaint.color = if (textColor == Color.WHITE) Color.BLACK else Color.WHITE
                canvas.drawText(notificationText, notificationX, currentY, outlinePaint)
            }
            
            canvas.drawText(notificationText, notificationX, currentY, paint)
            currentY += 20
        }
    }
    
    fun applySettings(settings: ScreenSaverSettings) {
        this.settings = settings
        
        if (settings.customFontPath.isNotEmpty()) {
            try {
                clockFont = Typeface.createFromFile(settings.customFontPath)
            } catch (e: Exception) {
                clockFont = Typeface.DEFAULT_BOLD
            }
        } else {
            clockFont = Typeface.DEFAULT_BOLD
        }
        
        invalidate()
    }
    
    fun updateContent(
        timeString: String,
        batteryInfo: BatteryInfo?,
        weatherData: WeatherData?,
        calendarEvents: List<CalendarEvent>,
        notifications: List<NotificationData>,
        customImagePath: String
    ) {
        this.timeString = timeString
        this.batteryInfo = batteryInfo
        this.weatherData = weatherData
        this.calendarEvents = calendarEvents
        this.notifications = notifications
        
        if (this.customImagePath != customImagePath) {
            this.customImagePath = customImagePath
            loadCustomImage(customImagePath)
        }
        
        invalidate()
    }
    
    fun updateBatteryInfo(batteryInfo: BatteryInfo) {
        this.batteryInfo = batteryInfo
        invalidate()
    }
    
    private fun loadCustomImage(imagePath: String) {
        if (imagePath.isEmpty()) {
            customImage = null
            return
        }
        
        try {
            // This would load the actual image from the path
            // For now, we'll just set it to null
            customImage = null
        } catch (e: Exception) {
            customImage = null
        }
    }
}