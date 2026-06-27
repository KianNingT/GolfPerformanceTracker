package com.play.golf.perf.tracker.domain.model

/**
 * Domain model for an individual shot.
 * Nested inside [PlayerDetail.shots] and displayed on Screen 2 (summary row)
 * and Screen 3 (full detail).
 */
data class Shot(
    val shotId: Int,
    val playerId: Int,
    val club: String,
    val ballSpeed: Double,
    val launchAngle: Double,
    val distance: Double,
    val spinRate: Int,
    val carryDistance: Double,
    val peakHeight: Double,
    val landingAngle: Double
)