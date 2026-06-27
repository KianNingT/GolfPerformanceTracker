package com.play.golf.perf.tracker.presentation.shotdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.golf.perf.tracker.domain.usecase.GetShotDetailUseCase
import com.play.golf.perf.tracker.navigation.GolfNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShotDetailViewModel @Inject constructor(
    private val getShotDetailUseCase: GetShotDetailUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Both playerId and shotId are passed as nav args
    private val playerId: Int = checkNotNull(savedStateHandle[GolfNavArgs.PLAYER_ID]) {
        "ShotDetailViewModel requires a playerId nav argument"
    }
    private val shotId: Int = checkNotNull(savedStateHandle[GolfNavArgs.SHOT_ID]) {
        "ShotDetailViewModel requires a shotId nav argument"
    }

    private val _uiState = MutableStateFlow(ShotDetailUiState())
    val uiState: StateFlow<ShotDetailUiState> = _uiState.asStateFlow()

    init {
        loadShot()
    }

    /**
     * Reads the shot from Room cache.
     * No network call needed — Screen 2 guarantees the cache is warm
     * before this screen is ever reachable via navigation.
     */
    private fun loadShot() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val shot = getShotDetailUseCase(playerId = playerId, shotId = shotId)

            if (shot != null) {
                Timber.d(
                    "ShotDetailViewModel: loaded shot $shotId " +
                            "for player $playerId — club=${shot.club}"
                )
                _uiState.update {
                    it.copy(
                        shot      = shot,
                        isLoading = false,
                        hasError  = false,
                    )
                }
            } else {
                Timber.w(
                    "ShotDetailViewModel: shot $shotId not found " +
                            "in cache for player $playerId"
                )
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        hasError     = true,
                        errorMessage = "Shot data not available. Please go back and try again.",
                    )
                }
            }
        }
    }
}