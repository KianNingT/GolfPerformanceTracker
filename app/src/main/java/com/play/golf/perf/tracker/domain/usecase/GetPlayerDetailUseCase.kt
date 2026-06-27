package com.play.golf.perf.tracker.domain.usecase

import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.domain.model.PlayerDetail
import com.play.golf.perf.tracker.domain.repository.GolfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for the Player Detail screen (Screen 2).
 *
 * Responsibilities:
 * - Fetch full player detail including shots (offline-first)
 * - Support forced network refresh for pull-to-refresh
 */
class GetPlayerDetailUseCase @Inject constructor(
    private val repository: GolfRepository
) {
    /**
     * Returns a [Flow] of [Resource<PlayerDetail>].
     * Serves from Room cache first, then refreshes from network in background.
     *
     * @param playerId     the player's numeric ID
     * @param forceRefresh true to bypass cache and always hit the network
     */
    operator fun invoke(
        playerId: Int,
        forceRefresh: Boolean = false
    ): Flow<Resource<PlayerDetail>> = repository.getPlayerDetail(
        playerId     = playerId,
        forceRefresh = forceRefresh
    )
}