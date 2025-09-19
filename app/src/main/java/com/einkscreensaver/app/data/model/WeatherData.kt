package com.einkscreensaver.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherData(
    val temperature: Double,
    val temperatureUnit: String,
    val condition: String,
    val conditionIcon: String,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: String,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Int,
    val feelsLike: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val sunrise: String,
    val sunset: String,
    val location: String,
    val lastUpdated: Long,
    val forecast: List<ForecastItem> = emptyList()
) : Parcelable

@Parcelize
data class ForecastItem(
    val date: String,
    val dayOfWeek: String,
    val minTemp: Double,
    val maxTemp: Double,
    val condition: String,
    val conditionIcon: String,
    val precipitation: Double,
    val humidity: Int
) : Parcelable

enum class WeatherCondition {
    SUNNY, PARTLY_CLOUDY, CLOUDY, RAINY, STORMY, SNOWY, FOGGY, WINDY, UNKNOWN
}

fun String.toWeatherCondition(): WeatherCondition {
    return when (this.lowercase()) {
        "clear", "sunny" -> WeatherCondition.SUNNY
        "partly cloudy", "partly_cloudy" -> WeatherCondition.PARTLY_CLOUDY
        "cloudy", "overcast" -> WeatherCondition.CLOUDY
        "rain", "rainy", "drizzle" -> WeatherCondition.RAINY
        "storm", "thunderstorm" -> WeatherCondition.STORMY
        "snow", "snowy" -> WeatherCondition.SNOWY
        "fog", "foggy", "mist" -> WeatherCondition.FOGGY
        "windy" -> WeatherCondition.WINDY
        else -> WeatherCondition.UNKNOWN
    }
}