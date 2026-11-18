package com.example.agriculture_management.platform.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.agriculture_management.MainActivity
import com.example.agriculture_management.R
import com.example.agriculture_management.data.local.UpgradedFarmDatabase
import java.util.concurrent.TimeUnit

class ReminderNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("green_farm_ui", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications", true)
        if (!notificationsEnabled) {
            return Result.success()
        }

        ensureNotificationChannel(applicationContext)

        val db = Room.databaseBuilder(
            applicationContext,
            UpgradedFarmDatabase::class.java,
            "nong_san_xanh.db"
        ).fallbackToDestructiveMigrationOnDowngrade(true).build()

        return try {
            val now = System.currentTimeMillis()
            val windowEnd = now + TimeUnit.HOURS.toMillis(6)
            val dueReminders = db.reminderDao().findDueReminders(windowEnd)

            for (reminder in dueReminders) {
                showReminderNotification(
                    context = applicationContext,
                    notificationId = reminder.id.hashCode(),
                    title = "Nhắc việc nông trại: ${reminder.title}",
                    content = "Công việc đã đến hạn thực hiện. Nhấn để mở Nông Sản Xanh."
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        } finally {
            db.close()
        }
    }

    private fun showReminderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "nong_san_xanh_reminders"
        private const val WORK_NAME = "nong_san_xanh_reminder_periodic_work"

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc việc canh tác",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Thông báo nhắc lịch tưới nước, bón phân và thu hoạch"
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        }

        fun schedulePeriodicChecks(context: Context) {
            ensureNotificationChannel(context)
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(
                6, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
