package com.play.golf.perf.tracker.presentation.playerdetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.core.network.NetworkObserver
import com.play.golf.perf.tracker.core.network.NetworkStatus
import com.play.golf.perf.tracker.domain.model.PlayerDetail
import com.play.golf.perf.tracker.domain.model.Shot
import com.play.golf.perf.tracker.domain.usecase.GetPlayerDetailUseCase
import com.play.golf.perf.tracker.navigation.GolfNavArgs
import com.play.golf.perf.tracker.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlayerDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getPlayerDetailUseCase: GetPlayerDetailUseCase
    private lateinit var networkObserver: NetworkObserver
    private lateinit var savedStateHandle: SavedStateHandle

    private val networkStatusFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    private val testShot = Shot(
        shotId = 1, playerId = 1, club = "Driver",
        ballSpeed = 182.4, launchAngle = 10.2, distance = 292.1,
        spinRate = 2680, carryDistance = 271.3, peakHeight = 98.4, landingAngle = 38.2,
    )

    private val testPlayerDetail = PlayerDetail(
        id = 1, name = "Tiger Woods", country = "USA", club = "Driver",
        avatarUrl = null, averageSpeed = 182.4, averageDistance = 285.6, totalShots = 320,
        bio = "Greatest", age = 48, turnsProYear = 1996, majorWins = 15, totalWins = 82,
        scoringAverage = 68.1, greensInRegulation = 65.8, drivingAccuracy = 61.2,
        puttingAverage = 1.74, shots = listOf(testShot),
    )

    @Before
    fun setUp() {
        getPlayerDetailUseCase = mockk()
        networkObserver        = mockk()
        savedStateHandle       = SavedStateHandle(mapOf(GolfNavArgs.PLAYER_ID to 1))

        every { networkObserver.networkStatus } returns networkStatusFlow
    }

    private fun createViewModel() = PlayerDetailViewModel(
        getPlayerDetailUseCase = getPlayerDetailUseCase,
        networkObserver        = networkObserver,
        savedStateHandle       = savedStateHandle,
    )

    // Initial Loading

    @Test
    fun `uiState shows isLoading true during initial load with no cache`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Loading())
            // never completes
        }

        // safety stub for any forceRefresh = true call
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = true)
        } returns flow { emit(Resource.Loading()) }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)  // ← skips the default empty state
            val loading = awaitItem()
            assertThat(loading.isLoading).isTrue()
            assertThat(loading.playerDetail).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shows player detail on successful load`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flowOf(Resource.Success(testPlayerDetail))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.playerDetail).isNotNull()
        assertThat(viewModel.uiState.value.playerDetail?.name).isEqualTo("Tiger Woods")
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shows shots list after successful load`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flowOf(Resource.Success(testPlayerDetail))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.playerDetail?.shots).hasSize(1)
        assertThat(viewModel.uiState.value.playerDetail?.shots?.first()?.club)
            .isEqualTo("Driver")
    }

    // Offline first dual emission

    @Test
    fun `uiState receives cached data then fresh data without blank loading state`() = runTest {
        val freshDetail = testPlayerDetail.copy(totalShots = 325)

        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Success(testPlayerDetail))
            emit(Resource.Success(freshDetail))
        }

        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = true)
        } returns flow { /* whatever you want, maybe success */ }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)  // ← skips the default empty state
            val first = awaitItem()
            assertThat(first.playerDetail?.totalShots).isEqualTo(320)

            val second = awaitItem()
            assertThat(second.playerDetail?.totalShots).isEqualTo(325)
            assertThat(second.isLoading).isFalse()
            assertThat(second.isRefreshing).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Error Handling

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shows hasError true when no cache and network fails`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Loading())
            emit(Resource.Error<PlayerDetail>("failed"))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasError).isTrue()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("failed")
        assertThat(viewModel.uiState.value.playerDetail).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState does not show hasError when cache exists and background refresh fails`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Success(testPlayerDetail)) // from cache
            emit(Resource.Error<PlayerDetail>("refresh failed"))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        // cache is present — error should NOT wipe out the screen
        assertThat(viewModel.uiState.value.playerDetail).isNotNull()
        assertThat(viewModel.uiState.value.hasError).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("refresh failed")
    }

    // Force Refresh

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onRefresh calls use case with forceRefresh true`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flowOf(Resource.Success(testPlayerDetail))

        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = true)
        } returns flowOf(Resource.Success(testPlayerDetail))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRefresh()
        advanceUntilIdle()

        verify { getPlayerDetailUseCase(playerId = 1, forceRefresh = true) }
    }

    // Stats Expand/Collapse

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `isStatsExpanded defaults to true`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flowOf(Resource.Success(testPlayerDetail))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isStatsExpanded).isTrue()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggleStatsExpanded flips isStatsExpanded`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flowOf(Resource.Success(testPlayerDetail))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleStatsExpanded()
        assertThat(viewModel.uiState.value.isStatsExpanded).isFalse()

        viewModel.toggleStatsExpanded()
        assertThat(viewModel.uiState.value.isStatsExpanded).isTrue()
    }

    // Network State

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState isOffline reflects network status`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flowOf(Resource.Success(testPlayerDetail))

        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = true)
        } returns flowOf(Resource.Success(testPlayerDetail))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // current state

            networkStatusFlow.emit(NetworkStatus.Unavailable)
            assertThat(awaitItem().isOffline).isTrue()

            networkStatusFlow.emit(NetworkStatus.Available)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Dismiss Error

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `dismissError clears hasError and errorMessage`() = runTest {
        every {
            getPlayerDetailUseCase(playerId = 1, forceRefresh = false)
        } returns flow {
            emit(Resource.Loading())
            emit(Resource.Error<PlayerDetail>("oops"))
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasError).isTrue()

        viewModel.dismissError()

        assertThat(viewModel.uiState.value.hasError).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}