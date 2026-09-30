package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.PreferencesManager

class ScreenOffApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        preferencesManager = PreferencesManager(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_QUICK_ACTIONS,
                "Quick Lock & Power Controls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Quick actions in the notification shade to lock screen or access power menu"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_QUICK_ACTIONS = "screen_off_quick_actions"
        lateinit var instance: ScreenOffApp
            private set
    }
}
