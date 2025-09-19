package com.einkscreensaver.app.analytics

import android.content.Context
import android.os.Bundle
import com.einkscreensaver.app.data.model.ScreenSaverSettings
import kotlinx.coroutines.*
import java.util.*

class AnalyticsManager(private val context: Context) {
    
    private val eventTracker = EventTracker()
    private val performanceMonitor = PerformanceMonitor()
    private val usageTracker = UsageTracker()
    private val crashReporter = CrashReporter()
    private val metricsCollector = MetricsCollector()
    
    suspend fun initialize() {
        eventTracker.initialize()
        performanceMonitor.initialize()
        usageTracker.initialize()
        crashReporter.initialize()
        metricsCollector.initialize()
    }
    
    fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        eventTracker.trackEvent(eventName, parameters)
    }
    
    fun trackScreenView(screenName: String, parameters: Map<String, Any> = emptyMap()) {
        eventTracker.trackScreenView(screenName, parameters)
    }
    
    fun trackUserAction(action: String, parameters: Map<String, Any> = emptyMap()) {
        eventTracker.trackUserAction(action, parameters)
    }
    
    fun trackPerformance(metric: String, value: Long, parameters: Map<String, Any> = emptyMap()) {
        performanceMonitor.trackMetric(metric, value, parameters)
    }
    
    fun trackUsage(feature: String, duration: Long, parameters: Map<String, Any> = emptyMap()) {
        usageTracker.trackUsage(feature, duration, parameters)
    }
    
    fun reportCrash(exception: Throwable, context: String = "") {
        crashReporter.reportCrash(exception, context)
    }
    
    fun getAnalyticsData(): AnalyticsData {
        return AnalyticsData(
            events = eventTracker.getEvents(),
            performanceMetrics = performanceMonitor.getMetrics(),
            usageStats = usageTracker.getStats(),
            crashReports = crashReporter.getReports()
        )
    }
    
    fun exportData(): String {
        return metricsCollector.exportData()
    }
}

class EventTracker {
    private val events = mutableListOf<AnalyticsEvent>()
    private val sessionId = UUID.randomUUID().toString()
    private var sessionStartTime = System.currentTimeMillis()
    
    suspend fun initialize() {
        loadEvents()
    }
    
    fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        val event = AnalyticsEvent(
            id = UUID.randomUUID().toString(),
            name = eventName,
            parameters = parameters,
            timestamp = System.currentTimeMillis(),
            sessionId = sessionId
        )
        
        events.add(event)
        saveEvent(event)
    }
    
    fun trackScreenView(screenName: String, parameters: Map<String, Any> = emptyMap()) {
        val screenParams = parameters.toMutableMap()
        screenParams["screen_name"] = screenName
        
        trackEvent("screen_view", screenParams)
    }
    
    fun trackUserAction(action: String, parameters: Map<String, Any> = emptyMap()) {
        val actionParams = parameters.toMutableMap()
        actionParams["action"] = action
        
        trackEvent("user_action", actionParams)
    }
    
    fun getEvents(): List<AnalyticsEvent> {
        return events.toList()
    }
    
    fun getEventsByType(type: String): List<AnalyticsEvent> {
        return events.filter { it.name == type }
    }
    
    fun getSessionDuration(): Long {
        return System.currentTimeMillis() - sessionStartTime
    }
    
    private suspend fun loadEvents() {
        // Load from database or preferences
    }
    
    private fun saveEvent(event: AnalyticsEvent) {
        // Save to database or preferences
    }
}

class PerformanceMonitor {
    private val metrics = mutableMapOf<String, MutableList<PerformanceMetric>>()
    private val thresholds = mutableMapOf<String, Long>()
    
    suspend fun initialize() {
        loadMetrics()
        initializeThresholds()
    }
    
    fun trackMetric(metricName: String, value: Long, parameters: Map<String, Any> = emptyMap()) {
        val metric = PerformanceMetric(
            name = metricName,
            value = value,
            parameters = parameters,
            timestamp = System.currentTimeMillis()
        )
        
        metrics.getOrPut(metricName) { mutableListOf() }.add(metric)
        
        // Check if metric exceeds threshold
        val threshold = thresholds[metricName]
        if (threshold != null && value > threshold) {
            Log.w("PerformanceMonitor", "Metric $metricName exceeded threshold: $value > $threshold")
        }
        
        // Keep only last 1000 metrics per type
        val metricList = metrics[metricName]!!
        if (metricList.size > 1000) {
            metricList.removeAt(0)
        }
    }
    
    fun getMetrics(): Map<String, List<PerformanceMetric>> {
        return metrics.mapValues { it.value.toList() }
    }
    
    fun getAverageMetric(metricName: String): Double {
        val metricList = metrics[metricName] ?: return 0.0
        return metricList.map { it.value }.average()
    }
    
    fun getMaxMetric(metricName: String): Long {
        val metricList = metrics[metricName] ?: return 0L
        return metricList.maxOfOrNull { it.value } ?: 0L
    }
    
    fun getMinMetric(metricName: String): Long {
        val metricList = metrics[metricName] ?: return 0L
        return metricList.minOfOrNull { it.value } ?: 0L
    }
    
    fun setThreshold(metricName: String, threshold: Long) {
        thresholds[metricName] = threshold
    }
    
    private suspend fun loadMetrics() {
        // Load from database or preferences
    }
    
    private fun initializeThresholds() {
        thresholds["screen_refresh_time"] = 1000L // 1 second
        thresholds["memory_usage"] = 100 * 1024 * 1024L // 100 MB
        thresholds["cpu_usage"] = 80L // 80%
        thresholds["battery_drain"] = 5L // 5% per hour
    }
}

class UsageTracker {
    private val usageStats = mutableMapOf<String, UsageStat>()
    private val sessionStats = mutableListOf<SessionStat>()
    
    suspend fun initialize() {
        loadUsageStats()
    }
    
    fun trackUsage(feature: String, duration: Long, parameters: Map<String, Any> = emptyMap()) {
        val stat = usageStats.getOrPut(feature) {
            UsageStat(
                feature = feature,
                totalUsage = 0L,
                usageCount = 0,
                averageUsage = 0.0,
                lastUsed = 0L
            )
        }
        
        stat.totalUsage += duration
        stat.usageCount++
        stat.averageUsage = stat.totalUsage.toDouble() / stat.usageCount
        stat.lastUsed = System.currentTimeMillis()
        
        // Track session
        val sessionStat = SessionStat(
            feature = feature,
            duration = duration,
            timestamp = System.currentTimeMillis(),
            parameters = parameters
        )
        
        sessionStats.add(sessionStat)
        
        // Keep only last 1000 sessions
        if (sessionStats.size > 1000) {
            sessionStats.removeAt(0)
        }
    }
    
    fun getStats(): Map<String, UsageStat> {
        return usageStats.toMap()
    }
    
    fun getSessionStats(): List<SessionStat> {
        return sessionStats.toList()
    }
    
    fun getMostUsedFeatures(): List<UsageStat> {
        return usageStats.values.sortedByDescending { it.totalUsage }
    }
    
    fun getUsageTrend(feature: String, days: Int = 7): List<DailyUsage> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val recentSessions = sessionStats.filter { 
            it.feature == feature && it.timestamp >= cutoffTime 
        }
        
        val dailyUsage = mutableMapOf<String, Long>()
        recentSessions.forEach { session ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(java.util.Date(session.timestamp))
            dailyUsage[date] = (dailyUsage[date] ?: 0L) + session.duration
        }
        
        return dailyUsage.map { (date, usage) ->
            DailyUsage(date, usage)
        }.sortedBy { it.date }
    }
    
    private suspend fun loadUsageStats() {
        // Load from database or preferences
    }
}

class CrashReporter {
    private val crashReports = mutableListOf<CrashReport>()
    
    suspend fun initialize() {
        loadCrashReports()
    }
    
    fun reportCrash(exception: Throwable, context: String = "") {
        val crashReport = CrashReport(
            id = UUID.randomUUID().toString(),
            exception = exception,
            context = context,
            timestamp = System.currentTimeMillis(),
            deviceInfo = getDeviceInfo(),
            appVersion = getAppVersion()
        )
        
        crashReports.add(crashReport)
        saveCrashReport(crashReport)
        
        // Send to crash reporting service
        sendCrashReport(crashReport)
    }
    
    fun getReports(): List<CrashReport> {
        return crashReports.toList()
    }
    
    fun getCrashCount(): Int {
        return crashReports.size
    }
    
    fun getCrashRate(): Double {
        val totalSessions = getTotalSessions()
        return if (totalSessions > 0) {
            crashReports.size.toDouble() / totalSessions
        } else 0.0
    }
    
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "version" to android.os.Build.VERSION.RELEASE,
            "sdk" to android.os.Build.VERSION.SDK_INT.toString()
        )
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getTotalSessions(): Int {
        // This would be calculated from session data
        return 100
    }
    
    private suspend fun loadCrashReports() {
        // Load from database or preferences
    }
    
    private fun saveCrashReport(crashReport: CrashReport) {
        // Save to database or preferences
    }
    
    private fun sendCrashReport(crashReport: CrashReport) {
        // Send to crash reporting service (Firebase Crashlytics, etc.)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate sending crash report
                delay(1000)
                Log.d("CrashReporter", "Crash report sent: ${crashReport.id}")
            } catch (e: Exception) {
                Log.e("CrashReporter", "Failed to send crash report", e)
            }
        }
    }
}

class MetricsCollector {
    private val collectedMetrics = mutableMapOf<String, Any>()
    
    suspend fun initialize() {
        loadMetrics()
    }
    
    fun collectMetric(key: String, value: Any) {
        collectedMetrics[key] = value
    }
    
    fun getMetric(key: String): Any? {
        return collectedMetrics[key]
    }
    
    fun getAllMetrics(): Map<String, Any> {
        return collectedMetrics.toMap()
    }
    
    fun exportData(): String {
        val exportData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "metrics" to collectedMetrics,
            "device_info" to getDeviceInfo(),
            "app_info" to getAppInfo()
        )
        
        return com.google.gson.Gson().toJson(exportData)
    }
    
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "version" to android.os.Build.VERSION.RELEASE,
            "sdk" to android.os.Build.VERSION.SDK_INT.toString(),
            "screen_density" to context.resources.displayMetrics.density.toString(),
            "screen_size" to "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}"
        )
    }
    
    private fun getAppInfo(): Map<String, String> {
        return mapOf(
            "package_name" to context.packageName,
            "version_name" to getAppVersion(),
            "version_code" to getAppVersionCode().toString()
        )
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getAppVersionCode(): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.longVersionCode
        } catch (e: Exception) {
            0L
        }
    }
    
    private suspend fun loadMetrics() {
        // Load from database or preferences
    }
}

data class AnalyticsEvent(
    val id: String,
    val name: String,
    val parameters: Map<String, Any>,
    val timestamp: Long,
    val sessionId: String
)

data class PerformanceMetric(
    val name: String,
    val value: Long,
    val parameters: Map<String, Any>,
    val timestamp: Long
)

data class UsageStat(
    val feature: String,
    var totalUsage: Long,
    var usageCount: Int,
    var averageUsage: Double,
    var lastUsed: Long
)

data class SessionStat(
    val feature: String,
    val duration: Long,
    val timestamp: Long,
    val parameters: Map<String, Any>
)

data class DailyUsage(
    val date: String,
    val usage: Long
)

data class CrashReport(
    val id: String,
    val exception: Throwable,
    val context: String,
    val timestamp: Long,
    val deviceInfo: Map<String, String>,
    val appVersion: String
)

data class AnalyticsData(
    val events: List<AnalyticsEvent>,
    val performanceMetrics: Map<String, List<PerformanceMetric>>,
    val usageStats: Map<String, UsageStat>,
    val crashReports: List<CrashReport>
)