package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.ScreenOffApp
import com.example.data.ScreenLockEntity
import com.example.service.LockAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class LockResult {
    data class Success(val methodUsed: String) : LockResult()
    data class PermissionRequired(val missingMethod: String) : LockResult()
    data class Error(val message: String) : LockResult()
}

object LockManager {

    fun executeLock(
        context: Context,
        triggerSource: String = "App Action"
    ): LockResult {
        val accessibilityService = LockAccessibilityService.instance
        return if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val success = accessibilityService.lockScreen()
            if (success) {
                triggerHaptic(context)
                logEvent(triggerSource, "Accessibility API", "Lock Screen", true)
                LockResult.Success("Accessibility API")
            } else {
                logEvent(triggerSource, "Accessibility API", "Lock Screen", false)
                LockResult.Error("Failed to trigger screen lock")
            }
        } else {
            LockResult.PermissionRequired("Accessibility Service (Required for Fingerprint-Safe Lock)")
        }
    }

    fun executePowerMenu(
        context: Context,
        triggerSource: String = "App Action"
    ): LockResult {
        val accessibilityService = LockAccessibilityService.instance
        return if (accessibilityService != null) {
            val success = accessibilityService.openPowerMenu()
            if (success) {
                triggerHaptic(context)
                logEvent(triggerSource, "Accessibility API", "Power Menu", true)
                LockResult.Success("Accessibility API")
            } else {
                LockResult.Error("Could not show power dialog")
            }
        } else {
            LockResult.PermissionRequired("Accessibility Service (Required for Power Menu)")
        }
    }

    private fun triggerHaptic(context: Context) {
        val app = ScreenOffApp.instance
        if (!app.preferencesManager.isVibrationEnabled()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration errors on devices without vibrator hardware
        }
    }

    private fun logEvent(
        source: String,
        method: String,
        action: String,
        success: Boolean
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ScreenOffApp.instance.database.screenLockDao().insertEvent(
                    ScreenLockEntity(
                        triggerSource = source,
                        method = method,
                        actionType = action,
                        isSuccess = success
                    )
                )
            } catch (_: Exception) {}
        }
    }
}
