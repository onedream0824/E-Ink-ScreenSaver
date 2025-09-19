package com.einkscreensaver.app.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.einkscreensaver.app.data.model.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class WidgetManager(private val context: Context) {
    
    private val widgets = mutableListOf<BaseWidget>()
    private val widgetFactory = WidgetFactory()
    private var isInitialized = false
    
    fun initialize() {
        if (isInitialized) return
        
        // Register default widgets
        registerDefaultWidgets()
        isInitialized = true
    }
    
    private fun registerDefaultWidgets() {
        widgets.addAll(listOf(
            widgetFactory.createWidget(WidgetType.TIME_CLOCK),
            widgetFactory.createWidget(WidgetType.DATE_DISPLAY),
            widgetFactory.createWidget(WidgetType.BATTERY_STATUS),
            widgetFactory.createWidget(WidgetType.WEATHER_INFO),
            widgetFactory.createWidget(WidgetType.CALENDAR_EVENTS),
            widgetFactory.createWidget(WidgetType.NOTIFICATION_COUNT),
            widgetFactory.createWidget(WidgetType.SYSTEM_INFO),
            widgetFactory.createWidget(WidgetType.QUOTE_DISPLAY),
            widgetFactory.createWidget(WidgetType.PROGRESS_BAR),
            widgetFactory.createWidget(WidgetType.CHART_DISPLAY),
            widgetFactory.createWidget(WidgetType.IMAGE_GALLERY),
            widgetFactory.createWidget(WidgetType.TEXT_NOTES),
            widgetFactory.createWidget(WidgetType.QUICK_ACTIONS),
            widgetFactory.createWidget(WidgetType.STATISTICS),
            widgetFactory.createWidget(WidgetType.CUSTOM_HTML)
        ))
    }
    
    fun getWidgets(): List<BaseWidget> = widgets.toList()
    
    fun getWidget(type: WidgetType): BaseWidget? = widgets.find { it.type == type }
    
    fun addWidget(widget: BaseWidget) {
        widgets.add(widget)
    }
    
    fun removeWidget(type: WidgetType) {
        widgets.removeAll { it.type == type }
    }
    
    fun updateWidget(type: WidgetType, data: Any) {
        widgets.find { it.type == type }?.updateData(data)
    }
    
    fun drawWidgets(canvas: Canvas, settings: ScreenSaverSettings) {
        widgets.forEach { widget ->
            if (widget.isEnabled()) {
                widget.draw(canvas, settings)
            }
        }
    }
    
    fun updateAllWidgets() {
        widgets.forEach { it.update() }
    }
}

abstract class BaseWidget(
    val type: WidgetType,
    protected val context: Context
) {
    protected var isEnabled = true
    protected var position = WidgetPosition(0, 0)
    protected var size = WidgetSize(200, 100)
    protected var opacity = 1.0f
    protected var rotation = 0f
    protected var scale = 1.0f
    
    abstract fun draw(canvas: Canvas, settings: ScreenSaverSettings)
    abstract fun update()
    abstract fun updateData(data: Any)
    
    fun isEnabled(): Boolean = isEnabled
    fun setEnabled(enabled: Boolean) { isEnabled = enabled }
    fun setPosition(x: Int, y: Int) { position = WidgetPosition(x, y) }
    fun setSize(width: Int, height: Int) { size = WidgetSize(width, height) }
    fun setOpacity(opacity: Float) { this.opacity = opacity.coerceIn(0f, 1f) }
    fun setRotation(rotation: Float) { this.rotation = rotation }
    fun setScale(scale: Float) { this.scale = scale }
}

data class WidgetPosition(val x: Int, val y: Int)
data class WidgetSize(val width: Int, val height: Int)

enum class WidgetType {
    TIME_CLOCK, DATE_DISPLAY, BATTERY_STATUS, WEATHER_INFO, CALENDAR_EVENTS,
    NOTIFICATION_COUNT, SYSTEM_INFO, QUOTE_DISPLAY, PROGRESS_BAR, CHART_DISPLAY,
    IMAGE_GALLERY, TEXT_NOTES, QUICK_ACTIONS, STATISTICS, CUSTOM_HTML
}

class TimeClockWidget(context: Context) : BaseWidget(WidgetType.TIME_CLOCK, context) {
    private var currentTime = ""
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun draw(canvas: Canvas, settings: ScreenSaverSettings) {
        val paint = Paint().apply {
            color = if (settings.blackBackground) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            textSize = 72f
            textAlign = Paint.Align.CENTER
            alpha = (opacity * 255).toInt()
        }
        
        canvas.save()
        canvas.translate(position.x.toFloat(), position.y.toFloat())
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        
        canvas.drawText(currentTime, size.width / 2f, size.height / 2f, paint)
        canvas.restore()
    }
    
    override fun update() {
        currentTime = timeFormat.format(Date())
    }
    
    override fun updateData(data: Any) {
        if (data is String) {
            currentTime = data
        }
    }
}

class DateDisplayWidget(context: Context) : BaseWidget(WidgetType.DATE_DISPLAY, context) {
    private var currentDate = ""
    private val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    
    override fun draw(canvas: Canvas, settings: ScreenSaverSettings) {
        val paint = Paint().apply {
            color = if (settings.blackBackground) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            textSize = 24f
            textAlign = Paint.Align.CENTER
            alpha = (opacity * 255).toInt()
        }
        
        canvas.save()
        canvas.translate(position.x.toFloat(), position.y.toFloat())
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        
        canvas.drawText(currentDate, size.width / 2f, size.height / 2f, paint)
        canvas.restore()
    }
    
    override fun update() {
        currentDate = dateFormat.format(Date())
    }
    
    override fun updateData(data: Any) {
        if (data is String) {
            currentDate = data
        }
    }
}

class BatteryStatusWidget(context: Context) : BaseWidget(WidgetType.BATTERY_STATUS, context) {
    private var batteryInfo: BatteryInfo? = null
    
    override fun draw(canvas: Canvas, settings: ScreenSaverSettings) {
        batteryInfo?.let { battery ->
            val paint = Paint().apply {
                alpha = (opacity * 255).toInt()
            }
            
            canvas.save()
            canvas.translate(position.x.toFloat(), position.y.toFloat())
            canvas.rotate(rotation)
            canvas.scale(scale, scale)
            
            // Draw battery outline
            paint.color = if (settings.blackBackground) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            
            val batteryRect = RectF(0f, 0f, size.width.toFloat(), size.height.toFloat())
            canvas.drawRoundRect(batteryRect, 8f, 8f, paint)
            
            // Draw battery level
            val levelWidth = (size.width * battery.level / 100f)
            paint.style = Paint.Style.FILL
            paint.color = battery.getBatteryColor()
            
            val levelRect = RectF(4f, 4f, levelWidth - 4f, size.height.toFloat() - 4f)
            canvas.drawRoundRect(levelRect, 4f, 4f, paint)
            
            // Draw battery percentage text
            paint.color = if (settings.blackBackground) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            paint.textSize = 16f
            paint.textAlign = Paint.Align.CENTER
            
            canvas.drawText("${battery.level}%", size.width / 2f, size.height / 2f + 6f, paint)
            
            canvas.restore()
        }
    }
    
    override fun update() {
        // Battery info is updated externally
    }
    
    override fun updateData(data: Any) {
        if (data is BatteryInfo) {
            batteryInfo = data
        }
    }
}

class WeatherInfoWidget(context: Context) : BaseWidget(WidgetType.WEATHER_INFO, context) {
    private var weatherData: WeatherData? = null
    
    override fun draw(canvas: Canvas, settings: ScreenSaverSettings) {
        weatherData?.let { weather ->
            val paint = Paint().apply {
                color = if (settings.blackBackground) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
                alpha = (opacity * 255).toInt()
            }
            
            canvas.save()
            canvas.translate(position.x.toFloat(), position.y.toFloat())
            canvas.rotate(rotation)
            canvas.scale(scale, scale)
            
            // Draw temperature
            paint.textSize = 32f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${weather.temperature}${weather.temperatureUnit}", 0f, 30f, paint)
            
            // Draw condition
            paint.textSize = 16f
            canvas.drawText(weather.condition, 0f, 55f, paint)
            
            // Draw location
            paint.textSize = 12f
            canvas.drawText(weather.location, 0f, 75f, paint)
            
            canvas.restore()
        }
    }
    
    override fun update() {
        // Weather data is updated externally
    }
    
    override fun updateData(data: Any) {
        if (data is WeatherData) {
            weatherData = data
        }
    }
}

class QuoteDisplayWidget(context: Context) : BaseWidget(WidgetType.QUOTE_DISPLAY, context) {
    private var currentQuote = "The future belongs to those who believe in the beauty of their dreams."
    private var currentAuthor = "Eleanor Roosevelt"
    private var quoteIndex = 0
    
    private val quotes = listOf(
        "The future belongs to those who believe in the beauty of their dreams." to "Eleanor Roosevelt",
        "It is during our darkest moments that we must focus to see the light." to "Aristotle",
        "The way to get started is to quit talking and begin doing." to "Walt Disney",
        "Don't be pushed around by the fears in your mind. Be led by the dreams in your heart." to "Roy T. Bennett",
        "Success is not final, failure is not fatal: it is the courage to continue that counts." to "Winston Churchill"
    )
    
    override fun draw(canvas: Canvas, settings: ScreenSaverSettings) {
        val paint = Paint().apply {
            color = if (settings.blackBackground) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            alpha = (opacity * 255).toInt()
        }
        
        canvas.save()
        canvas.translate(position.x.toFloat(), position.y.toFloat())
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        
        // Draw quote text
        paint.textSize = 16f
        paint.textAlign = Paint.Align.CENTER
        
        val lines = currentQuote.split(" ").chunked(6).map { it.joinToString(" ") }
        var y = 20f
        lines.forEach { line ->
            canvas.drawText(line, size.width / 2f, y, paint)
            y += 25f
        }
        
        // Draw author
        paint.textSize = 12f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("— $currentAuthor", size.width.toFloat() - 10f, size.height.toFloat() - 10f, paint)
        
        canvas.restore()
    }
    
    override fun update() {
        // Rotate quotes every 30 seconds
        if (System.currentTimeMillis() % 30000 < 1000) {
            quoteIndex = (quoteIndex + 1) % quotes.size
            val (quote, author) = quotes[quoteIndex]
            currentQuote = quote
            currentAuthor = author
        }
    }
    
    override fun updateData(data: Any) {
        if (data is Pair<*, *>) {
            currentQuote = data.first as String
            currentAuthor = data.second as String
        }
    }
}

class ProgressBarWidget(context: Context) : BaseWidget(WidgetType.PROGRESS_BAR, context) {
    private var progress = 0f
    private var maxProgress = 100f
    private var label = "Progress"
    
    override fun draw(canvas: Canvas, settings: ScreenSaverSettings) {
        val paint = Paint().apply {
            alpha = (opacity * 255).toInt()
        }
        
        canvas.save()
        canvas.translate(position.x.toFloat(), position.y.toFloat())
        canvas.rotate(rotation)
        canvas.scale(scale, scale)
        
        // Draw background
        paint.color = if (settings.blackBackground) 0x33FFFFFF else 0x33000000
        paint.style = Paint.Style.FILL
        
        val backgroundRect = RectF(0f, 0f, size.width.toFloat(), size.height.toFloat())
        canvas.drawRoundRect(backgroundRect, 8f, 8f, paint)
        
        // Draw progress
        val progressWidth = (size.width * progress / maxProgress)
        paint.color = 0xFF4CAF50.toInt()
        
        val progressRect = RectF(0f, 0f, progressWidth, size.height.toFloat())
        canvas.drawRoundRect(progressRect, 8f, 8f, paint)
        
        // Draw label
        paint.color = if (settings.blackBackground) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
        paint.textSize = 14f
        paint.textAlign = Paint.Align.CENTER
        
        canvas.drawText(label, size.width / 2f, size.height / 2f + 5f, paint)
        
        // Draw percentage
        paint.textSize = 12f
        canvas.drawText("${(progress / maxProgress * 100).toInt()}%", size.width / 2f, size.height - 5f, paint)
        
        canvas.restore()
    }
    
    override fun update() {
        // Progress is updated externally
    }
    
    override fun updateData(data: Any) {
        when (data) {
            is Float -> progress = data
            is Int -> progress = data.toFloat()
            is Triple<*, *, *> -> {
                progress = (data.first as Number).toFloat()
                maxProgress = (data.second as Number).toFloat()
                label = data.third as String
            }
        }
    }
}

class WidgetFactory {
    fun createWidget(type: WidgetType): BaseWidget {
        return when (type) {
            WidgetType.TIME_CLOCK -> TimeClockWidget(context)
            WidgetType.DATE_DISPLAY -> DateDisplayWidget(context)
            WidgetType.BATTERY_STATUS -> BatteryStatusWidget(context)
            WidgetType.WEATHER_INFO -> WeatherInfoWidget(context)
            WidgetType.QUOTE_DISPLAY -> QuoteDisplayWidget(context)
            WidgetType.PROGRESS_BAR -> ProgressBarWidget(context)
            else -> TimeClockWidget(context) // Default fallback
        }
    }
    
    private val context: Context
        get() = throw NotImplementedError("Context must be provided")
}