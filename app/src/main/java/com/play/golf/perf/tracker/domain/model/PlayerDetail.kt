package com.play.golf.perf.tracker.domain.model

/**
 * Domain model for full player detail.
 * Consumed by [GetPlayerDetailUseCase] and displayed on the Player Detail screen.
 * Contains the nested [shots] list for display on Screen 2 and navigation to Screen 3.
 */
data class PlayerDetail(
    val id: Int,
    val name: String,
    val country: String,
    val club: String,
    val avatarUrl: String?,
    val averageSpeed: Double,
    val averageDistance: Double,
    val totalShots: Int,
    val bio: String?,
    val age: Int?,
    val turnsProYear: Int?,
    val majorWins: Int?,
    val totalWins: Int?,
    val scoringAverage: Double?,
    val greensInRegulation: Double?,
    val drivingAccuracy: Double?,
    val puttingAverage: Double?,
    val shots: List<Shot>
)