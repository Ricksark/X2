package com.example.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.MainActivity
import com.example.receiver.ScreenOffActionReceiver
import com.example.util.LockManager
import com.example.util.LockResult

class LockScreenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        val isAssist = action == Intent.ACTION_ASSIST
        val isPowerMenu = action == ScreenOffActionReceiver.ACTION_POWER_MENU

        val triggerSource = when {
            isAssist -> "Digital Assistant"
            isPowerMenu -> "Power Menu Shortcut"
            else -> "Launcher Shortcut / API"
        }

        val result = if (isPowerMenu) {
            LockManager.executePowerMenu(this, triggerSource)
        } else {
            LockManager.executeLock(this, triggerSource)
        }

        when (result) {
            is LockResult.Success -> {
                // Done! Finish instantly
                finishAndRemoveTask()
            }
            is LockResult.PermissionRequired -> {
                Toast.makeText(
                    this,
                    "Screen Off: Please grant Accessibility or Device Admin permission in the app first.",
                    Toast.LENGTH_LONG
                ).show()
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(mainIntent)
                finish()
            }
            is LockResult.Error -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
