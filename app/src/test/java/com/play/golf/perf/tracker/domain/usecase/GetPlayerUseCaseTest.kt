package com.play.golf.perf.tracker.domain.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.domain.model.Player
import com.play.golf.perf.tracker.domain.repository.GolfRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetPlayersUseCaseTest {

    private lateinit var repository: GolfRepository
    private lateinit var useCase: GetPlayersUseCase

    // Test fixtures

    private val testPlayers = listOf(
        Player(
            id              = 1,
            name            = "Tiger Woods",
            country         = "USA",
            club            = "Driver",
            avatarUrl       = null,
            averageSpeed    = 182.4,
            averageDistance = 285.6,
            totalShots      = 320,
        ),
        Player(
            id              = 2,
            name            = "Rory McIlroy",
            country         = "Northern Ireland",
            club            = "Driver",
            avatarUrl       = null,
            averageSpeed    = 191.2,
            averageDistance = 311.6,
            totalShots      = 298,
        ),
        Player(
            id              = 3,
            name            = "Jon Rahm",
            country         = "Spain",
            club            = "3-Iron",
            avatarUrl       = null,
            averageSpeed    = 178.9,
            averageDistance = 272.3,
            totalShots      = 341,
        ),
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase    = GetPlayersUseCase(repository)
    }

    // invoke

    @Test
    fun `invoke delegates to repository getPagedPlayers with empty params by default`() = runTest {
        every {
            repository.getPagedPlayers(nameQuery = "", clubFilter = "")
        } returns flowOf(PagingData.from(testPlayers))

        useCase()

        verify(exactly = 1) {
            repository.getPagedPlayers(nameQuery = "", clubFilter = "")
        }
    }

    @Test
    fun `invoke passes nameQuery and clubFilter to repository`() = runTest {
        every {
            repository.getPagedPlayers(nameQuery = "Tiger", clubFilter = "Driver")
        } returns flowOf(PagingData.from(testPlayers))

        useCase(nameQuery = "Tiger", clubFilter = "Driver")

        verify(exactly = 1) {
            repository.getPagedPlayers(nameQuery = "Tiger", clubFilter = "Driver")
        }
    }

    @Test
    fun `invoke returns paged data from repository`() = runTest {
        every {
            repository.getPagedPlayers(nameQuery = "", clubFilter = "")
        } returns flowOf(PagingData.from(testPlayers))

        val result = useCase().asSnapshot()

        assertThat(result).hasSize(3)
        assertThat(result[0].name).isEqualTo("Tiger Woods")
        assertThat(result[1].name).isEqualTo("Rory McIlroy")
    }

    @Test
    fun `invoke with name query returns filtered paged data`() = runTest {
        val filtered = testPlayers.filter { it.name.contains("Tiger") }
        every {
            repository.getPagedPlayers(nameQuery = "Tiger", clubFilter = "")
        } returns flowOf(PagingData.from(filtered))

        val result = useCase(nameQuery = "Tiger").asSnapshot()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Tiger Woods")
    }

    @Test
    fun `invoke with club filter returns only matching players`() = runTest {
        val ironPlayers = testPlayers.filter { it.club == "3-Iron" }
        every {
            repository.getPagedPlayers(nameQuery = "", clubFilter = "3-Iron")
        } returns flowOf(PagingData.from(ironPlayers))

        val result = useCase(clubFilter = "3-Iron").asSnapshot()

        assertThat(result).hasSize(1)
        assertThat(result[0].club).isEqualTo("3-Iron")
    }

    @Test
    fun `invoke with no matches returns empty paged data`() = runTest {
        every {
            repository.getPagedPlayers(nameQuery = "zzz", clubFilter = "")
        } returns flowOf(PagingData.from(emptyList()))

        val result = useCase(nameQuery = "zzz").asSnapshot()

        assertThat(result).isEmpty()
    }

    // refresh

    @Test
    fun `refresh delegates to repository refreshPlayers`() = runTest {
        every { repository.refreshPlayers() } returns flowOf(Resource.Success(Unit))

        useCase.refresh()

        verify(exactly = 1) { repository.refreshPlayers() }
    }

    @Test
    fun `refresh emits Loading then Success from repository`() = runTest {
        every { repository.refreshPlayers() } returns flow {
            emit(Resource.Loading())
            emit(Resource.Success(Unit))
        }

        useCase.refresh().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            assertThat(awaitItem()).isInstanceOf(Resource.Success::class.java)
            awaitComplete()
        }
    }

    @Test
    fun `refresh emits Loading then Error on network failure`() = runTest {
        every { repository.refreshPlayers() } returns flow {
            emit(Resource.Loading())
            emit(Resource.Error<Unit>("network error", isConnectivityError = true))
        }

        useCase.refresh().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.isConnectivityError).isTrue()
            awaitComplete()
        }
    }

    // getDistinctClubs

    @Test
    fun `getDistinctClubs delegates to repository`() = runTest {
        coEvery { repository.getDistinctClubs() } returns listOf("Driver", "3-Iron", "5-Iron")

        val clubs = useCase.getDistinctClubs()

        coVerify(exactly = 1) { repository.getDistinctClubs() }
        assertThat(clubs).containsExactly("Driver", "3-Iron", "5-Iron").inOrder()
    }

    @Test
    fun `getDistinctClubs returns empty list when cache is empty`() = runTest {
        coEvery { repository.getDistinctClubs() } returns emptyList()

        val clubs = useCase.getDistinctClubs()

        assertThat(clubs).isEmpty()
    }
}