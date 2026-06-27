package com.play.golf.perf.tracker.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.domain.model.PlayerDetail
import com.play.golf.perf.tracker.domain.model.Shot
import com.play.golf.perf.tracker.domain.repository.GolfRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetPlayerDetailUseCaseTest {

    private lateinit var repository: GolfRepository
    private lateinit var useCase: GetPlayerDetailUseCase

    // Test Fixtures

    private val testShots = listOf(
        Shot(
            shotId        = 1,
            playerId      = 1,
            club          = "Driver",
            ballSpeed     = 182.4,
            launchAngle   = 10.2,
            distance      = 292.1,
            spinRate      = 2680,
            carryDistance = 271.3,
            peakHeight    = 98.4,
            landingAngle  = 38.2,
        )
    )

    private val testPlayerDetail = PlayerDetail(
        id                 = 1,
        name               = "Tiger Woods",
        country            = "USA",
        club               = "Driver",
        avatarUrl          = null,
        averageSpeed       = 182.4,
        averageDistance    = 285.6,
        totalShots         = 320,
        bio                = "Greatest of all time",
        age                = 48,
        turnsProYear       = 1996,
        majorWins          = 15,
        totalWins          = 82,
        scoringAverage     = 68.1,
        greensInRegulation = 65.8,
        drivingAccuracy    = 61.2,
        puttingAverage     = 1.74,
        shots              = testShots,
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase    = GetPlayerDetailUseCase(repository)
    }

    // invoke, default (no force refresh)

    @Test
    fun `invoke delegates to repository getPlayerDetail`() = runTest {
        every {
            repository.getPlayerDetail(playerId = 1, forceRefresh = false)
        } returns flowOf(Resource.Success(testPlayerDetail))

        useCase(playerId = 1).collect()   // ✅ triggers the cold flow

        verify(exactly = 1) {
            repository.getPlayerDetail(playerId = 1, forceRefresh = false)
        }
    }

    @Test
    fun `invoke emits Loading then Success with player detail`() = runTest {
        every {
            repository.getPlayerDetail(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Loading())
            emit(Resource.Success(testPlayerDetail))
        }

        useCase(playerId = 1).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            val success = awaitItem() as Resource.Success
            assertThat(success.data?.name).isEqualTo("Tiger Woods")
            assertThat(success.data?.shots).hasSize(1)
            assertThat(success.data?.majorWins).isEqualTo(15)

            awaitComplete()
        }
    }

    @Test
    fun `invoke emits cached data then fresh data (offline-first two emissions)`() = runTest {
        val freshDetail = testPlayerDetail.copy(totalShots = 321)

        every {
            repository.getPlayerDetail(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Success(testPlayerDetail))  // from cache
            emit(Resource.Success(freshDetail))       // from network
        }

        useCase(playerId = 1).test {
            val first = awaitItem() as Resource.Success
            assertThat(first.data?.totalShots).isEqualTo(320)

            val second = awaitItem() as Resource.Success
            assertThat(second.data?.totalShots).isEqualTo(321)

            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Error when both cache and network fail`() = runTest {
        every {
            repository.getPlayerDetail(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Loading())
            emit(Resource.Error<PlayerDetail>("not found"))
        }

        useCase(playerId = 1).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("not found")
            awaitComplete()
        }
    }

    // invoke, forceRefresh = true

    @Test
    fun `invoke with forceRefresh true passes flag to repository`() = runTest {
        every {
            repository.getPlayerDetail(playerId = 1, forceRefresh = true)
        } returns flowOf(Resource.Success(testPlayerDetail))

        useCase(playerId = 1, forceRefresh = true).collect()

        verify(exactly = 1) {
            repository.getPlayerDetail(playerId = 1, forceRefresh = true)
        }
    }

    @Test
    fun `invoke with forceRefresh true emits fresh data`() = runTest {
        val freshDetail = testPlayerDetail.copy(scoringAverage = 67.9)

        every {
            repository.getPlayerDetail(playerId = 1, forceRefresh = true)
        } returns flowOf(Resource.Success(freshDetail))

        useCase(playerId = 1, forceRefresh = true).test {
            val success = awaitItem() as Resource.Success
            assertThat(success.data?.scoringAverage).isEqualTo(67.9)
            awaitComplete()
        }
    }

    // Connectivity error

    @Test
    fun `invoke emits connectivity error when offline and no cache`() = runTest {
        every {
            repository.getPlayerDetail(playerId = 99, forceRefresh = false)
        } returns flow {
            emit(Resource.Loading())
            emit(Resource.Error<PlayerDetail>("no internet", isConnectivityError = true))
        }

        useCase(playerId = 99).test {
            awaitItem() // Loading
            val error = awaitItem() as Resource.Error
            assertThat(error.isConnectivityError).isTrue()
            awaitComplete()
        }
    }
}