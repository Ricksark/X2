package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("screen_off_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LOCK_METHOD = "pref_lock_method"
        const val KEY_VIBRATION = "pref_vibration"
        const val KEY_PERSISTENT_NOTIFICATION = "pref_persistent_notification"
        const val KEY_BIOMETRIC_PROTECTION = "pref_biometric_protection"
        const val KEY_THEME_MODE = "pref_theme_mode" // "SYSTEM", "DARK", "LIGHT"
        const val KEY_ONBOARDING_COMPLETED = "pref_onboarding_completed"

        const val METHOD_ACCESSIBILITY = "ACCESSIBILITY"
        const val METHOD_DEVICE_ADMIN = "DEVICE_ADMIN"
    }

    private val _lockMethod = MutableStateFlow(
        prefs.getString(KEY_LOCK_METHOD, METHOD_ACCESSIBILITY) ?: METHOD_ACCESSIBILITY
    )
    val lockMethod: StateFlow<String> = _lockMethod.asStateFlow()

    private val _vibrationFeedback = MutableStateFlow(
        prefs.getBoolean(KEY_VIBRATION, true)
    )
    val vibrationFeedback: StateFlow<Boolean> = _vibrationFeedback.asStateFlow()

    private val _persistentNotification = MutableStateFlow(
        prefs.getBoolean(KEY_PERSISTENT_NOTIFICATION, false)
    )
    val persistentNotification: StateFlow<Boolean> = _persistentNotification.asStateFlow()

    private val _biometricProtection = MutableStateFlow(
        prefs.getBoolean(KEY_BIOMETRIC_PROTECTION, false)
    )
    val biometricProtection: StateFlow<Boolean> = _biometricProtection.asStateFlow()

    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun setLockMethod(method: String) {
        prefs.edit().putString(KEY_LOCK_METHOD, method).apply()
        _lockMethod.value = method
    }

    fun setVibrationFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        _vibrationFeedback.value = enabled
    }

    fun setPersistentNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERSISTENT_NOTIFICATION, enabled).apply()
        _persistentNotification.value = enabled
    }

    fun setBiometricProtection(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_PROTECTION, enabled).apply()
        _biometricProtection.value = enabled
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    // Direct synchronous accessors
    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)
    fun getActiveLockMethod(): String = prefs.getString(KEY_LOCK_METHOD, METHOD_ACCESSIBILITY) ?: METHOD_ACCESSIBILITY
    fun isPersistentNotificationEnabled(): Boolean = prefs.getBoolean(KEY_PERSISTENT_NOTIFICATION, false)
    fun isBiometricProtectionEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_PROTECTION, false)
}
