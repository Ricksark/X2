package com.example.ui

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ScreenOffApp
import com.example.data.MethodCount
import com.example.data.PreferencesManager
import com.example.data.ScreenLockEntity
import com.example.data.SourceCount
import com.example.service.LockAccessibilityService
import com.example.ui.screens.PinScreenMode
import com.example.util.BiometricHelper
import com.example.util.BiometricStatus
import com.example.util.LockManager
import com.example.util.LockResult
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val isAccessibilityEnabled: Boolean = false,
    val biometricStatus: BiometricStatus = BiometricStatus.UNKNOWN,
    val isAndroid9Plus: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
    val vibrationEnabled: Boolean = true,
    val persistentNotificationEnabled: Boolean = false,
    val themeMode: String = "SYSTEM",
    val isOnboardingCompleted: Boolean = false,
    val isPinSet: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val isBiometricUnlockEnabled: Boolean = true,
    val isAppUnlocked: Boolean = false,
    val pinSetupMode: PinScreenMode? = null,
    val lastMessage: String? = null
)

class MainViewModel : ViewModel() {

    private val app = ScreenOffApp.instance
    private val dao = app.database.screenLockDao()
    private val prefs = app.preferencesManager
    private val pinManager = app.pinSecurityManager

    private val _uiState = MutableStateFlow(
        MainUiState(
            isPinSet = pinManager.isPinSet(),
            isAppLockEnabled = pinManager.isAppLockEnabled(),
            isBiometricUnlockEnabled = pinManager.isBiometricUnlockEnabled(),
            isAppUnlocked = !pinManager.isAppLockEnabled() // Unlocked if lock is disabled
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val totalLocks: StateFlow<Int> = dao.getTotalLockCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPowerMenus: StateFlow<Int> = dao.getTotalPowerMenuCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentEvents: StateFlow<List<ScreenLockEntity>> = dao.getRecentEvents(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val countsBySource: StateFlow<List<SourceCount>> = dao.getCountsBySource()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val countsByMethod: StateFlow<List<MethodCount>> = dao.getCountsByMethod()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            prefs.vibrationFeedback.collect { vib ->
                _uiState.value = _uiState.value.copy(vibrationEnabled = vib)
            }
        }
        viewModelScope.launch {
            prefs.persistentNotification.collect { notif ->
                _uiState.value = _uiState.value.copy(persistentNotificationEnabled = notif)
            }
        }
        viewModelScope.launch {
            prefs.themeMode.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
        viewModelScope.launch {
            prefs.onboardingCompleted.collect { comp ->
                _uiState.value = _uiState.value.copy(isOnboardingCompleted = comp)
            }
        }
        refreshSecurityState()
    }

    fun refreshStatus(context: Context) {
        val access = LockAccessibilityService.isAccessibilityEnabled(context)
        val bioStatus = BiometricHelper.getBiometricStatus(context)
        refreshSecurityState()

        _uiState.value = _uiState.value.copy(
            isAccessibilityEnabled = access,
            biometricStatus = bioStatus
        )
    }

    fun refreshSecurityState() {
        val pinSet = pinManager.isPinSet()
        val appLockEnabled = pinManager.isAppLockEnabled()
        val bioEnabled = pinManager.isBiometricUnlockEnabled()

        _uiState.value = _uiState.value.copy(
            isPinSet = pinSet,
            isAppLockEnabled = appLockEnabled,
            isBiometricUnlockEnabled = bioEnabled,
            isAppUnlocked = if (!appLockEnabled) true else _uiState.value.isAppUnlocked
        )
    }

    fun unlockAppSession() {
        _uiState.value = _uiState.value.copy(isAppUnlocked = true)
    }

    fun lockAppSession() {
        if (pinManager.isAppLockEnabled()) {
            _uiState.value = _uiState.value.copy(isAppUnlocked = false)
        }
    }

    fun openPinSetup(mode: PinScreenMode) {
        _uiState.value = _uiState.value.copy(pinSetupMode = mode)
    }

    fun closePinSetup() {
        _uiState.value = _uiState.value.copy(pinSetupMode = null)
        refreshSecurityState()
    }

    fun setBiometricUnlockEnabled(enabled: Boolean) {
        pinManager.setBiometricUnlockEnabled(enabled)
        refreshSecurityState()
    }

    fun setVibration(enabled: Boolean) {
        prefs.setVibrationFeedback(enabled)
    }

    fun setPersistentNotification(context: Context, enabled: Boolean) {
        prefs.setPersistentNotification(enabled)
        NotificationHelper.updatePersistentNotification(context, enabled)
    }

    fun setThemeMode(mode: String) {
        prefs.setThemeMode(mode)
    }

    fun completeOnboarding() {
        prefs.setOnboardingCompleted(true)
    }

    fun testLock(context: Context) {
        val result = LockManager.executeLock(context, triggerSource = "In-App Test Button")
        when (result) {
            is LockResult.Success -> {
                _uiState.value = _uiState.value.copy(lastMessage = "Screen locked successfully (${result.methodUsed})")
            }
            is LockResult.PermissionRequired -> {
                _uiState.value = _uiState.value.copy(lastMessage = "Permission Required: ${result.missingMethod}")
            }
            is LockResult.Error -> {
                _uiState.value = _uiState.value.copy(lastMessage = result.message)
            }
        }
    }

    fun testPowerMenu(context: Context) {
        val result = LockManager.executePowerMenu(context, triggerSource = "In-App Test Button")
        when (result) {
            is LockResult.Success -> {
                _uiState.value = _uiState.value.copy(lastMessage = "Power menu opened successfully")
            }
            is LockResult.PermissionRequired -> {
                _uiState.value = _uiState.value.copy(lastMessage = "Permission Required: ${result.missingMethod}")
            }
            is LockResult.Error -> {
                _uiState.value = _uiState.value.copy(lastMessage = result.message)
            }
        }
    }

    fun clearAnalytics() {
        viewModelScope.launch {
            dao.clearAll()
            _uiState.value = _uiState.value.copy(lastMessage = "Usage analytics cleared")
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(lastMessage = null)
    }
}
