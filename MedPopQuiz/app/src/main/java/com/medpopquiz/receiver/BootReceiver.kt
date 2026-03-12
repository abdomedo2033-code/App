package com.medpopquiz.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.medpopquiz.service.PopupMonitorService
import com.medpopquiz.utils.PreferenceManager
import com.medpopquiz.worker.DailyReviewWorker

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed - starting MedPop Quiz services")
            
            val prefs = PreferenceManager(context)
            
            // Start the popup monitor service if enabled
            if (prefs.isPopupOnUnlockEnabled()) {
                startPopupMonitorService(context)
            }
            
            // Schedule daily review if enabled
            if (prefs.isDailyReviewEnabled()) {
                DailyReviewWorker.schedule(context)
            }
        }
    }

    private fun startPopupMonitorService(context: Context) {
        val serviceIntent = Intent(context, PopupMonitorService::class.java)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
