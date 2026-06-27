package com.play.golf.perf.tracker.domain.usecase

import androidx.paging.PagingData
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.domain.model.Player
import com.play.golf.perf.tracker.domain.repository.GolfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for the Player List screen (Screen 1).
 *
 * Responsibilities:
 * - Provide a paged, searchable, filterable [Flow] of [Player] domain models
 * - Trigger a remote refresh of the player list cache
 * - Expose distinct club types for the filter chip row
 */
class GetPlayersUseCase @Inject constructor(
    private val repository: GolfRepository
) {
    /**
     * Returns a [Flow] of [PagingData] sourced from Room.
     * Search and club filter are applied locally — no network call.
     *
     * @param nameQuery  player name search string (empty = no filter)
     * @param clubFilter exact club type match (empty = no filter)
     */
    operator fun invoke(
        nameQuery: String = "",
        clubFilter: String = ""
    ): Flow<PagingData<Player>> = repository.getPagedPlayers(
        nameQuery  = nameQuery,
        clubFilter = clubFilter
    )

    /**
     * Triggers a network refresh and returns a [Flow] of [Resource<Unit>]
     * so the ViewModel can react to loading/success/error states.
     */
    fun refresh(): Flow<Resource<Unit>> = repository.refreshPlayers()

    /**
     * Returns the list of distinct club types from the current cache.
     * Used to populate filter chips above the player list.
     */
    suspend fun getDistinctClubs(): List<String> = repository.getDistinctClubs()
}