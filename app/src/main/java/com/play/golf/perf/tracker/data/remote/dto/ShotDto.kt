package com.play.golf.perf.tracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShotDto(
    @Json(name = "shotId")
    val shotId: Int,
    @Json(name = "club")
    val club: String,
    @Json(name = "ballSpeed")
    val ballSpeed: Double,
    @Json(name = "launchAngle")
    val launchAngle: Double,
    @Json(name = "distance")
    val distance: Double,
    @Json(name = "spinRate")
    val spinRate: Int,
    @Json(name = "carryDistance")
    val carryDistance: Double,
    @Json(name = "peakHeight")
    val peakHeight: Double,
    @Json(name = "landingAngle")
    val landingAngle: Double
)