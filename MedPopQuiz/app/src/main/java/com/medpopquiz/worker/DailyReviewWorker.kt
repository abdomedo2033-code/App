package com.medpopquiz.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.*
import com.medpopquiz.ui.popup.QuizPopupActivity
import com.medpopquiz.utils.PreferenceManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyReviewWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        private const val TAG = "DailyReviewWorker"
        private const val WORK_NAME = "daily_review_work"

        fun schedule(context: Context) {
            val prefs = PreferenceManager(context)
            if (!prefs.isDailyReviewEnabled()) {
                cancel(context)
                return
            }

            val hour = prefs.getDailyReviewHour()
            val minute = prefs.getDailyReviewMinute()
            val reviewCount = prefs.getDailyReviewCount()

            // Calculate initial delay
            val currentTime = Calendar.getInstance()
            val scheduledTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (scheduledTime.before(currentTime)) {
                scheduledTime.add(Calendar.DAY_OF_YEAR, 1)
            }

            val initialDelay = scheduledTime.timeInMillis - currentTime.timeInMillis

            // Create work request
            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReviewWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWorkRequest
            )

            Log.d(TAG, "Daily review scheduled for $hour:$minute with $reviewCount reviews")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Daily review cancelled")
        }
    }

    override fun doWork(): Result {
        Log.d(TAG, "Daily review worker executing")
        
        val prefs = PreferenceManager(applicationContext)
        val reviewCount = prefs.getDailyReviewCount()

        // Show multiple popups with delays
        for (i in 0 until reviewCount) {
            val delay = i * 2 * 60 * 1000L // 2 minutes between each popup
            
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.postDelayed({
                showQuizPopup()
            }, delay)
        }

        return Result.success()
    }

    private fun showQuizPopup() {
        try {
            val intent = Intent(applicationContext, QuizPopupActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            applicationContext.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing daily review popup", e)
        }
    }
}

// Alternative: Alarm-based scheduling for more precise timing
class DailyReviewAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "DailyReviewAlarm"
        private const val ACTION_DAILY_REVIEW = "com.medpopquiz.DAILY_REVIEW"
        
        fun schedule(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyReviewAlarmReceiver::class.java).apply {
                action = ACTION_DAILY_REVIEW
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
        
        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyReviewAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DAILY_REVIEW) {
            Log.d(TAG, "Daily review alarm triggered")
            
            val prefs = PreferenceManager(context)
            val reviewCount = prefs.getDailyReviewCount()
            
            // Show quiz popup
            val popupIntent = Intent(context, QuizPopupActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(popupIntent)
            
            // Reschedule for next day
            schedule(context, prefs.getDailyReviewHour(), prefs.getDailyReviewMinute())
        }
    }
}
