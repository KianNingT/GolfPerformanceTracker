package com.play.golf.perf.tracker.di

import com.play.golf.perf.tracker.data.repository.GolfRepositoryImpl
import com.play.golf.perf.tracker.domain.repository.GolfRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the concrete [GolfRepositoryImpl] to the [GolfRepository] interface.
     * Use @Binds (not @Provides) since no extra instantiation logic is needed —
     * Hilt constructs [GolfRepositoryImpl] directly via @Inject constructor.
     */
    @Binds
    @Singleton
    abstract fun bindGolfRepository(
        impl: GolfRepositoryImpl
    ): GolfRepository
}