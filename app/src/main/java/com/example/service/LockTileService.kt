package com.example.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.example.MainActivity
import com.example.receiver.ScreenOffDeviceAdminReceiver
import com.example.util.LockManager
import com.example.util.LockResult

class LockTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isReady = LockAccessibilityService.isAccessibilityEnabled(this) ||
                ScreenOffDeviceAdminReceiver.isDeviceAdminActive(this)

        tile.state = if (isReady) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Lock Screen"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isReady) "Tap to lock" else "Permission required"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val result = LockManager.executeLock(this, triggerSource = "Quick Settings Tile")
        when (result) {
            is LockResult.Success -> {
                // Screen locked
            }
            is LockResult.PermissionRequired -> {
                Toast.makeText(this, "Screen Off needs Accessibility or Device Admin permission", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startActivityAndCollapse(android.app.PendingIntent.getActivity(
                        this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
                    ))
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
            }
            is LockResult.Error -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }
        updateTileState()
    }
}
