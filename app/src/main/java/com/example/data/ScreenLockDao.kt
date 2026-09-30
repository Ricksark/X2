package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenLockDao {

    @Insert
    suspend fun insertEvent(event: ScreenLockEntity): Long

    @Query("SELECT COUNT(*) FROM screen_lock_events WHERE actionType = 'Lock Screen'")
    fun getTotalLockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM screen_lock_events WHERE actionType = 'Power Menu'")
    fun getTotalPowerMenuCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM screen_lock_events")
    fun getTotalActionCount(): Flow<Int>

    @Query("SELECT * FROM screen_lock_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 30): Flow<List<ScreenLockEntity>>

    @Query("SELECT triggerSource, COUNT(*) as count FROM screen_lock_events GROUP BY triggerSource ORDER BY count DESC")
    fun getCountsBySource(): Flow<List<SourceCount>>

    @Query("SELECT method, COUNT(*) as count FROM screen_lock_events GROUP BY method")
    fun getCountsByMethod(): Flow<List<MethodCount>>

    @Query("DELETE FROM screen_lock_events")
    suspend fun clearAll()
}
