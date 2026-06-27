package com.play.golf.perf.tracker.presentation.playerlist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.core.network.NetworkObserver
import com.play.golf.perf.tracker.core.network.NetworkStatus
import com.play.golf.perf.tracker.domain.model.Player
import com.play.golf.perf.tracker.domain.usecase.GetPlayersUseCase
import com.play.golf.perf.tracker.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlayerListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var networkObserver: NetworkObserver
    private lateinit var viewModel: PlayerListViewModel

    private val networkStatusFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    private val testPlayers = listOf(
        Player(1, "Tiger Woods",    "USA",             "Driver", null, 182.4, 285.6, 320),
        Player(2, "Rory McIlroy",   "Northern Ireland","Driver", null, 191.2, 311.6, 298),
        Player(3, "Jon Rahm",       "Spain",           "3-Iron", null, 178.9, 272.3, 341),
        Player(4, "Scottie Scheffler","USA",            "Driver", null, 186.7, 301.4, 315),
    )

    @Before
    fun setUp() {
        getPlayersUseCase = mockk()
        networkObserver   = mockk()

        every { networkObserver.networkStatus } returns networkStatusFlow

        // Default stubs — overridden per test where needed
        every { getPlayersUseCase(any(), any()) } returns flowOf(PagingData.from(testPlayers))
        every { getPlayersUseCase.refresh() } returns flowOf(Resource.Success(Unit))
        coEvery { getPlayersUseCase.getDistinctClubs() } returns listOf("Driver", "3-Iron")
    }

    private fun createViewModel() = PlayerListViewModel(
        getPlayersUseCase = getPlayersUseCase,
        networkObserver   = networkObserver,
    )

    // Initial State

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initial uiState has empty search query and no selected club`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.searchQuery).isEmpty()
        assertThat(viewModel.uiState.value.selectedClub).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `initial uiState isOffline is false when network is available`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOffline).isFalse()
    }

    @Test
    fun `initial uiState populates availableClubs after refresh`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.availableClubs)
            .containsExactly("Driver", "3-Iron")
    }

    // Search

    @Test
    fun `onSearchQueryChange updates searchQuery in uiState`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Tiger")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.searchQuery).isEqualTo("Tiger")
    }

    @Test
    fun `onSearchQueryChange with empty string clears search query`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Tiger")
        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.searchQuery).isEmpty()
    }

    // Club Filter

    @Test
    fun `onClubFilterSelected sets selectedClub in uiState`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onClubFilterSelected("Driver")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedClub).isEqualTo("Driver")
    }

    @Test
    fun `onClubFilterSelected with same club clears filter (toggle behaviour)`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onClubFilterSelected("Driver")
        viewModel.onClubFilterSelected("Driver")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedClub).isEmpty()
    }

    @Test
    fun `onClubFilterSelected switching club updates selectedClub`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onClubFilterSelected("Driver")
        viewModel.onClubFilterSelected("3-Iron")
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedClub).isEqualTo("3-Iron")
    }

    // Clear Filters

    @Test
    fun `clearFilters resets searchQuery and selectedClub`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Tiger")
        viewModel.onClubFilterSelected("Driver")
        viewModel.clearFilters()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.searchQuery).isEmpty()
        assertThat(viewModel.uiState.value.selectedClub).isEmpty()
    }

    // Refresh
    @Test
    fun `refreshPlayers sets isRefreshing true during loading`() = runTest {
        val refreshFlow = MutableSharedFlow<Resource<Unit>>(replay = 0)
        every { getPlayersUseCase.refresh() } returns refreshFlow

        viewModel = createViewModel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.isRefreshing).isFalse()

            viewModel.refreshPlayers()

            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

            refreshFlow.emit(Resource.Loading())

            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

            val loading = awaitItem()
            assertThat(loading.isRefreshing).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshPlayers clears isRefreshing on success`() = runTest {
        every { getPlayersUseCase.refresh() } returns flowOf(
            Resource.Loading(),
            Resource.Success(Unit)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isRefreshing).isFalse()
    }

    @Test
    fun `refreshPlayers sets errorMessage on non-connectivity error`() = runTest {
        every { getPlayersUseCase.refresh() } returns flowOf(
            Resource.Error("server error", isConnectivityError = false)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isEqualTo("server error")
    }

    @Test
    fun `refreshPlayers does not set errorMessage on connectivity error`() = runTest {
        every { getPlayersUseCase.refresh() } returns flowOf(
            Resource.Error("no internet", isConnectivityError = true)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        // Connectivity errors are shown via the offline banner, not errorMessage
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    // Network Observer

    @Test
    fun `uiState isOffline becomes true when network is lost`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // initial state

            networkStatusFlow.emit(NetworkStatus.Unavailable)
            val offline = awaitItem()
            assertThat(offline.isOffline).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState isOffline becomes false when network is restored`() = runTest {
        networkStatusFlow.value = NetworkStatus.Unavailable

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // offline initial state

            networkStatusFlow.emit(NetworkStatus.Available)
            val online = awaitItem()
            assertThat(online.isOffline).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Dismiss Error

    @Test
    fun `dismissError clears errorMessage`() = runTest {
        every { getPlayersUseCase.refresh() } returns flowOf(
            Resource.Error("something went wrong", isConnectivityError = false)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.dismissError()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }


}