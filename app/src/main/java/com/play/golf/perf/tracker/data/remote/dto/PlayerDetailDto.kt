package com.play.golf.perf.tracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerDetailDto(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "country")
    val country: String,
    @Json(name = "club")
    val club: String,
    @Json(name = "avatarUrl")
    val avatarUrl: String?,
    @Json(name = "averageSpeed")
    val averageSpeed: Double,
    @Json(name = "averageDistance")
    val averageDistance: Double,
    @Json(name = "totalShots")
    val totalShots: Int,
    @Json(name = "bio")
    val bio: String?,
    @Json(name = "age")
    val age: Int?,
    @Json(name = "turnsProYear")
    val turnsProYear: Int?,
    @Json(name = "majorWins")
    val majorWins: Int?,
    @Json(name = "totalWins")
    val totalWins: Int?,
    @Json(name = "scoringAverage")
    val scoringAverage: Double?,
    @Json(name = "greensInRegulation")
    val greensInRegulation: Double?,
    @Json(name = "drivingAccuracy")
    val drivingAccuracy: Double?,
    @Json(name = "puttingAverage")
    val puttingAverage: Double?,
    @Json(name = "shots")
    val shots: List<ShotDto>
)