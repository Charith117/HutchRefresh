package com.hutchrefresh.app

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class RefreshService : Service() {

    companion object {
        const val CHANNEL_ID = "HutchRefreshChannel"
        const val NOTIFICATION_ID = 1
        const val INTERVAL_MS = 10 * 1000L // 10 seconds minimum
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val broadcastIntent = Intent(this, RefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + INTERVAL_MS,
            INTERVAL_MS,
            pendingIntent
        )

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val broadcastIntent = Intent(this, RefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hutch Auto Refresh",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Hutch app refreshing in the background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hutch Auto Refresh")
            .setContentText("Running — refreshing every 5 minutes")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
