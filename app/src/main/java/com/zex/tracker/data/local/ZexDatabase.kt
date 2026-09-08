package com.zex.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zex.tracker.data.local.dao.LocationDao
import com.zex.tracker.data.local.entity.LocationEntity

@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
abstract class ZexDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
