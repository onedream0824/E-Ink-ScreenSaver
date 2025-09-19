package com.einkscreensaver.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenSaverSettings(
    val dateTimeFormat: String = "EEE dd.MM",
    val clockFont: String = "Custom",
    val customFontPath: String = "",
    val textOutline: Boolean = false,
    val screenLayout: ScreenLayout = ScreenLayout.CENTERED,
    val invertColors: Boolean = false,
    val blackBackground: Boolean = false,
    val customImagePath: String = "",
    val imageFillsScreen: Boolean = false,
    val showNotifications: Boolean = true,
    val notificationCount: Boolean = true,
    val notificationPreview: Boolean = false,
    val refreshMode: RefreshMode = RefreshMode.HYBRID,
    val refreshInterval: Int = 5,
    val showWeather: Boolean = false,
    val weatherLocation: String = "",
    val weatherUnits: WeatherUnits = WeatherUnits.CELSIUS,
    val weatherUpdateInterval: Int = 30,
    val showCalendar: Boolean = false,
    val calendarDaysAhead: Int = 7,
    val calendarMaxEvents: Int = 5,
    val autoBrightness: Boolean = true,
    val brightnessLevel: Int = 50,
    val powerSaveMode: Boolean = false,
    val cpuFrequencyLimit: Boolean = false,
    val theme: Theme = Theme.CLASSIC,
    val imageSource: ImageSource = ImageSource.LOCAL,
    val imageRotation: ImageRotation = ImageRotation.DAILY,
    val debugMode: Boolean = false,
    val logLevel: LogLevel = LogLevel.INFO,
    val performanceMonitoring: Boolean = false,
    val memoryOptimization: Boolean = true
) : Parcelable

enum class ScreenLayout {
    CENTERED, LEFT_ALIGNED, RIGHT_ALIGNED, FULL_SCREEN
}

enum class RefreshMode {
    ALWAYS_ON, BATTERY_SAVER, HYBRID, DEEP_SLEEP
}

enum class WeatherUnits {
    CELSIUS, FAHRENHEIT
}

enum class Theme {
    CLASSIC, MODERN, MINIMAL, DARK, CUSTOM
}

enum class ImageSource {
    LOCAL, RANDOM_INTERNET, UNSPLASH_API, CUSTOM_URL
}

enum class ImageRotation {
    DAILY, HOURLY, MANUAL
}

enum class LogLevel {
    VERBOSE, DEBUG, INFO, WARN, ERROR
}