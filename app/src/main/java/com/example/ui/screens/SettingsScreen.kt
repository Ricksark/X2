package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.receiver.ScreenOffDeviceAdminReceiver
import com.example.ui.MainUiState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldActive

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSetLockMethod: (String) -> Unit,
    onSetVibration: (Boolean) -> Unit,
    onSetPersistentNotification: (Boolean) -> Unit,
    onSetBiometricProtection: (Boolean) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenDeviceAdminSettings: () -> Unit,
    onOpenDefaultAssistantSettings: () -> Unit,
    onOpenAppInfoSettings: () -> Unit,
    onPromptBiometric: (onSuccess: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showUninstallHelperDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("settings_screen_content")
    ) {
        Text(
            text = "Settings & Controls",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Preferences, locking engine & device security",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Lock Method
        SettingsSectionHeader(title = "Locking Engine")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Method 1: Accessibility
                MethodOptionRow(
                    title = "Accessibility API (Recommended)",
                    badge = "Fingerprint Safe",
                    badgeColor = EmeraldActive,
                    description = "Locks screen while keeping biometric & fingerprint reader unlock fully functional without prompting for PIN.",
                    isSelected = uiState.lockMethod == PreferencesManager.METHOD_ACCESSIBILITY,
                    onClick = {
                        onSetLockMethod(PreferencesManager.METHOD_ACCESSIBILITY)
                        if (!uiState.isAccessibilityEnabled) {
                            onOpenAccessibilitySettings()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Method 2: Device Admin
                MethodOptionRow(
                    title = "Device Administrator",
                    badge = "Legacy Support",
                    badgeColor = AmberWarning,
                    description = "Uses DevicePolicyManager.lockNow(). Designed for older Android versions. (Note: May require PIN/password on unlock).",
                    isSelected = uiState.lockMethod == PreferencesManager.METHOD_DEVICE_ADMIN,
                    onClick = {
                        onSetLockMethod(PreferencesManager.METHOD_DEVICE_ADMIN)
                        if (!uiState.isDeviceAdminEnabled) {
                            onOpenDeviceAdminSettings()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Android 13+ Restricted Setting Notice & Quick Fix
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberWarning.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Android 13+ 'Restricted Setting' Fix",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "If Android shows 'Restricted setting: For your security, this setting is unavailable' when enabling Screen Off:\n" +
                            "1. Tap 'Open App Info' below\n" +
                            "2. Tap the three dots (⋮) in the top-right corner\n" +
                            "3. Tap 'Allow restricted settings'\n" +
                            "4. Verify your phone PIN or fingerprint\n" +
                            "5. Return and toggle Accessibility ON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onOpenAppInfoSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_settings_open_app_info")
                ) {
                    Text("Open App Info (Tap ⋮ for Restricted Settings)")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Appearance
        SettingsSectionHeader(title = "Appearance & Theme")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SYSTEM" to "System", "DARK" to "Dark", "LIGHT" to "Light").forEach { (key, label) ->
                        val isSelected = uiState.themeMode == key
                        OutlinedButton(
                            onClick = { onSetThemeMode(key) },
                            colors = if (isSelected) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_$key")
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: System Features & Shortcuts
        SettingsSectionHeader(title = "Shortcuts & Controls")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Persistent Notification Switch
                SettingsToggleRow(
                    title = "Quick Notification Controls",
                    description = "Show Lock and Power Menu buttons in notification pull-down shade",
                    icon = Icons.Default.Notifications,
                    checked = uiState.persistentNotificationEnabled,
                    onCheckedChange = onSetPersistentNotification,
                    testTag = "switch_persistent_notification"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vibration feedback switch
                SettingsToggleRow(
                    title = "Haptic Tactile Feedback",
                    description = "Vibrate softly when screen is locked or power dialog is triggered",
                    icon = Icons.Default.Vibration,
                    checked = uiState.vibrationEnabled,
                    onCheckedChange = onSetVibration,
                    testTag = "switch_vibration"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Biometric App Protection switch
                SettingsToggleRow(
                    title = "Biometric App Protection",
                    description = "Require fingerprint authentication before modifying device administration",
                    icon = Icons.Default.Fingerprint,
                    checked = uiState.biometricProtectionEnabled,
                    onCheckedChange = { enable ->
                        if (enable && uiState.biometricStatus.isAvailable) {
                            onPromptBiometric {
                                onSetBiometricProtection(true)
                            }
                        } else {
                            onSetBiometricProtection(false)
                        }
                    },
                    testTag = "switch_biometric_protection"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Digital Assistant Integration
        SettingsSectionHeader(title = "Default Digital Assistant")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Assistant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Lock with Home Button Gesture",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Set Screen Off as your default digital assistant app. Once selected, long-pressing the home button or swiping up from bottom screen corners will immediately lock your screen without physical buttons.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenDefaultAssistantSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_default_assistant_settings")
                ) {
                    Text("Configure Default Assistant App")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Device Administrator Lifecycle & Removal Helper
        SettingsSectionHeader(title = "Lifecycle & Uninstallation Helper")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberWarning.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Device Administrator Guide & Removal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "If you enabled Device Administrator and wish to uninstall the app or revoke the permission, Android security requires deactivating it first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showUninstallHelperDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_show_uninstall_instructions")
                    ) {
                        Text("Removal Instructions")
                    }

                    if (uiState.isDeviceAdminEnabled) {
                        Button(
                            onClick = {
                                if (uiState.biometricProtectionEnabled) {
                                    onPromptBiometric {
                                        ScreenOffDeviceAdminReceiver.removeAdmin(context)
                                    }
                                } else {
                                    ScreenOffDeviceAdminReceiver.removeAdmin(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_deactivate_admin")
                        ) {
                            Text("Deactivate Admin")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showUninstallHelperDialog) {
        AlertDialog(
            onDismissRequest = { showUninstallHelperDialog = false },
            title = {
                Text("How to Remove Device Admin", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "On older versions of Android, or if you enabled Device Administrator:\n\n" +
                                "1. Open device Settings\n" +
                                "2. Go to 'Security' (or 'Location & Security')\n" +
                                "3. Tap 'Device administrators' (or 'Device admin apps')\n" +
                                "4. Uncheck 'Screen Off' and confirm deactivation\n" +
                                "5. You can now uninstall the app normally.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showUninstallHelperDialog = false
                    onOpenDeviceAdminSettings()
                }) {
                    Text("Open Device Admin Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallHelperDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun MethodOptionRow(
    title: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
