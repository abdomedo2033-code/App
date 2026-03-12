package com.medpopquiz.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.medpopquiz.MedPopQuizApp
import com.medpopquiz.R
import com.medpopquiz.receiver.ScreenUnlockReceiver
import com.medpopquiz.ui.main.MainActivity

class PopupMonitorService : Service() {

    companion object {
        private const val TAG = "PopupMonitorService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "medpop_quiz_channel"
    }

    private var screenUnlockReceiver: ScreenUnlockReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        unregisterScreenReceiver()
    }

    private fun registerScreenReceiver() {
        if (screenUnlockReceiver == null) {
            screenUnlockReceiver = ScreenUnlockReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenUnlockReceiver, filter, RECEIVER_EXPORTED)
            } else {
                registerReceiver(screenUnlockReceiver, filter)
            }
            
            Log.d(TAG, "Screen receiver registered")
        }
    }

    private fun unregisterScreenReceiver() {
        screenUnlockReceiver?.let {
            try {
                unregisterReceiver(it)
                Log.d(TAG, "Screen receiver unregistered")
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Receiver not registered", e)
            }
            screenUnlockReceiver = null
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MedPop Quiz Active")
            .setContentText("Quiz popups enabled on unlock")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
