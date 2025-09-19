package com.einkscreensaver.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.einkscreensaver.app.R
import com.einkscreensaver.app.data.model.*
import com.einkscreensaver.app.data.repository.SettingsRepository
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var dateTimeFormatPreference: EditTextPreference
    private lateinit var clockFontPreference: ListPreference
    private lateinit var customFontPreference: Preference
    private lateinit var textOutlinePreference: SwitchPreferenceCompat
    private lateinit var screenLayoutPreference: ListPreference
    private lateinit var invertColorsPreference: SwitchPreferenceCompat
    private lateinit var blackBackgroundPreference: SwitchPreferenceCompat
    private lateinit var customImagePreference: Preference
    private lateinit var imageFillsScreenPreference: SwitchPreferenceCompat
    private lateinit var showNotificationsPreference: SwitchPreferenceCompat
    private lateinit var notificationCountPreference: SwitchPreferenceCompat
    private lateinit var notificationPreviewPreference: SwitchPreferenceCompat
    private lateinit var refreshModePreference: ListPreference
    private lateinit var refreshIntervalPreference: SeekBarPreference
    private lateinit var showWeatherPreference: SwitchPreferenceCompat
    private lateinit var weatherLocationPreference: EditTextPreference
    private lateinit var weatherUnitsPreference: ListPreference
    private lateinit var weatherUpdateIntervalPreference: SeekBarPreference
    private lateinit var showCalendarPreference: SwitchPreferenceCompat
    private lateinit var calendarDaysAheadPreference: SeekBarPreference
    private lateinit var calendarMaxEventsPreference: SeekBarPreference
    private lateinit var autoBrightnessPreference: SwitchPreferenceCompat
    private lateinit var brightnessLevelPreference: SeekBarPreference
    private lateinit var powerSaveModePreference: SwitchPreferenceCompat
    private lateinit var cpuFrequencyLimitPreference: SwitchPreferenceCompat
    private lateinit var themePreference: ListPreference
    private lateinit var imageSourcePreference: ListPreference
    private lateinit var imageRotationPreference: ListPreference
    private lateinit var debugModePreference: SwitchPreferenceCompat
    private lateinit var logLevelPreference: ListPreference
    private lateinit var performanceMonitoringPreference: SwitchPreferenceCompat
    private lateinit var memoryOptimizationPreference: SwitchPreferenceCompat
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        settingsRepository = SettingsRepository(this)
        
        setupToolbar()
        setupPreferences()
        loadSettings()
    }
    
    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)
    }
    
    private fun setupPreferences() {
        val fragment = SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
            .commit()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_reset -> {
                showResetDialog()
                true
            }
            R.id.action_preview -> {
                previewSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showResetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to default values?")
            .setPositiveButton("Reset") { _, _ ->
                resetSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetSettings() {
        lifecycleScope.launch {
            settingsRepository.resetToDefaults()
            loadSettings()
        }
    }
    
    private fun previewSettings() {
        val intent = Intent(this, TestScreenSaverActivity::class.java)
        startActivity(intent)
    }
    
    private fun loadSettings() {
        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            updatePreferences(settings)
        }
    }
    
    private fun updatePreferences(settings: ScreenSaverSettings) {
        // This would update all preference values
        // Implementation depends on the preference fragment structure
    }
    
    class SettingsFragment : PreferenceFragmentCompat() {
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
            
            setupPreferences()
            setupListeners()
        }
        
        private fun setupPreferences() {
            // Date/Time Format
            findPreference<EditTextPreference>("date_time_format")?.apply {
                summary = "Current format: ${preferenceManager.sharedPreferences.getString("date_time_format", "EEE dd.MM")}"
            }
            
            // Clock Font
            findPreference<ListPreference>("clock_font")?.apply {
                summary = "Current font: ${preferenceManager.sharedPreferences.getString("clock_font", "Custom")}"
            }
            
            // Custom Font
            findPreference<Preference>("custom_font")?.apply {
                summary = "Tap to select custom font file"
            }
            
            // Screen Layout
            findPreference<ListPreference>("screen_layout")?.apply {
                summary = "Current layout: ${preferenceManager.sharedPreferences.getString("screen_layout", "Centered")}"
            }
            
            // Refresh Mode
            findPreference<ListPreference>("refresh_mode")?.apply {
                summary = "Current mode: ${preferenceManager.sharedPreferences.getString("refresh_mode", "Hybrid")}"
            }
            
            // Theme
            findPreference<ListPreference>("theme")?.apply {
                summary = "Current theme: ${preferenceManager.sharedPreferences.getString("theme", "Classic")}"
            }
            
            // Weather Location
            findPreference<EditTextPreference>("weather_location")?.apply {
                summary = "Current location: ${preferenceManager.sharedPreferences.getString("weather_location", "Current Location")}"
            }
            
            // Weather Units
            findPreference<ListPreference>("weather_units")?.apply {
                summary = "Current units: ${preferenceManager.sharedPreferences.getString("weather_units", "Celsius")}"
            }
            
            // Image Source
            findPreference<ListPreference>("image_source")?.apply {
                summary = "Current source: ${preferenceManager.sharedPreferences.getString("image_source", "Local Images")}"
            }
            
            // Image Rotation
            findPreference<ListPreference>("image_rotation")?.apply {
                summary = "Current rotation: ${preferenceManager.sharedPreferences.getString("image_rotation", "Daily")}"
            }
            
            // Log Level
            findPreference<ListPreference>("log_level")?.apply {
                summary = "Current level: ${preferenceManager.sharedPreferences.getString("log_level", "Info")}"
            }
        }
        
        private fun setupListeners() {
            // Custom Font Selection
            findPreference<Preference>("custom_font")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), FontSelectionActivity::class.java)
                startActivity(intent)
                true
            }
            
            // Custom Image Selection
            findPreference<Preference>("custom_image")?.setOnPreferenceClickListener {
                val intent = Intent(requireContext(), ImageSelectionActivity::class.java)
                startActivity(intent)
                true
            }
            
            // Refresh Mode Info
            findPreference<Preference>("refresh_mode_info")?.setOnPreferenceClickListener {
                showRefreshModeInfo()
                true
            }
            
            // Weather Location
            findPreference<EditTextPreference>("weather_location")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<EditTextPreference>("weather_location")?.summary = "Current location: $newValue"
                true
            }
            
            // Date/Time Format
            findPreference<EditTextPreference>("date_time_format")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<EditTextPreference>("date_time_format")?.summary = "Current format: $newValue"
                true
            }
            
            // Clock Font
            findPreference<ListPreference>("clock_font")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("clock_font")?.summary = "Current font: $newValue"
                true
            }
            
            // Screen Layout
            findPreference<ListPreference>("screen_layout")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("screen_layout")?.summary = "Current layout: $newValue"
                true
            }
            
            // Refresh Mode
            findPreference<ListPreference>("refresh_mode")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("refresh_mode")?.summary = "Current mode: $newValue"
                true
            }
            
            // Theme
            findPreference<ListPreference>("theme")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("theme")?.summary = "Current theme: $newValue"
                true
            }
            
            // Weather Units
            findPreference<ListPreference>("weather_units")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("weather_units")?.summary = "Current units: $newValue"
                true
            }
            
            // Image Source
            findPreference<ListPreference>("image_source")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("image_source")?.summary = "Current source: $newValue"
                true
            }
            
            // Image Rotation
            findPreference<ListPreference>("image_rotation")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("image_rotation")?.summary = "Current rotation: $newValue"
                true
            }
            
            // Log Level
            findPreference<ListPreference>("log_level")?.setOnPreferenceChangeListener { _, newValue ->
                findPreference<ListPreference>("log_level")?.summary = "Current level: $newValue"
                true
            }
        }
        
        private fun showRefreshModeInfo() {
            AlertDialog.Builder(requireContext())
                .setTitle("Refresh Mode Information")
                .setMessage("""
                    Always On: Updates happen smoothly, screen light may turn to cold color temperature.
                    
                    Battery Saver: Phone sleeps between updates to save power. May require PIN entry.
                    
                    Hybrid: Uses Always On initially, switches to Battery Saver after 2 hours of inactivity.
                    
                    Deep Sleep: Uses Always On initially, shows static image after 2 hours of inactivity.
                """.trimIndent())
                .setPositiveButton("OK", null)
                .show()
        }
    }
}