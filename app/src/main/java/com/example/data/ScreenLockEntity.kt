package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_lock_events")
data class ScreenLockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerSource: String, // "Quick Settings Tile", "App Widget", "Notification", "Digital Assistant", "In-App Button", "Third-Party API"
    val method: String,        // "Accessibility API" or "Device Administrator"
    val actionType: String,    // "Lock Screen" or "Power Menu"
    val isSuccess: Boolean = true
)

data class SourceCount(
    val triggerSource: String,
    val count: Int
)

data class MethodCount(
    val method: String,
    val count: Int
)

data class DayCount(
    val dayTimestamp: Long,
    val count: Int
)
