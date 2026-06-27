package com.play.golf.perf.tracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerDto(
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
    val totalShots: Int
)