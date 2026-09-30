package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.components.BiometricStatusCard
import com.example.ui.components.ServiceStatusBanner
import com.example.ui.theme.CyanPrimaryDark
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.IndigoSecondaryDark

@Composable
fun HomeScreen(
    uiState: MainUiState,
    totalLocks: Int,
    onTestLock: () -> Unit,
    onTestPowerMenu: () -> Unit,
    onEnableAccessibility: () -> Unit,
    onFixRestrictedSettings: () -> Unit,
    onVerifyFingerprint: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var infoDialogTitle by remember { mutableStateOf<String?>(null) }
    var infoDialogMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("home_screen_content")
    ) {
        // App header
        Text(
            text = "Screen Off",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Protect your physical power button & unlock with fingerprint",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Service Status Banner
        ServiceStatusBanner(
            isAccessibilityActive = uiState.isAccessibilityEnabled,
            onEnableAccessibility = onEnableAccessibility,
            onFixRestrictedSettings = onFixRestrictedSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Tactile Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Lock Screen Hero Card
            TactileActionCard(
                title = "Lock Screen",
                subtitle = "Tap to lock now",
                icon = Icons.Default.Lock,
                gradient = Brush.linearGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                ),
                onClick = onTestLock,
                testTag = "btn_hero_lock",
                modifier = Modifier.weight(1f)
            )

            // Power Menu Hero Card
            TactileActionCard(
                title = "Power Menu",
                subtitle = "Access restart & off",
                icon = Icons.Default.PowerSettingsNew,
                gradient = Brush.linearGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                ),
                onClick = onTestPowerMenu,
                testTag = "btn_hero_power_menu",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Power Button Preservation Counter
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hardware_savings_card")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldActive.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldActive,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$totalLocks Button Presses Spared",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Every tap spares your fragile physical mechanical switch from wear and tear.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Biometric Sensor Status
        BiometricStatusCard(
            biometricStatus = uiState.biometricStatus,
            onVerifyFingerprint = onVerifyFingerprint
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Ways to Lock section
        Text(
            text = "4 Ways to Lock Anywhere",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        FeatureAccessTile(
            title = "Quick Settings Tile",
            description = "Pull down your notification shade and tap the Screen Off tile.",
            icon = Icons.Default.Tune,
            onClick = {
                infoDialogTitle = "Quick Settings Tile"
                infoDialogMessage = "Swipe down twice from the top of your screen to open Quick Settings, tap the pencil (Edit) icon, and drag the 'Lock Screen' or 'Power Menu' tile into your active tiles."
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureAccessTile(
            title = "Resizable Home Screen Widget",
            description = "Place 1x1 or 2x1 interactive buttons right on your home screen.",
            icon = Icons.Default.Widgets,
            onClick = {
                infoDialogTitle = "Add Home Screen Widget"
                infoDialogMessage = "Long press on any empty space on your home screen, choose 'Widgets', locate 'Screen Off', and drag it to your screen. You can resize it horizontally to reveal both Lock and Power buttons!"
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureAccessTile(
            title = "Default Digital Assistant",
            description = "Lock instantly by long-pressing your Home button or navigation pill.",
            icon = Icons.Default.Assistant,
            onClick = {
                infoDialogTitle = "Default Digital Assistant"
                infoDialogMessage = "You can set Screen Off as your default assistant app. Long-pressing the home button or swiping diagonally from the bottom corner will then lock your screen instantly!"
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureAccessTile(
            title = "Status Bar Quick Notification",
            description = "Keep lock and power controls ready in your notification drawer.",
            icon = Icons.Default.Notifications,
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (infoDialogTitle != null && infoDialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                infoDialogTitle = null
                infoDialogMessage = null
            },
            title = {
                Text(infoDialogTitle!!, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(infoDialogMessage!!)
            },
            confirmButton = {
                TextButton(onClick = {
                    infoDialogTitle = null
                    infoDialogMessage = null
                }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun TactileActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "button_scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .scale(scale)
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun FeatureAccessTile(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

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
        }
    }
}
