package com.zex.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zex.tracker.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationEntity): Long

    @Query("SELECT * FROM locations WHERE uploaded = 0 ORDER BY recordedAt ASC LIMIT :limit")
    suspend fun getPendingUploads(limit: Int): List<LocationEntity>

    @Query("UPDATE locations SET uploaded = 1 WHERE id IN (:ids)")
    suspend fun markUploaded(ids: List<Int>)

    @Query("DELETE FROM locations WHERE uploaded = 1 AND recordedAt < :timestamp")
    suspend fun deleteUploadedOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM locations WHERE uploaded = 0")
    fun getPendingCountFlow(): Flow<Int>
}
