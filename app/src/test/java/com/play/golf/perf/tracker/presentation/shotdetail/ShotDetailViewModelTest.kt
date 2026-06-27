package com.play.golf.perf.tracker.presentation.shotdetail

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.domain.model.Shot
import com.play.golf.perf.tracker.domain.usecase.GetShotDetailUseCase
import com.play.golf.perf.tracker.navigation.GolfNavArgs
import com.play.golf.perf.tracker.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ShotDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getShotDetailUseCase: GetShotDetailUseCase

    private val testShot = Shot(
        shotId        = 3,
        playerId      = 1,
        club          = "7-Iron",
        ballSpeed     = 128.3,
        launchAngle   = 16.8,
        distance      = 178.4,
        spinRate      = 6820,
        carryDistance = 172.1,
        peakHeight    = 88.6,
        landingAngle  = 48.3,
    )

    @Before
    fun setUp() {
        getShotDetailUseCase = mockk()
    }

    private fun createViewModel(
        playerId: Int = 1,
        shotId: Int   = 3,
    ) = ShotDetailViewModel(
        getShotDetailUseCase = getShotDetailUseCase,
        savedStateHandle     = SavedStateHandle(
            mapOf(
                GolfNavArgs.PLAYER_ID to playerId,
                GolfNavArgs.SHOT_ID   to shotId,
            )
        )
    )

    // Cache Hit

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState contains shot when found in cache`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 3) } returns testShot

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.shot).isNotNull()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.hasError).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shot contains correct club`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 3) } returns testShot

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.shot?.club).isEqualTo("7-Iron")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shot contains correct ballSpeed`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 3) } returns testShot

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.shot?.ballSpeed).isEqualTo(128.3)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shot contains correct spinRate`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 3) } returns testShot

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.shot?.spinRate).isEqualTo(6820)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shot contains correct carryDistance`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 3) } returns testShot

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.shot?.carryDistance).isEqualTo(172.1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shot contains correct peakHeight and landingAngle`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 3) } returns testShot

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.shot?.peakHeight).isEqualTo(88.6)
        assertThat(viewModel.uiState.value.shot?.landingAngle).isEqualTo(48.3)
    }

    // Cache Miss

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState shows hasError true when shot not in cache`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 99) } returns null

        val viewModel = createViewModel(playerId = 1, shotId = 99)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.hasError).isTrue()
        assertThat(viewModel.uiState.value.shot).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState errorMessage is not null when shot not found`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 99) } returns null

        val viewModel = createViewModel(playerId = 1, shotId = 99)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `uiState isLoading is false after cache miss`() = runTest {
        coEvery { getShotDetailUseCase(playerId = 1, shotId = 99) } returns null

        val viewModel = createViewModel(playerId = 1, shotId = 99)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    // Different player/shot IDs

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `reads correct playerId and shotId from SavedStateHandle`() = runTest {
        val differentShot = testShot.copy(shotId = 5, playerId = 2, club = "5-Iron")
        coEvery { getShotDetailUseCase(playerId = 2, shotId = 5) } returns differentShot

        val viewModel = createViewModel(playerId = 2, shotId = 5)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.shot?.shotId).isEqualTo(5)
        assertThat(viewModel.uiState.value.shot?.club).isEqualTo("5-Iron")
    }
}