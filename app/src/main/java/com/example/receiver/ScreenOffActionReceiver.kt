package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.util.LockManager
import com.example.util.LockResult

class ScreenOffActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val source = intent.getStringExtra("EXTRA_SOURCE") ?: "Notification / Third-Party"

        when (action) {
            ACTION_LOCK -> {
                val result = LockManager.executeLock(context, triggerSource = source)
                if (result is LockResult.PermissionRequired) {
                    Toast.makeText(context, "Screen Off permission required to lock", Toast.LENGTH_SHORT).show()
                }
            }
            ACTION_POWER_MENU -> {
                val result = LockManager.executePowerMenu(context, triggerSource = source)
                if (result is LockResult.PermissionRequired) {
                    Toast.makeText(context, "Accessibility permission required for Power Menu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val ACTION_LOCK = "com.aistudio.screenoff.ACTION_LOCK"
        const val ACTION_POWER_MENU = "com.aistudio.screenoff.ACTION_POWER_MENU"
    }
}
