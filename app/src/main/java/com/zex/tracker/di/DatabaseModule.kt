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
    fun provideDatabase(@ApplicationContext context: Context): ZexDatabase {
        return Room.databaseBuilder(context, ZexDatabase::class.java, "zex.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideLocationDao(database: ZexDatabase): LocationDao {
        return database.locationDao()
    }
}
