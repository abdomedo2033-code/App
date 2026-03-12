package com.medpopquiz.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.medpopquiz.ui.popup.QuizPopupActivity
import com.medpopquiz.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenUnlockReceiver"
        private const val MIN_INTERVAL_MS = 3000L // Minimum 3 seconds between popups
        private var lastPopupTime: Long = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "Screen ON received")
                handleScreenEvent(context)
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "User Present received")
                handleScreenEvent(context)
            }
        }
    }

    private fun handleScreenEvent(context: Context) {
        val prefs = PreferenceManager(context)
        
        // Check if popup on unlock is enabled
        if (!prefs.isPopupOnUnlockEnabled()) {
            return
        }

        // Check minimum interval to prevent rapid popups
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPopupTime < MIN_INTERVAL_MS) {
            return
        }

        // Check if screen is actually on and unlocked
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isInteractive) {
            return
        }

        // Update last popup time
        lastPopupTime = currentTime

        // Show popup activity
        CoroutineScope(Dispatchers.Main).launch {
            showPopupActivity(context)
        }
    }

    private fun showPopupActivity(context: Context) {
        try {
            val popupIntent = Intent(context, QuizPopupActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
            context.startActivity(popupIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting popup activity", e)
        }
    }
}
