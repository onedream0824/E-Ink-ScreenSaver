package com.einkscreensaver.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.einkscreensaver.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "screensaver_settings")
    
    private val dateTimeFormatKey = stringPreferencesKey("date_time_format")
    private val clockFontKey = stringPreferencesKey("clock_font")
    private val customFontPathKey = stringPreferencesKey("custom_font_path")
    private val textOutlineKey = booleanPreferencesKey("text_outline")
    private val screenLayoutKey = stringPreferencesKey("screen_layout")
    private val invertColorsKey = booleanPreferencesKey("invert_colors")
    private val blackBackgroundKey = booleanPreferencesKey("black_background")
    private val customImagePathKey = stringPreferencesKey("custom_image_path")
    private val imageFillsScreenKey = booleanPreferencesKey("image_fills_screen")
    private val showNotificationsKey = booleanPreferencesKey("show_notifications")
    private val notificationCountKey = booleanPreferencesKey("notification_count")
    private val notificationPreviewKey = booleanPreferencesKey("notification_preview")
    private val refreshModeKey = stringPreferencesKey("refresh_mode")
    private val refreshIntervalKey = intPreferencesKey("refresh_interval")
    private val showWeatherKey = booleanPreferencesKey("show_weather")
    private val weatherLocationKey = stringPreferencesKey("weather_location")
    private val weatherUnitsKey = stringPreferencesKey("weather_units")
    private val weatherUpdateIntervalKey = intPreferencesKey("weather_update_interval")
    private val showCalendarKey = booleanPreferencesKey("show_calendar")
    private val calendarDaysAheadKey = intPreferencesKey("calendar_days_ahead")
    private val calendarMaxEventsKey = intPreferencesKey("calendar_max_events")
    private val autoBrightnessKey = booleanPreferencesKey("auto_brightness")
    private val brightnessLevelKey = intPreferencesKey("brightness_level")
    private val powerSaveModeKey = booleanPreferencesKey("power_save_mode")
    private val cpuFrequencyLimitKey = booleanPreferencesKey("cpu_frequency_limit")
    private val themeKey = stringPreferencesKey("theme")
    private val imageSourceKey = stringPreferencesKey("image_source")
    private val imageRotationKey = stringPreferencesKey("image_rotation")
    private val debugModeKey = booleanPreferencesKey("debug_mode")
    private val logLevelKey = stringPreferencesKey("log_level")
    private val performanceMonitoringKey = booleanPreferencesKey("performance_monitoring")
    private val memoryOptimizationKey = booleanPreferencesKey("memory_optimization")
    
    val settings: Flow<ScreenSaverSettings> = context.dataStore.data.map { preferences ->
        ScreenSaverSettings(
            dateTimeFormat = preferences[dateTimeFormatKey] ?: "EEE dd.MM",
            clockFont = preferences[clockFontKey] ?: "Custom",
            customFontPath = preferences[customFontPathKey] ?: "",
            textOutline = preferences[textOutlineKey] ?: false,
            screenLayout = ScreenLayout.valueOf(preferences[screenLayoutKey] ?: "CENTERED"),
            invertColors = preferences[invertColorsKey] ?: false,
            blackBackground = preferences[blackBackgroundKey] ?: false,
            customImagePath = preferences[customImagePathKey] ?: "",
            imageFillsScreen = preferences[imageFillsScreenKey] ?: false,
            showNotifications = preferences[showNotificationsKey] ?: true,
            notificationCount = preferences[notificationCountKey] ?: true,
            notificationPreview = preferences[notificationPreviewKey] ?: false,
            refreshMode = RefreshMode.valueOf(preferences[refreshModeKey] ?: "HYBRID"),
            refreshInterval = preferences[refreshIntervalKey] ?: 5,
            showWeather = preferences[showWeatherKey] ?: false,
            weatherLocation = preferences[weatherLocationKey] ?: "",
            weatherUnits = WeatherUnits.valueOf(preferences[weatherUnitsKey] ?: "CELSIUS"),
            weatherUpdateInterval = preferences[weatherUpdateIntervalKey] ?: 30,
            showCalendar = preferences[showCalendarKey] ?: false,
            calendarDaysAhead = preferences[calendarDaysAheadKey] ?: 7,
            calendarMaxEvents = preferences[calendarMaxEventsKey] ?: 5,
            autoBrightness = preferences[autoBrightnessKey] ?: true,
            brightnessLevel = preferences[brightnessLevelKey] ?: 50,
            powerSaveMode = preferences[powerSaveModeKey] ?: false,
            cpuFrequencyLimit = preferences[cpuFrequencyLimitKey] ?: false,
            theme = Theme.valueOf(preferences[themeKey] ?: "CLASSIC"),
            imageSource = ImageSource.valueOf(preferences[imageSourceKey] ?: "LOCAL"),
            imageRotation = ImageRotation.valueOf(preferences[imageRotationKey] ?: "DAILY"),
            debugMode = preferences[debugModeKey] ?: false,
            logLevel = LogLevel.valueOf(preferences[logLevelKey] ?: "INFO"),
            performanceMonitoring = preferences[performanceMonitoringKey] ?: false,
            memoryOptimization = preferences[memoryOptimizationKey] ?: true
        )
    }
    
    suspend fun updateSettings(settings: ScreenSaverSettings) {
        context.dataStore.edit { preferences ->
            preferences[dateTimeFormatKey] = settings.dateTimeFormat
            preferences[clockFontKey] = settings.clockFont
            preferences[customFontPathKey] = settings.customFontPath
            preferences[textOutlineKey] = settings.textOutline
            preferences[screenLayoutKey] = settings.screenLayout.name
            preferences[invertColorsKey] = settings.invertColors
            preferences[blackBackgroundKey] = settings.blackBackground
            preferences[customImagePathKey] = settings.customImagePath
            preferences[imageFillsScreenKey] = settings.imageFillsScreen
            preferences[showNotificationsKey] = settings.showNotifications
            preferences[notificationCountKey] = settings.notificationCount
            preferences[notificationPreviewKey] = settings.notificationPreview
            preferences[refreshModeKey] = settings.refreshMode.name
            preferences[refreshIntervalKey] = settings.refreshInterval
            preferences[showWeatherKey] = settings.showWeather
            preferences[weatherLocationKey] = settings.weatherLocation
            preferences[weatherUnitsKey] = settings.weatherUnits.name
            preferences[weatherUpdateIntervalKey] = settings.weatherUpdateInterval
            preferences[showCalendarKey] = settings.showCalendar
            preferences[calendarDaysAheadKey] = settings.calendarDaysAhead
            preferences[calendarMaxEventsKey] = settings.calendarMaxEvents
            preferences[autoBrightnessKey] = settings.autoBrightness
            preferences[brightnessLevelKey] = settings.brightnessLevel
            preferences[powerSaveModeKey] = settings.powerSaveMode
            preferences[cpuFrequencyLimitKey] = settings.cpuFrequencyLimit
            preferences[themeKey] = settings.theme.name
            preferences[imageSourceKey] = settings.imageSource.name
            preferences[imageRotationKey] = settings.imageRotation.name
            preferences[debugModeKey] = settings.debugMode
            preferences[logLevelKey] = settings.logLevel.name
            preferences[performanceMonitoringKey] = settings.performanceMonitoring
            preferences[memoryOptimizationKey] = settings.memoryOptimization
        }
    }
    
    suspend fun updateDateTimeFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[dateTimeFormatKey] = format
        }
    }
    
    suspend fun updateClockFont(font: String) {
        context.dataStore.edit { preferences ->
            preferences[clockFontKey] = font
        }
    }
    
    suspend fun updateCustomFontPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[customFontPathKey] = path
        }
    }
    
    suspend fun updateTextOutline(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[textOutlineKey] = enabled
        }
    }
    
    suspend fun updateScreenLayout(layout: ScreenLayout) {
        context.dataStore.edit { preferences ->
            preferences[screenLayoutKey] = layout.name
        }
    }
    
    suspend fun updateInvertColors(invert: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[invertColorsKey] = invert
        }
    }
    
    suspend fun updateBlackBackground(black: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[blackBackgroundKey] = black
        }
    }
    
    suspend fun updateCustomImagePath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[customImagePathKey] = path
        }
    }
    
    suspend fun updateImageFillsScreen(fills: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[imageFillsScreenKey] = fills
        }
    }
    
    suspend fun updateRefreshMode(mode: RefreshMode) {
        context.dataStore.edit { preferences ->
            preferences[refreshModeKey] = mode.name
        }
    }
    
    suspend fun updateRefreshInterval(interval: Int) {
        context.dataStore.edit { preferences ->
            preferences[refreshIntervalKey] = interval
        }
    }
    
    suspend fun updateTheme(theme: Theme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }
    
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}