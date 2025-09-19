package com.einkscreensaver.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.einkscreensaver.app.data.model.WeatherData
import com.einkscreensaver.app.data.repository.SettingsRepository
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class WeatherUpdateService : Service() {
    
    private lateinit var settingsRepository: SettingsRepository
    private var updateJob: Job? = null
    private lateinit var weatherApi: WeatherApi
    
    companion object {
        private const val TAG = "WeatherUpdateService"
        private const val API_KEY = "your_api_key_here"
    }
    
    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        weatherApi = retrofit.create(WeatherApi::class.java)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWeatherUpdates()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startWeatherUpdates() {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val settings = settingsRepository.settings.first()
                    if (settings.showWeather && settings.weatherLocation.isNotEmpty()) {
                        val weatherData = fetchWeatherData(settings.weatherLocation, settings.weatherUnits)
                        // Store weather data or broadcast it
                        broadcastWeatherUpdate(weatherData)
                    }
                    
                    delay(settings.weatherUpdateInterval * 60 * 1000L)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating weather", e)
                    delay(5 * 60 * 1000L) // Retry after 5 minutes
                }
            }
        }
    }
    
    private suspend fun fetchWeatherData(location: String, units: WeatherUnits): WeatherData? {
        return try {
            val unit = when (units) {
                WeatherUnits.CELSIUS -> "metric"
                WeatherUnits.FAHRENHEIT -> "imperial"
            }
            
            val response = weatherApi.getCurrentWeather(location, API_KEY, unit)
            
            WeatherData(
                temperature = response.main.temp,
                temperatureUnit = if (units == WeatherUnits.CELSIUS) "°C" else "°F",
                condition = response.weather.firstOrNull()?.main ?: "Unknown",
                conditionIcon = response.weather.firstOrNull()?.icon ?: "",
                humidity = response.main.humidity,
                windSpeed = response.wind.speed,
                windDirection = response.wind.deg.toString(),
                pressure = response.main.pressure,
                visibility = response.visibility / 1000.0,
                uvIndex = 0, // Would need separate API call
                feelsLike = response.main.feelsLike,
                minTemp = response.main.tempMin,
                maxTemp = response.main.tempMax,
                sunrise = "", // Would need to convert timestamp
                sunset = "", // Would need to convert timestamp
                location = response.name,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data", e)
            null
        }
    }
    
    private fun broadcastWeatherUpdate(weatherData: WeatherData?) {
        val intent = Intent("com.einkscreensaver.app.WEATHER_UPDATE")
        intent.putExtra("weather_data", weatherData)
        sendBroadcast(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
    }
    
    interface WeatherApi {
        @GET("weather")
        suspend fun getCurrentWeather(
            @Query("q") location: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String
        ): WeatherResponse
    }
    
    data class WeatherResponse(
        val main: Main,
        val weather: List<Weather>,
        val wind: Wind,
        val visibility: Int,
        val name: String
    )
    
    data class Main(
        val temp: Double,
        val feelsLike: Double,
        val tempMin: Double,
        val tempMax: Double,
        val pressure: Double,
        val humidity: Int
    )
    
    data class Weather(
        val main: String,
        val description: String,
        val icon: String
    )
    
    data class Wind(
        val speed: Double,
        val deg: Int
    )
}