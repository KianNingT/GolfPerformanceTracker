package com.play.golf.perf.tracker.data.local

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.play.golf.perf.tracker.data.local.dao.PlayerDao
import com.play.golf.perf.tracker.data.local.dao.ShotDao
import com.play.golf.perf.tracker.data.local.entity.PlayerDetailEntity
import com.play.golf.perf.tracker.data.local.entity.PlayerEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerDaoTest {

    private lateinit var database: GolfDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var shotDao: ShotDao

    // Test Fixtures

    private fun playerEntity(
        id: Int,
        name: String,
        club: String = "Driver",
        country: String = "USA",
        averageSpeed: Double = 180.0,
        averageDistance: Double = 280.0,
        totalShots: Int = 300,
    ) = PlayerEntity(
        id              = id,
        name            = name,
        country         = country,
        club            = club,
        avatarUrl       = null,
        averageSpeed    = averageSpeed,
        averageDistance = averageDistance,
        totalShots      = totalShots,
    )

    private fun playerDetailEntity(id: Int, name: String) = PlayerDetailEntity(
        id                 = id,
        name               = name,
        country            = "USA",
        club               = "Driver",
        avatarUrl          = null,
        averageSpeed       = 182.4,
        averageDistance    = 285.6,
        totalShots         = 320,
        bio                = "Test bio",
        age                = 35,
        turnsProYear       = 2005,
        majorWins          = 3,
        totalWins          = 15,
        scoringAverage     = 69.5,
        greensInRegulation = 68.0,
        drivingAccuracy    = 64.0,
        puttingAverage     = 1.78,
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

    // Insert & Query Players

    @Test
    fun insertPlayers_and_getPagedPlayers_returns_all_rows() = runTest {
        val players = listOf(
            playerEntity(1, "Tiger Woods"),
            playerEntity(2, "Rory McIlroy"),
            playerEntity(3, "Jon Rahm"),
        )
        playerDao.insertPlayers(players)

        val pagingSource = playerDao.getPagedPlayers()
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).hasSize(3)
    }

    @Test
    fun insertPlayers_results_are_ordered_by_name_ascending() = runTest {
        playerDao.insertPlayers(
            listOf(
                playerEntity(3, "Tiger Woods"),
                playerEntity(1, "Rory McIlroy"),
                playerEntity(2, "Jon Rahm"),
            )
        )

        val pagingSource = playerDao.getPagedPlayers()
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data[0].name).isEqualTo("Jon Rahm")
        assertThat(result.data[1].name).isEqualTo("Rory McIlroy")
        assertThat(result.data[2].name).isEqualTo("Tiger Woods")
    }

    @Test
    fun insertPlayers_replaces_existing_row_with_same_id() = runTest {
        playerDao.insertPlayers(listOf(playerEntity(1, "Tiger Woods")))
        playerDao.insertPlayers(listOf(playerEntity(1, "Tiger Woods Updated")))

        val pagingSource = playerDao.getPagedPlayers()
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).hasSize(1)
        assertThat(result.data[0].name).isEqualTo("Tiger Woods Updated")
    }

    // Search & Filter

    @Test
    fun searchPagedPlayers_by_name_returns_matching_players() = runTest {
        playerDao.insertPlayers(
            listOf(
                playerEntity(1, "Tiger Woods",  club = "Driver"),
                playerEntity(2, "Rory McIlroy", club = "Driver"),
                playerEntity(3, "Jon Rahm",     club = "3-Iron"),
            )
        )

        val pagingSource = playerDao.searchPagedPlayers(nameQuery = "Tiger", clubFilter = "")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).hasSize(1)
        assertThat(result.data[0].name).isEqualTo("Tiger Woods")
    }

    @Test
    fun searchPagedPlayers_by_club_returns_only_matching_club() = runTest {
        playerDao.insertPlayers(
            listOf(
                playerEntity(1, "Tiger Woods",  club = "Driver"),
                playerEntity(2, "Rory McIlroy", club = "Driver"),
                playerEntity(3, "Jon Rahm",     club = "3-Iron"),
            )
        )

        val pagingSource = playerDao.searchPagedPlayers(nameQuery = "", clubFilter = "3-Iron")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).hasSize(1)
        assertThat(result.data[0].club).isEqualTo("3-Iron")
    }

    @Test
    fun searchPagedPlayers_with_both_name_and_club_applies_both_filters() = runTest {
        playerDao.insertPlayers(
            listOf(
                playerEntity(1, "Tiger Woods",    club = "Driver"),
                playerEntity(2, "Rory McIlroy",   club = "Driver"),
                playerEntity(3, "Jon Rahm",        club = "3-Iron"),
                playerEntity(4, "Patrick Cantlay", club = "Driver"),
            )
        )

        // Only "Driver" players whose name contains "Rory"
        val pagingSource = playerDao.searchPagedPlayers(nameQuery = "Rory", clubFilter = "Driver")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).hasSize(1)
        assertThat(result.data[0].name).isEqualTo("Rory McIlroy")
    }

    @Test
    fun searchPagedPlayers_with_no_match_returns_empty_list() = runTest {
        playerDao.insertPlayers(listOf(playerEntity(1, "Tiger Woods")))

        val pagingSource = playerDao.searchPagedPlayers(nameQuery = "zzz", clubFilter = "")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).isEmpty()
    }

    @Test
    fun searchPagedPlayers_is_case_insensitive() = runTest {
        playerDao.insertPlayers(listOf(playerEntity(1, "Tiger Woods")))

        val pagingSource = playerDao.searchPagedPlayers(nameQuery = "tiger", clubFilter = "")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).hasSize(1)
    }

    // Distinct Clubs

    @Test
    fun getDistinctClubs_returns_unique_club_types() = runTest {
        playerDao.insertPlayers(
            listOf(
                playerEntity(1, "Player A", club = "Driver"),
                playerEntity(2, "Player B", club = "Driver"),
                playerEntity(3, "Player C", club = "3-Iron"),
                playerEntity(4, "Player D", club = "5-Iron"),
            )
        )

        val clubs = playerDao.getDistinctClubs()

        assertThat(clubs).containsExactly("3-Iron", "5-Iron", "Driver").inOrder()
    }

    @Test
    fun getDistinctClubs_returns_empty_list_when_table_empty() = runTest {
        val clubs = playerDao.getDistinctClubs()
        assertThat(clubs).isEmpty()
    }

    // Player Count

    @Test
    fun getPlayerCount_returns_correct_count_after_insert() = runTest {
        playerDao.insertPlayers(
            listOf(
                playerEntity(1, "Player A"),
                playerEntity(2, "Player B"),
            )
        )

        assertThat(playerDao.getPlayerCount()).isEqualTo(2)
    }

    @Test
    fun getPlayerCount_returns_zero_on_empty_table() = runTest {
        assertThat(playerDao.getPlayerCount()).isEqualTo(0)
    }

    // Clear

    @Test
    fun clearPlayers_removes_all_rows() = runTest {
        playerDao.insertPlayers(
            listOf(
                playerEntity(1, "Tiger Woods"),
                playerEntity(2, "Rory McIlroy"),
            )
        )
        playerDao.clearPlayers()

        assertThat(playerDao.getPlayerCount()).isEqualTo(0)
    }

    // Player Detail

    @Test
    fun insertPlayerDetail_and_getPlayerDetail_returns_correct_entity() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1, "Tiger Woods"))

        val detail = playerDao.getPlayerDetail(1)

        assertThat(detail).isNotNull()
        assertThat(detail?.name).isEqualTo("Tiger Woods")
        assertThat(detail?.majorWins).isEqualTo(3)
    }

    @Test
    fun getPlayerDetail_returns_null_when_not_found() = runTest {
        val detail = playerDao.getPlayerDetail(999)
        assertThat(detail).isNull()
    }

    @Test
    fun insertPlayerDetail_replaces_existing_on_conflict() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1, "Tiger Woods"))
        playerDao.insertPlayerDetail(playerDetailEntity(1, "Tiger Woods Updated"))

        val detail = playerDao.getPlayerDetail(1)
        assertThat(detail?.name).isEqualTo("Tiger Woods Updated")
    }

    @Test
    fun clearPlayerDetail_removes_specific_player() = runTest {
        playerDao.insertPlayerDetail(playerDetailEntity(1, "Tiger Woods"))
        playerDao.insertPlayerDetail(playerDetailEntity(2, "Rory McIlroy"))

        playerDao.clearPlayerDetail(1)

        assertThat(playerDao.getPlayerDetail(1)).isNull()
        assertThat(playerDao.getPlayerDetail(2)).isNotNull()
    }
}