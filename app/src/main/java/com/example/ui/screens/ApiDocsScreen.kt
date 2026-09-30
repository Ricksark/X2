package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receiver.ScreenOffActionReceiver
import com.example.ui.theme.EmeraldActive

@Composable
fun ApiDocsScreen(
    onTriggerTestApiLock: () -> Unit,
    onTriggerTestApiPower: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("api_docs_screen_content")
    ) {
        Text(
            text = "Developer API & Integrations",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Automate screen lock with Tasker, Automate, ADB, or your own apps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live API Test Panel
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live API Endpoint Testing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Test triggering the public broadcast intents registered in AndroidManifest:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTriggerTestApiLock,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_api_lock")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast Lock")
                    }

                    OutlinedButton(
                        onClick = onTriggerTestApiPower,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_api_power")
                    ) {
                        Text("Broadcast Power")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // API Endpoint 1: Screen Lock Intent
        ApiEndpointCard(
            title = "Lock Screen Intent",
            actionString = ScreenOffActionReceiver.ACTION_LOCK,
            description = "Turns off the screen instantly using the active locking engine (Accessibility or Device Admin).",
            onCopy = { copyToClipboard(ScreenOffActionReceiver.ACTION_LOCK, "Intent Action") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // API Endpoint 2: Power Menu Intent
        ApiEndpointCard(
            title = "Open Power Menu Intent",
            actionString = ScreenOffActionReceiver.ACTION_POWER_MENU,
            description = "Triggers the system power dialogue (Shutdown / Restart options) using Accessibility.",
            onCopy = { copyToClipboard(ScreenOffActionReceiver.ACTION_POWER_MENU, "Intent Action") }
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ADB Shell Commands
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldActive)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ADB Shell Commands",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Broadcast command (Background):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val adbBroadcast = "adb shell am broadcast -a com.aistudio.screenoff.ACTION_LOCK"
                CodeSnippetBox(code = adbBroadcast, onCopy = { copyToClipboard(adbBroadcast, "ADB Broadcast") })

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Activity start command (Direct UI):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val adbActivity = "adb shell am start -a com.aistudio.screenoff.ACTION_LOCK"
                CodeSnippetBox(code = adbActivity, onCopy = { copyToClipboard(adbActivity, "ADB Activity") })
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Third-Party App Integration (Tasker & Automate)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tasker & Automate Integration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "In Tasker:\n" +
                            "1. Add Action -> System -> Send Intent\n" +
                            "2. Action: com.aistudio.screenoff.ACTION_LOCK\n" +
                            "3. Target: Broadcast Receiver (or Activity)\n" +
                            "4. Leave other fields empty.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Kotlin Code Sample:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val kotlinSnippet = """
val intent = Intent("com.aistudio.screenoff.ACTION_LOCK")
intent.setPackage("com.aistudio.screenoff.lock")
context.sendBroadcast(intent)
                """.trimIndent()
                CodeSnippetBox(code = kotlinSnippet, onCopy = { copyToClipboard(kotlinSnippet, "Kotlin Code") })
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ApiEndpointCard(
    title: String,
    actionString: String,
    description: String,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            CodeSnippetBox(code = actionString, onCopy = onCopy)
        }
    }
}

@Composable
fun CodeSnippetBox(
    code: String,
    onCopy: () -> Unit
) {
    val hScroll = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(hScroll)
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
