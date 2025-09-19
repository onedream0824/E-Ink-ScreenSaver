package com.einkscreensaver.app.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.einkscreensaver.app.R
import com.einkscreensaver.app.data.repository.SettingsRepository
import com.einkscreensaver.app.databinding.ActivityTestScreenSaverBinding
import com.einkscreensaver.app.ui.view.ScreenSaverView
import kotlinx.coroutines.launch

class TestScreenSaverActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTestScreenSaverBinding
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var screenSaverView: ScreenSaverView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestScreenSaverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        settingsRepository = SettingsRepository(this)
        
        setupFullScreen()
        setupScreenSaverView()
        loadSettings()
    }
    
    private fun setupFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }
    
    private fun setupScreenSaverView() {
        screenSaverView = ScreenSaverView(this)
        binding.container.addView(screenSaverView)
        
        binding.root.setOnClickListener {
            finish()
        }
    }
    
    private fun loadSettings() {
        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            screenSaverView.applySettings(settings)
            
            // Update with current time and mock data
            val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            screenSaverView.updateContent(
                timeString = currentTime,
                batteryInfo = null,
                weatherData = null,
                calendarEvents = emptyList(),
                notifications = emptyList(),
                customImagePath = settings.customImagePath
            )
        }
    }
}