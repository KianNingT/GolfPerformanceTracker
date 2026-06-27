package com.play.golf.perf.tracker.data.remote.api

import com.play.golf.perf.tracker.data.remote.dto.PlayerDetailResponseDto
import com.play.golf.perf.tracker.data.remote.dto.PlayerListResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GolfApiService {

    /**
     * Fetches the full list of player summaries.
     * Endpoint: GET https://kianningt.github.io/golf-app-api/players.json
     */
    @GET("players.json")
    suspend fun getPlayers(): Response<PlayerListResponseDto>

    /**
     * Fetches the full detail of a single player including their shots.
     * Endpoint: GET https://kianningt.github.io/golf-app-api/players/{id}.json
     *
     * @param id the player's numeric ID (1–25)
     */
    @GET("players/{id}.json")
    suspend fun getPlayerDetail(
        @Path("id") id: Int
    ): Response<PlayerDetailResponseDto>
}