package com.example.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ScreenOffApp
import com.example.receiver.ScreenOffActionReceiver

object NotificationHelper {

    private const val NOTIFICATION_ID_QUICK = 1001
    private const val NOTIFICATION_ID_ALERT = 1002

    fun updatePersistentNotification(context: Context, isEnabled: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (!isEnabled) {
            manager.cancel(NOTIFICATION_ID_QUICK)
            return
        }

        // Tap content -> open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context,
            201,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Lock Screen
        val lockIntent = Intent(context, ScreenOffActionReceiver::class.java).apply {
            action = ScreenOffActionReceiver.ACTION_LOCK
            putExtra("EXTRA_SOURCE", "Quick Notification")
        }
        val lockPending = PendingIntent.getBroadcast(
            context,
            202,
            lockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Power Menu
        val powerIntent = Intent(context, ScreenOffActionReceiver::class.java).apply {
            action = ScreenOffActionReceiver.ACTION_POWER_MENU
            putExtra("EXTRA_SOURCE", "Quick Notification")
        }
        val powerPending = PendingIntent.getBroadcast(
            context,
            203,
            powerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ScreenOffApp.CHANNEL_ID_QUICK_ACTIONS)
            .setSmallIcon(R.drawable.ic_widget_lock)
            .setContentTitle("Screen Off Controls")
            .setContentText("Tap buttons below to lock screen or access power menu")
            .setContentIntent(contentPending)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .addAction(R.drawable.ic_widget_lock, "Lock Screen", lockPending)
            .addAction(R.drawable.ic_widget_power, "Power Menu", powerPending)
            .build()

        manager.notify(NOTIFICATION_ID_QUICK, notification)
    }

    fun showUsageMilestoneNotification(context: Context, lockCount: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPending = PendingIntent.getActivity(
            context,
            301,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ScreenOffApp.CHANNEL_ID_QUICK_ACTIONS)
            .setSmallIcon(R.drawable.ic_widget_lock)
            .setContentTitle("Hardware Saved! 🎉")
            .setContentText("You have spared your physical power button $lockCount times!")
            .setContentIntent(contentPending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(NOTIFICATION_ID_ALERT, notification)
    }
}
