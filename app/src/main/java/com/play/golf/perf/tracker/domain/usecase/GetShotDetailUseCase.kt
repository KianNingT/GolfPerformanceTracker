package com.play.golf.perf.tracker.domain.usecase

import com.play.golf.perf.tracker.domain.model.Shot
import com.play.golf.perf.tracker.domain.repository.GolfRepository
import javax.inject.Inject

/**
 * Use case for the Shot Detail screen (Screen 3).
 *
 * Retrieves a single [Shot] from the Room cache using [playerId] + [shotId].
 * No network call is needed here — shots are always cached when the
 * player detail page is opened (Screen 2 guarantees the cache is warm).
 */
class GetShotDetailUseCase @Inject constructor(
    private val repository: GolfRepository
) {
    /**
     * Returns the cached [Shot] or null if not found.
     * Callers should handle the null case by navigating back gracefully.
     *
     * @param playerId the parent player's numeric ID
     * @param shotId   the shot's numeric ID within that player's shot list
     */
    suspend operator fun invoke(playerId: Int, shotId: Int): Shot? =
        repository.getCachedShot(playerId, shotId)
}