package com.einkscreensaver.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    
    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }
    
    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }
    
    fun spToPx(context: Context, sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }
    
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }
    
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }
    
    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }
    
    fun isTablet(context: Context): Boolean {
        val screenWidth = getScreenWidth(context)
        val screenHeight = getScreenHeight(context)
        val screenSize = maxOf(screenWidth, screenHeight)
        val density = getScreenDensity(context)
        val screenSizeInches = screenSize / (density * 160)
        return screenSizeInches >= 7.0
    }
    
    fun isLandscape(context: Context): Boolean {
        return getScreenWidth(context) > getScreenHeight(context)
    }
    
    fun getCurrentTimeString(format: String = "HH:mm"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date())
    }
    
    fun getCurrentDateString(format: String = "EEEE, MMMM dd, yyyy"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date())
    }
    
    fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$bytes B"
        }
    }
    
    fun getContrastColor(backgroundColor: Int): Int {
        val luminance = getLuminance(backgroundColor)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }
    
    fun getLuminance(color: Int): Double {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0
        
        val rsRGB = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gsRGB = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bsRGB = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)
        
        return 0.2126 * rsRGB + 0.7152 * gsRGB + 0.0722 * bsRGB
    }
    
    fun getContrastRatio(color1: Int, color2: Int): Double {
        val luminance1 = getLuminance(color1)
        val luminance2 = getLuminance(color2)
        val lighter = maxOf(luminance1, luminance2)
        val darker = minOf(luminance1, luminance2)
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    fun isColorAccessible(foreground: Int, background: Int): Boolean {
        val contrastRatio = getContrastRatio(foreground, background)
        return contrastRatio >= 4.5 // WCAG AA standard
    }
    
    fun adjustColorBrightness(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
    
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val r = (Color.red(color1) * (1 - ratio) + Color.red(color2) * ratio).toInt()
        val g = (Color.green(color1) * (1 - ratio) + Color.green(color2) * ratio).toInt()
        val b = (Color.blue(color1) * (1 - ratio) + Color.blue(color2) * ratio).toInt()
        return Color.rgb(r, g, b)
    }
    
    fun createBitmapFromText(
        text: String,
        textSize: Float,
        textColor: Int,
        backgroundColor: Int = Color.TRANSPARENT,
        typeface: Typeface? = null
    ): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = textColor
            this.typeface = typeface ?: Typeface.DEFAULT
            this.textAlign = Paint.Align.LEFT
        }
        
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        
        val width = bounds.width() + 20
        val height = bounds.height() + 20
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        if (backgroundColor != Color.TRANSPARENT) {
            canvas.drawColor(backgroundColor)
        }
        
        canvas.drawText(text, 10f, height - 10f, paint)
        
        return bitmap
    }
    
    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            Log.e("Utils", "Error saving bitmap", e)
            false
        }
    }
    
    fun loadBitmapFromFile(file: File): Bitmap? {
        return try {
            val inputStream = FileInputStream(file)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            Log.e("Utils", "Error loading bitmap", e)
            null
        }
    }
    
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            source.copyTo(destination, overwrite = true)
            true
        } catch (e: Exception) {
            Log.e("Utils", "Error copying file", e)
            false
        }
    }
    
    fun deleteFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            Log.e("Utils", "Error deleting file", e)
            false
        }
    }
    
    fun getFileExtension(file: File): String {
        val name = file.name
        val lastDot = name.lastIndexOf('.')
        return if (lastDot > 0) name.substring(lastDot + 1) else ""
    }
    
    fun isImageFile(file: File): Boolean {
        val extension = getFileExtension(file).lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }
    
    fun isVideoFile(file: File): Boolean {
        val extension = getFileExtension(file).lowercase()
        return extension in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm")
    }
    
    fun isAudioFile(file: File): Boolean {
        val extension = getFileExtension(file).lowercase()
        return extension in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a")
    }
    
    fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    
    fun generateRandomColor(): Int {
        val random = Random()
        return Color.rgb(
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
    }
    
    fun generateRandomColor(alpha: Int = 255): Int {
        val random = Random()
        return Color.argb(
            alpha,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )
    }
    
    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceIn(min, max)
    }
    
    fun clamp(value: Int, min: Int, max: Int): Int {
        return value.coerceIn(min, max)
    }
    
    fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }
    
    fun lerp(start: Int, end: Int, fraction: Float): Int {
        return (start + (end - start) * fraction).toInt()
    }
    
    fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
        val t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f)
        return t * t * (3f - 2f * t)
    }
    
    fun easeInOut(t: Float): Float {
        return if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
    }
    
    fun easeIn(t: Float): Float {
        return t * t
    }
    
    fun easeOut(t: Float): Float {
        return t * (2f - t)
    }
    
    fun bounce(t: Float): Float {
        return when {
            t < 1f / 2.75f -> 7.5625f * t * t
            t < 2f / 2.75f -> 7.5625f * (t - 1.5f / 2.75f) * (t - 1.5f / 2.75f) + 0.75f
            t < 2.5f / 2.75f -> 7.5625f * (t - 2.25f / 2.75f) * (t - 2.25f / 2.75f) + 0.9375f
            else -> 7.5625f * (t - 2.625f / 2.75f) * (t - 2.625f / 2.75f) + 0.984375f
        }
    }
    
    fun elastic(t: Float): Float {
        return if (t == 0f) 0f else if (t == 1f) 1f else {
            val p = 0.3f
            val s = p / 4f
            Math.pow(2.0, (-10 * t).toDouble()).toFloat() * Math.sin(((t - s) * (2 * Math.PI) / p).toDouble()).toFloat() + 1f
        }
    }
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
    
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.type == android.net.ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
    }
    
    fun isMobileDataConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.type == android.net.ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
    }
    
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        
        return when (networkInfo?.type) {
            android.net.ConnectivityManager.TYPE_WIFI -> "WiFi"
            android.net.ConnectivityManager.TYPE_MOBILE -> "Mobile"
            android.net.ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
            android.net.ConnectivityManager.TYPE_BLUETOOTH -> "Bluetooth"
            else -> "Unknown"
        }
    }
    
    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to android.os.Build.MANUFACTURER,
            "model" to android.os.Build.MODEL,
            "version" to android.os.Build.VERSION.RELEASE,
            "sdk" to android.os.Build.VERSION.SDK_INT.toString(),
            "board" to android.os.Build.BOARD,
            "bootloader" to android.os.Build.BOOTLOADER,
            "brand" to android.os.Build.BRAND,
            "device" to android.os.Build.DEVICE,
            "display" to android.os.Build.DISPLAY,
            "fingerprint" to android.os.Build.FINGERPRINT,
            "hardware" to android.os.Build.HARDWARE,
            "host" to android.os.Build.HOST,
            "id" to android.os.Build.ID,
            "product" to android.os.Build.PRODUCT,
            "tags" to android.os.Build.TAGS,
            "type" to android.os.Build.TYPE,
            "user" to android.os.Build.USER
        )
    }
    
    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.longVersionCode
        } catch (e: Exception) {
            0L
        }
    }
    
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getInstalledApps(context: Context): List<String> {
        return try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledPackages(0)
            packages.map { it.packageName }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
    }
    
    fun logInfo(tag: String, message: String) {
        Log.i(tag, message)
    }
    
    fun logDebug(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    fun logVerbose(tag: String, message: String) {
        Log.v(tag, message)
    }
}