package com.play.golf.perf.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached player summary — maps to the [players] table.
 * Populated from [PlayerDto] when the player list is fetched.
 * Used as the single source of truth for Screen 1 (Player List + Search).
 */
@Entity(tableName = "players")
data class PlayerEntity(
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

    /** Unix epoch ms of when this row was last synced from the API. */
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long = System.currentTimeMillis()
)