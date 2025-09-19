package com.einkscreensaver.app.service

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.*
import android.service.dreams.DreamService
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.einkscreensaver.app.R
import com.einkscreensaver.app.data.model.*
import com.einkscreensaver.app.data.repository.SettingsRepository
import com.einkscreensaver.app.ui.view.ScreenSaverView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class EInkScreenSaverService : DreamService() {
    
    private lateinit var screenSaverView: ScreenSaverView
    private lateinit var settingsRepository: SettingsRepository
    private var currentSettings: ScreenSaverSettings? = null
    private var updateJob: Job? = null
    private var refreshHandler: Handler? = null
    private var refreshRunnable: Runnable? = null
    
    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_TIME_TICK,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_DATE_CHANGED -> {
                    updateDisplay()
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    updateBatteryInfo()
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.Theme_EInkScreenSaver_ScreenSaver)
        
        settingsRepository = SettingsRepository(this)
        screenSaverView = ScreenSaverView(this)
        setContentView(screenSaverView)
        
        setupPowerManagement()
        setupRefreshHandler()
    }
    
    override fun onDreamingStarted() {
        super.onDreamingStarted()
        
        lifecycleScope.launch {
            currentSettings = settingsRepository.settings.first()
            currentSettings?.let { settings ->
                applySettings(settings)
                startRefreshCycle(settings)
            }
        }
        
        registerReceivers()
        updateDisplay()
        
        if (currentSettings?.debugMode == true) {
            Log.d(TAG, "ScreenSaver started")
        }
    }
    
    override fun onDreamingStopped() {
        super.onDreamingStopped()
        
        unregisterReceivers()
        stopRefreshCycle()
        updateJob?.cancel()
        
        if (currentSettings?.debugMode == true) {
            Log.d(TAG, "ScreenSaver stopped")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        refreshHandler?.removeCallbacksAndMessages(null)
        updateJob?.cancel()
    }
    
    private fun setupPowerManagement() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }
    
    private fun setupRefreshHandler() {
        refreshHandler = Handler(Looper.getMainLooper())
    }
    
    private fun applySettings(settings: ScreenSaverSettings) {
        screenSaverView.applySettings(settings)
        
        when (settings.theme) {
            Theme.DARK -> setTheme(R.style.Theme_EInkScreenSaver_Dark)
            Theme.CLASSIC -> setTheme(R.style.Theme_EInkScreenSaver)
            else -> setTheme(R.style.Theme_EInkScreenSaver)
        }
        
        if (settings.blackBackground) {
            window.decorView.setBackgroundColor(Color.BLACK)
        } else {
            window.decorView.setBackgroundColor(Color.WHITE)
        }
        
        if (settings.autoBrightness) {
            val brightness = settings.brightnessLevel / 100f
            val layoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
        }
    }
    
    private fun startRefreshCycle(settings: ScreenSaverSettings) {
        val interval = when (settings.refreshMode) {
            RefreshMode.ALWAYS_ON -> 1000L
            RefreshMode.BATTERY_SAVER -> settings.refreshInterval * 60 * 1000L
            RefreshMode.HYBRID -> 1000L
            RefreshMode.DEEP_SLEEP -> settings.refreshInterval * 60 * 1000L
        }
        
        refreshRunnable = object : Runnable {
            override fun run() {
                updateDisplay()
                refreshHandler?.postDelayed(this, interval)
            }
        }
        
        refreshHandler?.post(refreshRunnable!!)
    }
    
    private fun stopRefreshCycle() {
        refreshRunnable?.let { refreshHandler?.removeCallbacks(it) }
        refreshRunnable = null
    }
    
    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(timeTickReceiver, filter)
    }
    
    private fun unregisterReceivers() {
        try {
            unregisterReceiver(timeTickReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
    }
    
    private fun updateDisplay() {
        lifecycleScope.launch {
            try {
                val settings = currentSettings ?: return@launch
                
                val currentTime = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat(settings.dateTimeFormat, Locale.getDefault())
                val timeString = dateFormat.format(Date(currentTime))
                
                val batteryInfo = getBatteryInfo()
                val weatherData = if (settings.showWeather) getWeatherData() else null
                val calendarEvents = if (settings.showCalendar) getCalendarEvents() else emptyList()
                val notifications = if (settings.showNotifications) getNotifications() else emptyList()
                
                screenSaverView.updateContent(
                    timeString = timeString,
                    batteryInfo = batteryInfo,
                    weatherData = weatherData,
                    calendarEvents = calendarEvents,
                    notifications = notifications,
                    customImagePath = settings.customImagePath
                )
                
            } catch (e: Exception) {
                if (currentSettings?.debugMode == true) {
                    Log.e(TAG, "Error updating display", e)
                }
            }
        }
    }
    
    private fun updateBatteryInfo() {
        lifecycleScope.launch {
            val batteryInfo = getBatteryInfo()
            screenSaverView.updateBatteryInfo(batteryInfo)
        }
    }
    
    private suspend fun getBatteryInfo(): BatteryInfo {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        return batteryIntent?.let { intent ->
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
            
            val powerSaveMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                batteryManager.isPowerSaveMode
            } else {
                false
            }
            
            BatteryInfo(
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
        } ?: BatteryInfo(
            level = 0,
            isCharging = false,
            chargingType = ChargingType.NONE,
            temperature = 0f,
            voltage = 0,
            health = BatteryHealth.UNKNOWN,
            technology = "Unknown",
            plugged = false,
            present = false,
            scale = 100,
            status = BatteryStatus.UNKNOWN,
            powerSaveMode = false
        )
    }
    
    private suspend fun getWeatherData(): WeatherData? {
        return try {
            // This would be implemented with actual weather API calls
            // For now, return mock data
            WeatherData(
                temperature = 22.0,
                temperatureUnit = "°C",
                condition = "Sunny",
                conditionIcon = "sunny",
                humidity = 65,
                windSpeed = 5.2,
                windDirection = "NW",
                pressure = 1013.25,
                visibility = 10.0,
                uvIndex = 6,
                feelsLike = 24.0,
                minTemp = 18.0,
                maxTemp = 26.0,
                sunrise = "06:30",
                sunset = "18:45",
                location = "Current Location",
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            if (currentSettings?.debugMode == true) {
                Log.e(TAG, "Error fetching weather data", e)
            }
            null
        }
    }
    
    private suspend fun getCalendarEvents(): List<CalendarEvent> {
        return try {
            // This would be implemented with actual calendar queries
            // For now, return mock data
            emptyList()
        } catch (e: Exception) {
            if (currentSettings?.debugMode == true) {
                Log.e(TAG, "Error fetching calendar events", e)
            }
            emptyList()
        }
    }
    
    private suspend fun getNotifications(): List<NotificationData> {
        return try {
            // This would be implemented with actual notification access
            // For now, return mock data
            emptyList()
        } catch (e: Exception) {
            if (currentSettings?.debugMode == true) {
                Log.e(TAG, "Error fetching notifications", e)
            }
            emptyList()
        }
    }
    
    companion object {
        private const val TAG = "EInkScreenSaverService"
    }
}