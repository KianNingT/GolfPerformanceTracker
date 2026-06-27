package com.play.golf.perf.tracker.presentation.playerdetail

import com.play.golf.perf.tracker.domain.model.PlayerDetail

/**
 * Holds all UI state for the Player Detail screen (Screen 2).
 * [playerDetail] is null only during initial load or on error.
 */
data class PlayerDetailUiState(
    val playerDetail: PlayerDetail? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val isConnectivityError: Boolean = false,
    val isOffline: Boolean = false,
    /** Controls the animated expansion of the stats section on Screen 2. */
    val isStatsExpanded: Boolean = true,
)