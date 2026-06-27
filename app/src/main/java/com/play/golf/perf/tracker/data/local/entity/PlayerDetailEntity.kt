package com.play.golf.perf.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached player detail — maps to the [player_details] table.
 * Populated from [PlayerDetailDto] when a player detail page is opened.
 * Shots are stored separately in [ShotEntity] with a foreign key on [playerId].
 */
@Entity(tableName = "player_details")
data class PlayerDetailEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "country")
    val country: String,

    @ColumnInfo(name = "club")
    val club: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,

    @ColumnInfo(name = "average_speed")
    val averageSpeed: Double,

    @ColumnInfo(name = "average_distance")
    val averageDistance: Double,

    @ColumnInfo(name = "total_shots")
    val totalShots: Int,

    @ColumnInfo(name = "bio")
    val bio: String?,

    @ColumnInfo(name = "age")
    val age: Int?,

    @ColumnInfo(name = "turns_pro_year")
    val turnsProYear: Int?,

    @ColumnInfo(name = "major_wins")
    val majorWins: Int?,

    @ColumnInfo(name = "total_wins")
    val totalWins: Int?,

    @ColumnInfo(name = "scoring_average")
    val scoringAverage: Double?,

    @ColumnInfo(name = "greens_in_regulation")
    val greensInRegulation: Double?,

    @ColumnInfo(name = "driving_accuracy")
    val drivingAccuracy: Double?,

    @ColumnInfo(name = "putting_average")
    val puttingAverage: Double?,

    /** Unix epoch ms of when this row was last synced from the API. */
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long = System.currentTimeMillis()
)