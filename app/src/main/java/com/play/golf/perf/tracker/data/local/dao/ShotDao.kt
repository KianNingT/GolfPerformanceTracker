package com.play.golf.perf.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.play.golf.perf.tracker.data.local.entity.ShotEntity

@Dao
interface ShotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShots(shots: List<ShotEntity>)

    /**
     * Returns all shots belonging to a player ordered by shotId ascending.
     * Foreign key cascade handles deletion when parent player detail is removed.
     */
    @Query("SELECT * FROM shots WHERE player_id = :playerId ORDER BY shot_id ASC")
    suspend fun getShotsForPlayer(playerId: Int): List<ShotEntity>

    /**
     * Returns a single shot by its [playerId] and [shotId].
     * Used when navigating to Screen 3 (Shot Detail).
     */
    @Query("SELECT * FROM shots WHERE player_id = :playerId AND shot_id = :shotId LIMIT 1")
    suspend fun getShot(playerId: Int, shotId: Int): ShotEntity?

    /** Clears all shots for a player before re-inserting on re-fetch. */
    @Query("DELETE FROM shots WHERE player_id = :playerId")
    suspend fun clearShotsForPlayer(playerId: Int)
}