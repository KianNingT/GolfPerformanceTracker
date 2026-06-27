package com.play.golf.perf.tracker.presentation.shotdetail

import com.play.golf.perf.tracker.domain.model.Shot

/**
 * Holds all UI state for the Shot Detail screen (Screen 3).
 * [shot] is null only during initial load or when the cache lookup fails.
 */
data class ShotDetailUiState(
    val shot: Shot? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
)