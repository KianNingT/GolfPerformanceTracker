package com.play.golf.perf.tracker.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.domain.model.Shot
import com.play.golf.perf.tracker.domain.repository.GolfRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetShotDetailUseCaseTest {

    private lateinit var repository: GolfRepository
    private lateinit var useCase: GetShotDetailUseCase

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
        repository = mockk()
        useCase    = GetShotDetailUseCase(repository)
    }

    // Cache hit

    @Test
    fun `invoke returns shot from cache when found`() = runTest {
        coEvery { repository.getCachedShot(playerId = 1, shotId = 3) } returns testShot

        val result = useCase(playerId = 1, shotId = 3)

        assertThat(result).isNotNull()
        assertThat(result?.shotId).isEqualTo(3)
        assertThat(result?.club).isEqualTo("7-Iron")
    }

    @Test
    fun `invoke returns shot with all correct metric fields`() = runTest {
        coEvery { repository.getCachedShot(playerId = 1, shotId = 3) } returns testShot

        val result = useCase(playerId = 1, shotId = 3)

        assertThat(result?.ballSpeed).isEqualTo(128.3)
        assertThat(result?.launchAngle).isEqualTo(16.8)
        assertThat(result?.distance).isEqualTo(178.4)
        assertThat(result?.spinRate).isEqualTo(6820)
        assertThat(result?.carryDistance).isEqualTo(172.1)
        assertThat(result?.peakHeight).isEqualTo(88.6)
        assertThat(result?.landingAngle).isEqualTo(48.3)
    }

    // Cache miss

    @Test
    fun `invoke returns null when shot not found in cache`() = runTest {
        coEvery { repository.getCachedShot(playerId = 1, shotId = 99) } returns null

        val result = useCase(playerId = 1, shotId = 99)

        assertThat(result).isNull()
    }

    @Test
    fun `invoke returns null when playerId does not exist in cache`() = runTest {
        coEvery { repository.getCachedShot(playerId = 999, shotId = 1) } returns null

        val result = useCase(playerId = 999, shotId = 1)

        assertThat(result).isNull()
    }

    // Delegation

    @Test
    fun `invoke delegates to repository getCachedShot with correct args`() = runTest {
        coEvery { repository.getCachedShot(playerId = 2, shotId = 5) } returns null

        useCase(playerId = 2, shotId = 5)

        coVerify(exactly = 1) { repository.getCachedShot(playerId = 2, shotId = 5) }
    }

    @Test
    fun `invoke does not call repository more than once`() = runTest {
        coEvery { repository.getCachedShot(playerId = 1, shotId = 3) } returns testShot

        useCase(playerId = 1, shotId = 3)

        coVerify(exactly = 1) { repository.getCachedShot(any(), any()) }
    }
}