package com.medpopquiz

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.medpopquiz.data.AppDatabase

class MedPopQuizApp : Application(), Configuration.Provider {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MedPop Quiz Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medical quiz popups"
                setBypassDnd(true)
                lockscreenVisibility = NotificationManager.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

    companion object {
        lateinit var instance: MedPopQuizApp
            private set
        const val CHANNEL_ID = "medpop_quiz_channel"
    }
}
