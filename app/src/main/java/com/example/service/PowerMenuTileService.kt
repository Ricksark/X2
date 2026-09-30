package com.example.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.example.MainActivity
import com.example.util.LockManager
import com.example.util.LockResult

class PowerMenuTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isReady = LockAccessibilityService.isAccessibilityEnabled(this)
        tile.state = if (isReady) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Power Menu"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isReady) "Open dialog" else "Needs Accessibility"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val result = LockManager.executePowerMenu(this, triggerSource = "Quick Settings Tile")
        when (result) {
            is LockResult.Success -> {
                // Power menu shown
            }
            is LockResult.PermissionRequired -> {
                Toast.makeText(this, "Power Menu requires Accessibility Service to be enabled", Toast.LENGTH_LONG).show()
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
