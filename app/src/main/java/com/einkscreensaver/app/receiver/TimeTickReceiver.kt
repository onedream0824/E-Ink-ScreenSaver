package com.einkscreensaver.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeTickReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                broadcastTimeUpdate(context)
            }
        }
    }
    
    private fun broadcastTimeUpdate(context: Context) {
        val intent = Intent("com.einkscreensaver.app.TIME_UPDATE")
        intent.putExtra("timestamp", System.currentTimeMillis())
        context.sendBroadcast(intent)
    }
}