package com.zex.tracker.di

import android.content.Context
import androidx.room.Room
import com.zex.tracker.data.local.ZexDatabase
import com.zex.tracker.data.local.dao.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context, prefs: com.zex.tracker.data.local.prefs.SecurePrefs): ZexDatabase {
        var dbKey = prefs.getString("db_encryption_key")
        if (dbKey.isNullOrEmpty()) {
            dbKey = java.util.UUID.randomUUID().toString()
            prefs.putString("db_encryption_key", dbKey)
        }
        
        try {
            net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
        } catch (e: Exception) {
            com.zex.tracker.core.logging.ZexLogger.e("DatabaseModule", "Failed to load SQLCipher libs", e)
        }
        
        val factory = net.sqlcipher.database.SupportFactory(dbKey.toByteArray())

        return Room.databaseBuilder(context, ZexDatabase::class.java, "zex_secure.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideLocationDao(database: ZexDatabase): LocationDao {
        return database.locationDao()
    }
}
