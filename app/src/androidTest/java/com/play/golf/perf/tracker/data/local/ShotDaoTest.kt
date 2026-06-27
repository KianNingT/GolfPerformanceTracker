package com.play.golf.perf.tracker.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.data.local.dao.PlayerDao
import com.play.golf.perf.tracker.data.local.dao.ShotDao
import com.play.golf.perf.tracker.data.local.entity.PlayerDetailEntity
import com.play.golf.perf.tracker.data.local.entity.ShotEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShotDaoTest {

    private lateinit var database: GolfDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var shotDao: ShotDao

    // Test Fixtures

    private fun playerDetailEntity(id: Int) = PlayerDetailEntity(
        id                 = id,
        name               = "Player $id",
        country            = "USA",
        club               = "Driver",
        avatarUrl          = null,
        averageSpeed       = 180.0,
        averageDistance    = 280.0,
        totalShots         = 300,
        bio                = null,
        age                = 30,
        turnsProYear       = 2005,
        majorWins          = 2,
        totalWins          = 10,
        scoringAverage     = 69.5,
        greensInRegulation = 67.0,
        drivingAccuracy    = 63.0,
        puttingAverage     = 1.79,
    )

    private fun shotEntity(
        playerId: Int,
        shotId: Int,
        club: String      = "Driver",
        ballSpeed: Double = 180.0,
        distance: Double  = 280.0,
    ) = ShotEntity(
        playerId      = playerId,
        shotId        = shotId,
        club          = club,
        ballSpeed     = ballSpeed,
        launchAngle   = 10.5,
        distance      = distance,
        spinRate      = 2700,
        carryDistance = 265.0,
        peakHeight    = 95.0,
        landingAngle  = 38.5,
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GolfDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playerDao = database.playerDao()
        shotDao   = database.shotDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // Insert & Query

    @Test
    fun insertShots_and_getShotsForPlayer_returns_all_shots_for_player() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        shotDao.insertShots(
            listOf(
                shotEntity(playerId = 1, shotId = 1),
                shotEntity(playerId = 1, shotId = 2),
                shotEntity(playerId = 1, shotId = 3),
            )
        )

        val shots = shotDao.getShotsForPlayer(playerId = 1)

        assertThat(shots).hasSize(3)
    }

    @Test
    fun getShotsForPlayer_returns_shots_ordered_by_shotId_ascending() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        shotDao.insertShots(
            listOf(
                shotEntity(playerId = 1, shotId = 3),
                shotEntity(playerId = 1, shotId = 1),
                shotEntity(playerId = 1, shotId = 2),
            )
        )

        val shots = shotDao.getShotsForPlayer(playerId = 1)

        assertThat(shots[0].shotId).isEqualTo(1)
        assertThat(shots[1].shotId).isEqualTo(2)
        assertThat(shots[2].shotId).isEqualTo(3)
    }

    @Test
    fun getShotsForPlayer_only_returns_shots_for_specified_player() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        playerDao.insertPlayerDetail(playerDetailEntity(2))

        shotDao.insertShots(
            listOf(
                shotEntity(playerId = 1, shotId = 1),
                shotEntity(playerId = 1, shotId = 2),
                shotEntity(playerId = 2, shotId = 1),
            )
        )

        val player1Shots = shotDao.getShotsForPlayer(playerId = 1)
        val player2Shots = shotDao.getShotsForPlayer(playerId = 2)

        assertThat(player1Shots).hasSize(2)
        assertThat(player2Shots).hasSize(1)
    }

    @Test
    fun getShotsForPlayer_returns_empty_list_when_no_shots_exist() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))

        val shots = shotDao.getShotsForPlayer(playerId = 1)

        assertThat(shots).isEmpty()
    }

    @Test
    fun getShotsForPlayer_returns_empty_list_for_unknown_player() = runTest {
        val shots = shotDao.getShotsForPlayer(playerId = 999)
        assertThat(shots).isEmpty()
    }

    // Get Single Shot

    @Test
    fun getShot_returns_correct_shot_by_playerId_and_shotId() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        shotDao.insertShots(
            listOf(
                shotEntity(playerId = 1, shotId = 1, club = "Driver"),
                shotEntity(playerId = 1, shotId = 2, club = "7-Iron"),
                shotEntity(playerId = 1, shotId = 3, club = "5-Iron"),
            )
        )

        val shot = shotDao.getShot(playerId = 1, shotId = 2)

        assertThat(shot).isNotNull()
        assertThat(shot?.shotId).isEqualTo(2)
        assertThat(shot?.club).isEqualTo("7-Iron")
    }

    @Test
    fun getShot_returns_null_when_shotId_not_found() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        shotDao.insertShots(listOf(shotEntity(playerId = 1, shotId = 1)))

        val shot = shotDao.getShot(playerId = 1, shotId = 99)

        assertThat(shot).isNull()
    }

    @Test
    fun getShot_returns_null_when_playerId_not_found() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        shotDao.insertShots(listOf(shotEntity(playerId = 1, shotId = 1)))

        val shot = shotDao.getShot(playerId = 99, shotId = 1)

        assertThat(shot).isNull()
    }

    @Test
    fun getShot_returns_correct_metrics() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        shotDao.insertShots(
            listOf(
                shotEntity(
                    playerId  = 1,
                    shotId    = 1,
                    club      = "7-Iron",
                    ballSpeed = 128.3,
                    distance  = 178.4,
                )
            )
        )

        val shot = shotDao.getShot(playerId = 1, shotId = 1)

        assertThat(shot?.ballSpeed).isEqualTo(128.3)
        assertThat(shot?.distance).isEqualTo(178.4)
    }

    // Clear Shots

    @Test
    fun clearShotsForPlayer_removes_only_that_players_shots() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        playerDao.insertPlayerDetail(playerDetailEntity(2))

        shotDao.insertShots(
            listOf(
                shotEntity(playerId = 1, shotId = 1),
                shotEntity(playerId = 1, shotId = 2),
                shotEntity(playerId = 2, shotId = 1),
            )
        )

        shotDao.clearShotsForPlayer(playerId = 1)

        assertThat(shotDao.getShotsForPlayer(playerId = 1)).isEmpty()
        assertThat(shotDao.getShotsForPlayer(playerId = 2)).hasSize(1)
    }

    @Test
    fun clearShotsForPlayer_on_empty_table_does_not_throw() = runTest {
        // Should not throw even when nothing to delete
        shotDao.clearShotsForPlayer(playerId = 1)
        assertThat(shotDao.getShotsForPlayer(playerId = 1)).isEmpty()
    }

    // Foreign Key Cascade

    @Test
    fun deleting_player_detail_cascades_and_removes_all_shots() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))
        shotDao.insertShots(
            listOf(
                shotEntity(playerId = 1, shotId = 1),
                shotEntity(playerId = 1, shotId = 2),
            )
        )

        // Verify shots exist before deletion
        assertThat(shotDao.getShotsForPlayer(1)).hasSize(2)

        // Delete player detail — CASCADE should remove shots automatically
        playerDao.clearPlayerDetail(1)

        assertThat(shotDao.getShotsForPlayer(1)).isEmpty()
    }

    // Transaction, insertPlayerDetailWithShots

    @Test
    fun insertPlayerDetailWithShots_atomically_inserts_detail_and_shots() = runTest {
        val detail = playerDetailEntity(1)
        val shots  = listOf(
            shotEntity(playerId = 1, shotId = 1),
            shotEntity(playerId = 1, shotId = 2),
        )

        playerDao.insertPlayerDetailWithShots(
            playerDetail = detail,
            insertShots  = { shotDao.insertShots(shots) }
        )

        assertThat(playerDao.getPlayerDetail(1)).isNotNull()
        assertThat(shotDao.getShotsForPlayer(1)).hasSize(2)
    }

    @Test
    fun re_inserting_shots_after_clear_replaces_stale_data() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1))

        // First sync
        shotDao.insertShots(listOf(shotEntity(playerId = 1, shotId = 1, ballSpeed = 180.0)))

        // Simulate re-fetch — clear then re-insert with updated data
        shotDao.clearShotsForPlayer(1)
        shotDao.insertShots(listOf(shotEntity(playerId = 1, shotId = 1, ballSpeed = 185.0)))

        val shot = shotDao.getShot(playerId = 1, shotId = 1)
        assertThat(shot?.ballSpeed).isEqualTo(185.0)
        assertThat(shotDao.getShotsForPlayer(1)).hasSize(1)
    }
}