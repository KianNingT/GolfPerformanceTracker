package com.play.golf.perf.tracker.domain.model

/**
 * Domain model for a player summary.
 * Consumed by [GetPlayersUseCase] and displayed on the Player List screen.
 * Decoupled from both the DTO and the Room entity.
 */
data class Player(
    val id: Int,
    val name: String,
    val country: String,
    val club: String,
    val avatarUrl: String?,
    val averageSpeed: Double,
    val averageDistance: Double,
    val totalShots: Int
)