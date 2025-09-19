package com.einkscreensaver.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.einkscreensaver.app.data.model.ImageRotation
import com.einkscreensaver.app.data.model.ImageSource
import com.einkscreensaver.app.data.repository.SettingsRepository
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

class ImageUpdateService : Service() {
    
    private lateinit var settingsRepository: SettingsRepository
    private var updateJob: Job? = null
    private lateinit var unsplashApi: UnsplashApi
    
    companion object {
        private const val TAG = "ImageUpdateService"
        private const val UNSPLASH_ACCESS_KEY = "your_unsplash_access_key_here"
    }
    
    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        unsplashApi = retrofit.create(UnsplashApi::class.java)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startImageUpdates()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startImageUpdates() {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val settings = settingsRepository.settings.first()
                    
                    if (settings.imageSource == ImageSource.RANDOM_INTERNET || 
                        settings.imageSource == ImageSource.UNSPLASH_API) {
                        
                        val shouldUpdate = when (settings.imageRotation) {
                            ImageRotation.DAILY -> shouldUpdateDaily()
                            ImageRotation.HOURLY -> shouldUpdateHourly()
                            ImageRotation.MANUAL -> false
                        }
                        
                        if (shouldUpdate) {
                            val imageUrl = fetchRandomImage(settings.imageSource)
                            imageUrl?.let { url ->
                                broadcastImageUpdate(url)
                            }
                        }
                    }
                    
                    delay(getUpdateInterval(settings.imageRotation))
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating image", e)
                    delay(60 * 60 * 1000L) // Retry after 1 hour
                }
            }
        }
    }
    
    private fun shouldUpdateDaily(): Boolean {
        val lastUpdate = getLastImageUpdate()
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        return (now - lastUpdate) >= oneDay
    }
    
    private fun shouldUpdateHourly(): Boolean {
        val lastUpdate = getLastImageUpdate()
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        return (now - lastUpdate) >= oneHour
    }
    
    private fun getLastImageUpdate(): Long {
        val prefs = getSharedPreferences("image_updates", MODE_PRIVATE)
        return prefs.getLong("last_update", 0L)
    }
    
    private fun setLastImageUpdate() {
        val prefs = getSharedPreferences("image_updates", MODE_PRIVATE)
        prefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
    }
    
    private fun getUpdateInterval(rotation: ImageRotation): Long {
        return when (rotation) {
            ImageRotation.DAILY -> 24 * 60 * 60 * 1000L
            ImageRotation.HOURLY -> 60 * 60 * 1000L
            ImageRotation.MANUAL -> Long.MAX_VALUE
        }
    }
    
    private suspend fun fetchRandomImage(source: ImageSource): String? {
        return try {
            when (source) {
                ImageSource.UNSPLASH_API -> {
                    val response = unsplashApi.getRandomPhoto(UNSPLASH_ACCESS_KEY)
                    response.urls?.regular
                }
                ImageSource.RANDOM_INTERNET -> {
                    // Use a different random image API
                    "https://picsum.photos/1920/1080"
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching random image", e)
            null
        }
    }
    
    private fun broadcastImageUpdate(imageUrl: String) {
        setLastImageUpdate()
        val intent = Intent("com.einkscreensaver.app.IMAGE_UPDATE")
        intent.putExtra("image_url", imageUrl)
        sendBroadcast(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
    }
    
    interface UnsplashApi {
        @GET("photos/random")
        suspend fun getRandomPhoto(
            @Query("client_id") accessKey: String,
            @Query("orientation") orientation: String = "landscape",
            @Query("w") width: Int = 1920,
            @Query("h") height: Int = 1080
        ): UnsplashResponse
    }
    
    data class UnsplashResponse(
        val id: String,
        val urls: UnsplashUrls?,
        val user: UnsplashUser?,
        val description: String?
    )
    
    data class UnsplashUrls(
        val raw: String?,
        val full: String?,
        val regular: String?,
        val small: String?,
        val thumb: String?
    )
    
    data class UnsplashUser(
        val name: String?,
        val username: String?
    )
}