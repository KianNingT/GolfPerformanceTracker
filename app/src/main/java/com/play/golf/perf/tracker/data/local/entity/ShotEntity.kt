package com.play.golf.perf.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached shot data — maps to the [shots] table.
 * Each shot belongs to a player via [playerId].
 * Foreign key references [player_details.id] — shots are only cached
 * after the parent player detail has been fetched.
 */
@Entity(
    tableName = "shots",
    foreignKeys = [
        ForeignKey(
            entity        = PlayerDetailEntity::class,
            parentColumns = ["id"],
            childColumns  = ["player_id"],
            onDelete      = ForeignKey.CASCADE // delete shots when player detail is evicted
        )
    ],
    indices = [Index(value = ["player_id"])]
)
data class ShotEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk")
    val pk: Int = 0,

    /** Foreign key — links this shot to its parent player. */
    @ColumnInfo(name = "player_id")
    val playerId: Int,

    /** Original shotId from the API — used for display and navigation. */
    @ColumnInfo(name = "shot_id")
    val shotId: Int,

    @ColumnInfo(name = "club")
    val club: String,

    @ColumnInfo(name = "ball_speed")
    val ballSpeed: Double,

    @ColumnInfo(name = "launch_angle")
    val launchAngle: Double,

    @ColumnInfo(name = "distance")
    val distance: Double,

    @ColumnInfo(name = "spin_rate")
    val spinRate: Int,

    @ColumnInfo(name = "carry_distance")
    val carryDistance: Double,

    @ColumnInfo(name = "peak_height")
    val peakHeight: Double,

    @ColumnInfo(name = "landing_angle")
    val landingAngle: Double
)