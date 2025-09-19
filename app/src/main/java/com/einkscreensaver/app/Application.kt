package com.einkscreensaver.app

import android.app.Application
import com.einkscreensaver.app.ai.AIFeatures
import com.einkscreensaver.app.analytics.AnalyticsManager
import com.einkscreensaver.app.automation.AutomationEngine
import com.einkscreensaver.app.backup.BackupManager
import com.einkscreensaver.app.data.repository.SettingsRepository
import com.einkscreensaver.app.iot.IoTIntegration
import com.einkscreensaver.app.plugin.PluginManager
import com.einkscreensaver.app.scheduling.AdvancedScheduler
import com.einkscreensaver.app.security.SecurityManager
import com.einkscreensaver.app.social.SocialFeatures
import com.einkscreensaver.app.voice.VoiceControl
import com.einkscreensaver.app.widget.WidgetManager
import kotlinx.coroutines.*

class EInkScreenSaverApplication : Application() {
    
    // Core managers
    lateinit var settingsRepository: SettingsRepository
    lateinit var widgetManager: WidgetManager
    lateinit var analyticsManager: AnalyticsManager
    lateinit var securityManager: SecurityManager
    lateinit var backupManager: BackupManager
    
    // Advanced features
    lateinit var aiFeatures: AIFeatures
    lateinit var socialFeatures: SocialFeatures
    lateinit var automationEngine: AutomationEngine
    lateinit var voiceControl: VoiceControl
    lateinit var iotIntegration: IoTIntegration
    lateinit var pluginManager: PluginManager
    lateinit var advancedScheduler: AdvancedScheduler
    
    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize core managers
        initializeCoreManagers()
        
        // Initialize advanced features
        initializeAdvancedFeatures()
        
        // Start background services
        startBackgroundServices()
        
        // Initialize analytics
        initializeAnalytics()
    }
    
    private fun initializeCoreManagers() {
        settingsRepository = SettingsRepository(this)
        widgetManager = WidgetManager(this)
        analyticsManager = AnalyticsManager(this)
        securityManager = SecurityManager(this)
        backupManager = BackupManager(this)
    }
    
    private fun initializeAdvancedFeatures() {
        aiFeatures = AIFeatures(this)
        socialFeatures = SocialFeatures(this)
        automationEngine = AutomationEngine(this)
        voiceControl = VoiceControl(this)
        iotIntegration = IoTIntegration(this)
        pluginManager = PluginManager(this)
        advancedScheduler = AdvancedScheduler(this)
    }
    
    private fun startBackgroundServices() {
        applicationScope.launch {
            try {
                // Initialize all managers in parallel
                val initializationJobs = listOf(
                    async { settingsRepository.settings.first() }, // Ensure settings are loaded
                    async { widgetManager.initialize() },
                    async { analyticsManager.initialize() },
                    async { securityManager.initialize() },
                    async { backupManager.initialize() },
                    async { aiFeatures.initialize() },
                    async { socialFeatures.initialize() },
                    async { automationEngine.initialize() },
                    async { voiceControl.initialize() },
                    async { iotIntegration.initialize() },
                    async { pluginManager.initialize() },
                    async { advancedScheduler.initialize() }
                )
                
                // Wait for all initializations to complete
                initializationJobs.awaitAll()
                
                // Log successful initialization
                analyticsManager.trackEvent("app_initialized", mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "version" to getAppVersion()
                ))
                
            } catch (e: Exception) {
                analyticsManager.reportCrash(e, "Application initialization")
            }
        }
    }
    
    private fun initializeAnalytics() {
        applicationScope.launch {
            try {
                // Track app start
                analyticsManager.trackEvent("app_started", mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "version" to getAppVersion()
                ))
                
                // Track device info
                val deviceInfo = getDeviceInfo()
                analyticsManager.trackEvent("device_info", deviceInfo)
                
            } catch (e: Exception) {
                // Handle analytics initialization error
            }
        }
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getDeviceInfo(): Map<String, Any> {
        return mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "version" to android.os.Build.VERSION.RELEASE,
            "sdk" to android.os.Build.VERSION.SDK_INT,
            "screen_width" to resources.displayMetrics.widthPixels,
            "screen_height" to resources.displayMetrics.heightPixels,
            "screen_density" to resources.displayMetrics.density
        )
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Clean up resources
        applicationScope.coroutineContext.cancel()
        
        // Destroy voice control
        voiceControl.destroy()
        
        // Track app termination
        analyticsManager.trackEvent("app_terminated", mapOf(
            "timestamp" to System.currentTimeMillis()
        ))
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        
        // Track low memory event
        analyticsManager.trackEvent("low_memory", mapOf(
            "timestamp" to System.currentTimeMillis()
        ))
        
        // Clean up resources
        System.gc()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        // Track memory trim event
        analyticsManager.trackEvent("trim_memory", mapOf(
            "level" to level,
            "timestamp" to System.currentTimeMillis()
        ))
        
        // Clean up resources based on level
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Clean up non-essential resources
                System.gc()
            }
            TRIM_MEMORY_UI_HIDDEN -> {
                // Clean up UI resources
                System.gc()
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // Clean up all non-essential resources
                System.gc()
            }
        }
    }
}