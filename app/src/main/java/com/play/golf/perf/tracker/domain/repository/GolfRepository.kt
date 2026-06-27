package com.play.golf.perf.tracker.domain.repository

import androidx.paging.PagingData
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.domain.model.Player
import com.play.golf.perf.tracker.domain.model.PlayerDetail
import com.play.golf.perf.tracker.domain.model.Shot
import kotlinx.coroutines.flow.Flow

interface GolfRepository {

    /**
     * Returns a [Flow] of [PagingData] for the player list.
     * The repository decides whether to serve from cache or trigger a network fetch.
     * Paging 3 drives pagination automatically via Room [PagingSource].
     *
     * @param nameQuery  filter by player name (empty string = no filter)
     * @param clubFilter filter by club type (empty string = no filter)
     */
    fun getPagedPlayers(
        nameQuery: String = "",
        clubFilter: String = ""
    ): Flow<PagingData<Player>>

    /**
     * Triggers a full refresh of the player list from the remote API
     * and persists results to Room. Emits [Resource.Loading], then
     * [Resource.Success] or [Resource.Error].
     */
    fun refreshPlayers(): Flow<Resource<Unit>>

    /**
     * Returns all distinct club types currently cached in Room.
     * Used to populate the filter chip row on Screen 1.
     */
    suspend fun getDistinctClubs(): List<String>

    /**
     * Fetches full player detail including shots.
     * Serves from Room cache if available; fetches from network otherwise
     * or when [forceRefresh] is true.
     *
     * @param playerId     the player's numeric ID
     * @param forceRefresh bypasses cache and always hits the network
     */
    fun getPlayerDetail(
        playerId: Int,
        forceRefresh: Boolean = false
    ): Flow<Resource<PlayerDetail>>

    /**
     * Returns a single cached shot by [playerId] and [shotId].
     * Used for navigating to Screen 3 (Shot Detail) without re-fetching.
     */
    suspend fun getCachedShot(playerId: Int, shotId: Int): Shot?
}