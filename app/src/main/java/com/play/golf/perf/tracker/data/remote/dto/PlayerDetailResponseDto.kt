package com.play.golf.perf.tracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerDetailResponseDto(
    @Json(name = "data")
    val data: PlayerDetailDto,
    @Json(name = "message")
    val message: String?,
    @Json(name = "timestamp")
    val timestamp: String?
)