package com.play.golf.perf.tracker.di

import android.content.Context
import com.play.golf.perf.tracker.core.datastore.ThemeDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideThemeDataStore(
        @ApplicationContext context: Context
    ): ThemeDataStore = ThemeDataStore(context)
}