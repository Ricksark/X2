package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.RoseDanger
import com.example.util.PinSecurityManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PinScreenMode {
    UNLOCK_APP,
    CREATE_PIN,
    CONFIRM_PIN,
    VERIFY_TO_DISABLE
}

@Composable
fun PinLockScreen(
    mode: PinScreenMode = PinScreenMode.UNLOCK_APP,
    pinManager: PinSecurityManager,
    isBiometricAvailable: Boolean = false,
    onBiometricClick: (() -> Unit)? = null,
    onSuccess: () -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var tempPinForConfirmation by remember { mutableStateOf("") }
    var currentSubMode by remember { mutableStateOf(mode) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutSeconds by remember { mutableStateOf(0) }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerKeyHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(20)
                }
            }
        } catch (_: Exception) {}
    }

    fun triggerErrorShake() {
        coroutineScope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -8f at 250
                    8f at 300
                    0f at 400
                }
            )
        }
    }

    // Check lockout on start and tick down
    LaunchedEffect(Unit) {
        if (pinManager.isLockedOut()) {
            lockoutSeconds = pinManager.getRemainingLockoutSeconds()
            while (lockoutSeconds > 0) {
                delay(1000)
                lockoutSeconds = pinManager.getRemainingLockoutSeconds()
            }
        }
    }

    // Automatically trigger biometric unlock on appearance if in UNLOCK mode
    LaunchedEffect(Unit) {
        if (currentSubMode == PinScreenMode.UNLOCK_APP && isBiometricAvailable && pinManager.isBiometricUnlockEnabled() && !pinManager.isLockedOut()) {
            delay(200)
            onBiometricClick?.invoke()
        }
    }

    fun onDigitPress(digit: String) {
        if (lockoutSeconds > 0) return
        if (enteredPin.length < PinSecurityManager.PIN_LENGTH) {
            triggerKeyHaptic()
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == PinSecurityManager.PIN_LENGTH) {
                when (currentSubMode) {
                    PinScreenMode.UNLOCK_APP -> {
                        if (pinManager.verifyPin(newPin)) {
                            onSuccess()
                        } else {
                            if (pinManager.isLockedOut()) {
                                lockoutSeconds = pinManager.getRemainingLockoutSeconds()
                                errorMessage = "Too many failed attempts. Try again in $lockoutSeconds s"
                            } else {
                                errorMessage = "Incorrect PIN. Try again."
                            }
                            triggerErrorShake()
                            enteredPin = ""
                        }
                    }
                    PinScreenMode.CREATE_PIN -> {
                        tempPinForConfirmation = newPin
                        enteredPin = ""
                        currentSubMode = PinScreenMode.CONFIRM_PIN
                    }
                    PinScreenMode.CONFIRM_PIN -> {
                        if (newPin == tempPinForConfirmation) {
                            pinManager.savePin(newPin)
                            onSuccess()
                        } else {
                            errorMessage = "PINs do not match. Please start over."
                            triggerErrorShake()
                            enteredPin = ""
                            tempPinForConfirmation = ""
                            currentSubMode = PinScreenMode.CREATE_PIN
                        }
                    }
                    PinScreenMode.VERIFY_TO_DISABLE -> {
                        if (pinManager.verifyPin(newPin)) {
                            pinManager.removePin()
                            onSuccess()
                        } else {
                            errorMessage = "Incorrect PIN."
                            triggerErrorShake()
                            enteredPin = ""
                        }
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            triggerKeyHaptic()
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    val titleText = when (currentSubMode) {
        PinScreenMode.UNLOCK_APP -> "Screen Off Security"
        PinScreenMode.CREATE_PIN -> "Create Security PIN"
        PinScreenMode.CONFIRM_PIN -> "Confirm Your PIN"
        PinScreenMode.VERIFY_TO_DISABLE -> "Enter Current PIN"
    }

    val subtitleText = when (currentSubMode) {
        PinScreenMode.UNLOCK_APP -> "Enter 4-digit PIN or use fingerprint to unlock"
        PinScreenMode.CREATE_PIN -> "Set a 4-digit security PIN for app access"
        PinScreenMode.CONFIRM_PIN -> "Re-enter the 4-digit PIN to confirm"
        PinScreenMode.VERIFY_TO_DISABLE -> "Verify your PIN to turn off app lock"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("pin_lock_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Indicator with Shake Animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .padding(vertical = 8.dp)
            ) {
                for (i in 0 until PinSecurityManager.PIN_LENGTH) {
                    val isFilled = i < enteredPin.length
                    val dotColor = when {
                        errorMessage != null -> RoseDanger
                        isFilled -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error or Lockout Message
            AnimatedVisibility(visible = errorMessage != null || lockoutSeconds > 0) {
                Text(
                    text = if (lockoutSeconds > 0) "Locked for $lockoutSeconds seconds" else (errorMessage ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = RoseDanger,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Keypad (3x4 Grid)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIOMETRIC", "0", "BACKSPACE")
            )

            keypad.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        when (key) {
                            "BIOMETRIC" -> {
                                if (currentSubMode == PinScreenMode.UNLOCK_APP && isBiometricAvailable && pinManager.isBiometricUnlockEnabled()) {
                                    IconButton(
                                        onClick = { onBiometricClick?.invoke() },
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .testTag("key_biometric")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Unlock with Biometrics",
                                            tint = EmeraldActive,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                            }
                            "BACKSPACE" -> {
                                IconButton(
                                    onClick = { onBackspace() },
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .testTag("key_backspace")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            else -> {
                                KeypadButton(
                                    digit = key,
                                    onClick = { onDigitPress(key) }
                                )
                            }
                        }
                    }
                }
            }

            if (onCancel != null && currentSubMode != PinScreenMode.UNLOCK_APP) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onCancel) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    digit: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.92f else 1.0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("key_$digit")
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
