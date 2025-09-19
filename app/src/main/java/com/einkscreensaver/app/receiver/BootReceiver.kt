package com.einkscreensaver.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.einkscreensaver.app.service.WeatherUpdateService
import com.einkscreensaver.app.service.ImageUpdateService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                startServices(context)
            }
        }
    }
    
    private fun startServices(context: Context) {
        val weatherIntent = Intent(context, WeatherUpdateService::class.java)
        context.startService(weatherIntent)
        
        val imageIntent = Intent(context, ImageUpdateService::class.java)
        context.startService(imageIntent)
    }
}