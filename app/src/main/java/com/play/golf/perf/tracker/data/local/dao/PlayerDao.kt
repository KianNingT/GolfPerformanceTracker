package com.play.golf.perf.tracker.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.play.golf.perf.tracker.data.local.entity.PlayerDetailEntity
import com.play.golf.perf.tracker.data.local.entity.PlayerEntity

@Dao
interface PlayerDao {

    // Player List
    /**
     * Inserts or replaces all players on each sync.
     * REPLACE strategy ensures stale data is overwritten.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    /**
     * Returns a [PagingSource] of all players ordered by name.
     * Used by Paging 3 on the Player List screen.
     */
    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getPagedPlayers(): PagingSource<Int, PlayerEntity>

    /**
     * Returns a [PagingSource] filtered by name and/or club.
     * Empty strings match all rows — allows reusing one query for
     * search-by-name, filter-by-club, or both simultaneously.
     *
     * Search is immediate (local Room query, no API call).
     */
    @Query(
        """
        SELECT * FROM players
        WHERE (:nameQuery = '' OR name LIKE '%' || :nameQuery || '%')
        AND   (:clubFilter = '' OR club = :clubFilter)
        ORDER BY name ASC
        """
    )
    fun searchPagedPlayers(
        nameQuery: String,
        clubFilter: String
    ): PagingSource<Int, PlayerEntity>

    /**
     * One-shot query — returns all club types currently in the cache.
     * Used to populate the club filter chip row on Screen 1.
     */
    @Query("SELECT DISTINCT club FROM players ORDER BY club ASC")
    suspend fun getDistinctClubs(): List<String>

    /**
     * Returns true when the players table has at least one row.
     * Used by the repository to decide whether to show cached data
     * while a network refresh is in progress.
     */
    @Query("SELECT COUNT(*) FROM players")
    suspend fun getPlayerCount(): Int

    /** Clears the entire players table before a full re-sync. */
    @Query("DELETE FROM players")
    suspend fun clearPlayers()

    // Player Detail
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerDetail(playerDetail: PlayerDetailEntity)

    @Query("SELECT * FROM player_details WHERE id = :playerId")
    suspend fun getPlayerDetail(playerId: Int): PlayerDetailEntity?

    @Query("DELETE FROM player_details WHERE id = :playerId")
    suspend fun clearPlayerDetail(playerId: Int)

    /**
     * Wraps insert of detail + shots in a single transaction so they
     * are always stored atomically — no partial cache states.
     * Shots are cleared first to avoid duplicates on re-fetch.
     */
    @Transaction
    suspend fun insertPlayerDetailWithShots(
        playerDetail: PlayerDetailEntity,
        insertShots: suspend () -> Unit
    ) {
        insertPlayerDetail(playerDetail)
        insertShots()
    }
}