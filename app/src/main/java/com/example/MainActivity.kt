package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.receiver.ScreenOffActionReceiver
import com.example.ui.MainViewModel
import com.example.ui.components.OnboardingDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ApiDocsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.PinScreenMode
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ScreenOffTheme
import com.example.util.BiometricHelper

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    ANALYTICS("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics, "tab_analytics"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings"),
    API_DOCS("API", Icons.Filled.Api, Icons.Outlined.Api, "tab_api")
}

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setPersistentNotification(this, true)
            Toast.makeText(this, "Quick notification controls enabled", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setPersistentNotification(this, false)
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val totalLocks by viewModel.totalLocks.collectAsStateWithLifecycle()
            val totalPowerMenus by viewModel.totalPowerMenus.collectAsStateWithLifecycle()
            val recentEvents by viewModel.recentEvents.collectAsStateWithLifecycle()
            val countsBySource by viewModel.countsBySource.collectAsStateWithLifecycle()
            val countsByMethod by viewModel.countsByMethod.collectAsStateWithLifecycle()

            val isDark = when (uiState.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            ScreenOffTheme(darkTheme = isDark) {
                // If App Lock is enabled and current session is locked, show Custom PIN Entry Screen
                if (uiState.isAppLockEnabled && !uiState.isAppUnlocked) {
                    PinLockScreen(
                        mode = PinScreenMode.UNLOCK_APP,
                        pinManager = ScreenOffApp.instance.pinSecurityManager,
                        isBiometricAvailable = uiState.biometricStatus.isAvailable,
                        onBiometricClick = {
                            BiometricHelper.authenticate(
                                activity = this@MainActivity,
                                title = "Screen Off Unlock",
                                subtitle = "Touch fingerprint sensor or enter PIN",
                                onSuccess = {
                                    viewModel.unlockAppSession()
                                    Toast.makeText(this@MainActivity, "Unlocked with Biometrics ✓", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    // Let user enter PIN fallback
                                    Toast.makeText(this@MainActivity, "Biometric: $err. Please enter PIN.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onSuccess = {
                            viewModel.unlockAppSession()
                        }
                    )
                } else {
                    val snackbarHostState = remember { SnackbarHostState() }
                    var currentTab by remember { mutableIntStateOf(0) }

                    LaunchedEffect(uiState.lastMessage) {
                        uiState.lastMessage?.let { msg ->
                            snackbarHostState.showSnackbar(msg)
                            viewModel.clearMessage()
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("bottom_nav_bar")
                            ) {
                                ScreenTab.values().forEachIndexed { index, tab ->
                                    val isSelected = currentTab == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = index },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title
                                            )
                                        },
                                        label = { Text(tab.title) },
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                0 -> HomeScreen(
                                    uiState = uiState,
                                    totalLocks = totalLocks,
                                    onTestLock = { viewModel.testLock(this@MainActivity) },
                                    onTestPowerMenu = { viewModel.testPowerMenu(this@MainActivity) },
                                    onEnableAccessibility = { openAccessibilitySettings() },
                                    onFixRestrictedSettings = { openAppInfoSettings() },
                                    onVerifyFingerprint = {
                                        BiometricHelper.authenticate(
                                            activity = this@MainActivity,
                                            onSuccess = {
                                                Toast.makeText(this@MainActivity, "Biometric authentication successful! 🎉", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(this@MainActivity, "Biometric error: $err", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    onNavigateToSettings = { currentTab = 2 }
                                )
                                1 -> AnalyticsScreen(
                                    totalLocks = totalLocks,
                                    totalPowerMenus = totalPowerMenus,
                                    recentEvents = recentEvents,
                                    countsBySource = countsBySource,
                                    countsByMethod = countsByMethod,
                                    onClearAnalytics = { viewModel.clearAnalytics() }
                                )
                                2 -> SettingsScreen(
                                    uiState = uiState,
                                    onSetVibration = { vib -> viewModel.setVibration(vib) },
                                    onSetPersistentNotification = { enable ->
                                        handlePersistentNotificationToggle(enable)
                                    },
                                    onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                    onOpenAccessibilitySettings = { openAccessibilitySettings() },
                                    onOpenDefaultAssistantSettings = { openDefaultAssistantSettings() },
                                    onOpenPinSetup = { mode -> viewModel.openPinSetup(mode) },
                                    onToggleBiometricUnlock = { enable -> viewModel.setBiometricUnlockEnabled(enable) }
                                )
                                3 -> ApiDocsScreen(
                                    onTriggerTestApiLock = {
                                        val intent = Intent(ScreenOffActionReceiver.ACTION_LOCK).apply {
                                            putExtra("EXTRA_SOURCE", "In-App API Tester")
                                        }
                                        sendBroadcast(intent)
                                    },
                                    onTriggerTestApiPower = {
                                        val intent = Intent(ScreenOffActionReceiver.ACTION_POWER_MENU).apply {
                                            putExtra("EXTRA_SOURCE", "In-App API Tester")
                                        }
                                        sendBroadcast(intent)
                                    }
                                )
                            }

                            // PIN Setup / Change Dialog
                            uiState.pinSetupMode?.let { pinDialogMode ->
                                Dialog(
                                    onDismissRequest = { viewModel.closePinSetup() },
                                    properties = DialogProperties(usePlatformDefaultWidth = false)
                                ) {
                                    PinLockScreen(
                                        mode = pinDialogMode,
                                        pinManager = ScreenOffApp.instance.pinSecurityManager,
                                        isBiometricAvailable = uiState.biometricStatus.isAvailable,
                                        onSuccess = {
                                            viewModel.closePinSetup()
                                            val successMsg = if (pinDialogMode == PinScreenMode.VERIFY_TO_DISABLE)
                                                "App Lock turned off"
                                            else
                                                "Security PIN configured successfully! ✓"
                                            Toast.makeText(this@MainActivity, successMsg, Toast.LENGTH_SHORT).show()
                                        },
                                        onCancel = { viewModel.closePinSetup() }
                                    )
                                }
                            }

                            // First run Onboarding dialog
                            if (!uiState.isOnboardingCompleted) {
                                OnboardingDialog(
                                    onDismiss = { viewModel.completeOnboarding() },
                                    onEnableAccessibility = {
                                        viewModel.completeOnboarding()
                                        openAccessibilitySettings()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStatus(this)
    }

    override fun onStop() {
        super.onStop()
        // Re-lock app session when leaving app if lock is enabled
        viewModel.lockAppSession()
    }

    private fun handlePersistentNotificationToggle(enable: Boolean) {
        if (enable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    viewModel.setPersistentNotification(this, true)
                } else {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                viewModel.setPersistentNotification(this, true)
            }
        } else {
            viewModel.setPersistentNotification(this, false)
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Locate 'Screen Off' and turn the switch ON", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not open Accessibility Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppInfoSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
            }
            startActivity(intent)
            Toast.makeText(this, "Tap 3 dots (⋮) top-right -> 'Allow restricted settings'", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not open App Info settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDefaultAssistantSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
            } else {
                startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
            }
            Toast.makeText(this, "Select 'Screen Off' as default digital assistant app", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
            } catch (_: Exception) {
                Toast.makeText(this, "Could not open Assistant Settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
