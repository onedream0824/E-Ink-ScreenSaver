package com.einkscreensaver.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.einkscreensaver.app.R
import com.einkscreensaver.app.databinding.ActivityMainBinding
import com.einkscreensaver.app.data.repository.SettingsRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsRepository: SettingsRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        settingsRepository = SettingsRepository(this)
        
        setupUI()
        checkScreenSaverPermission()
    }
    
    private fun setupUI() {
        binding.apply {
            btnStartScreenSaver.setOnClickListener {
                startScreenSaver()
            }
            
            btnSettings.setOnClickListener {
                openSettings()
            }
            
            btnAbout.setOnClickListener {
                openAbout()
            }
            
            btnTestScreenSaver.setOnClickListener {
                testScreenSaver()
            }
        }
        
        updateUI()
    }
    
    private fun updateUI() {
        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            
            binding.apply {
                tvWelcomeTitle.text = getString(R.string.welcome_title)
                tvWelcomeSubtitle.text = getString(R.string.welcome_subtitle)
                
                btnStartScreenSaver.text = getString(R.string.start_screensaver)
                btnSettings.text = getString(R.string.settings)
                btnAbout.text = getString(R.string.about)
                btnTestScreenSaver.text = "Test ScreenSaver"
                
                if (settings.debugMode) {
                    btnTestScreenSaver.visibility = View.VISIBLE
                } else {
                    btnTestScreenSaver.visibility = View.GONE
                }
            }
        }
    }
    
    private fun checkScreenSaverPermission() {
        try {
            val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Handle case where dream settings are not available
        }
    }
    
    private fun startScreenSaver() {
        try {
            val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to system settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }
    
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun openAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
    
    private fun testScreenSaver() {
        val intent = Intent(this, TestScreenSaverActivity::class.java)
        startActivity(intent)
    }
}