package com.play.golf.perf.tracker.di

import android.content.Context
import androidx.room.Room
import com.play.golf.perf.tracker.data.local.GolfDatabase
import com.play.golf.perf.tracker.data.local.dao.PlayerDao
import com.play.golf.perf.tracker.data.local.dao.ShotDao
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
    fun provideGolfDatabase(
        @ApplicationContext context: Context
    ): GolfDatabase = Room.databaseBuilder(
        context,
        GolfDatabase::class.java,
        GolfDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration() // acceptable for a take-home; swap for migrations in prod
        .build()

    @Provides
    @Singleton
    fun providePlayerDao(database: GolfDatabase): PlayerDao = database.playerDao()

    @Provides
    @Singleton
    fun provideShotDao(database: GolfDatabase): ShotDao = database.shotDao()
}