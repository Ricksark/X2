package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.ScreenOffApp
import com.example.data.PreferencesManager
import com.example.data.ScreenLockEntity
import com.example.receiver.ScreenOffDeviceAdminReceiver
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
        val app = ScreenOffApp.instance
        val prefMethod = app.preferencesManager.getActiveLockMethod()
        var methodUsed = ""
        var success = false

        if (prefMethod == PreferencesManager.METHOD_ACCESSIBILITY) {
            val accessibilityService = LockAccessibilityService.instance
            if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                success = accessibilityService.lockScreen()
                methodUsed = "Accessibility API"
            } else if (ScreenOffDeviceAdminReceiver.isDeviceAdminActive(context)) {
                // Fallback to Device Admin if accessibility is not running
                success = ScreenOffDeviceAdminReceiver.lockNow(context)
                methodUsed = "Device Administrator (Fallback)"
            } else {
                return LockResult.PermissionRequired("Accessibility Service or Device Administrator")
            }
        } else {
            // User preferred Device Admin
            if (ScreenOffDeviceAdminReceiver.isDeviceAdminActive(context)) {
                success = ScreenOffDeviceAdminReceiver.lockNow(context)
                methodUsed = "Device Administrator"
            } else {
                val accessibilityService = LockAccessibilityService.instance
                if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    success = accessibilityService.lockScreen()
                    methodUsed = "Accessibility API (Fallback)"
                } else {
                    return LockResult.PermissionRequired("Device Administrator")
                }
            }
        }

        if (success) {
            triggerHaptic(context)
            logEvent(triggerSource, methodUsed, "Lock Screen", true)
            return LockResult.Success(methodUsed)
        } else {
            logEvent(triggerSource, methodUsed.ifEmpty { "None" }, "Lock Screen", false)
            return LockResult.Error("Failed to trigger screen lock")
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
            LockResult.PermissionRequired("Accessibility Service (Power Menu requires Accessibility)")
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
