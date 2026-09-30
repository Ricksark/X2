package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.R
import com.example.util.LockManager

class ScreenOffWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            updateAppWidget(context, appWidgetManager, appWidgetId, options)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_LOCK -> {
                LockManager.executeLock(context, triggerSource = "Home Screen Widget")
            }
            ACTION_WIDGET_POWER -> {
                LockManager.executePowerMenu(context, triggerSource = "Home Screen Widget")
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_LOCK = "com.aistudio.screenoff.WIDGET_LOCK"
        const val ACTION_WIDGET_POWER = "com.aistudio.screenoff.WIDGET_POWER"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ScreenOffWidgetProvider::class.java))
            for (id in ids) {
                val options = manager.getAppWidgetOptions(id)
                updateAppWidget(context, manager, id, options)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            options: Bundle?
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_screen_off)

            // Determine size
            val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 60
            val showPowerButton = minWidth >= 120

            if (showPowerButton) {
                views.setViewVisibility(R.id.widget_btn_power, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_btn_power, View.GONE)
            }

            // Lock Intent
            val lockIntent = Intent(context, ScreenOffWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_LOCK
            }
            val lockPending = PendingIntent.getBroadcast(
                context,
                101,
                lockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_lock, lockPending)

            // Power Intent
            val powerIntent = Intent(context, ScreenOffWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_POWER
            }
            val powerPending = PendingIntent.getBroadcast(
                context,
                102,
                powerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_power, powerPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
