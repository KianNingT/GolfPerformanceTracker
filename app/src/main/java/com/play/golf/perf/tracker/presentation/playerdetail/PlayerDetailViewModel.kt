package com.play.golf.perf.tracker.presentation.playerdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.core.network.NetworkObserver
import com.play.golf.perf.tracker.core.network.NetworkStatus
import com.play.golf.perf.tracker.domain.usecase.GetPlayerDetailUseCase
import com.play.golf.perf.tracker.navigation.GolfNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    private val getPlayerDetailUseCase: GetPlayerDetailUseCase,
    private val networkObserver: NetworkObserver,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Read playerId from nav args via SavedStateHandle — survives process death
    private val playerId: Int = checkNotNull(savedStateHandle[GolfNavArgs.PLAYER_ID]) {
        "PlayerDetailViewModel requires a playerId nav argument"
    }

    private val _uiState = MutableStateFlow(PlayerDetailUiState())
    val uiState: StateFlow<PlayerDetailUiState> = _uiState.asStateFlow()

    init {
        observeNetwork()
        loadPlayerDetail()
    }

    // Network Observer

    private fun observeNetwork() {
        networkObserver.networkStatus
            .onEach { status ->
                val isOffline = status == NetworkStatus.Unavailable
                _uiState.update { it.copy(isOffline = isOffline) }

                // Re-fetch from network when connectivity is restored
                if (status == NetworkStatus.Available &&
                    _uiState.value.playerDetail != null
                ) {
                    Timber.d("PlayerDetailViewModel: network restored — refreshing player $playerId")
                    loadPlayerDetail(forceRefresh = true)
                }
            }
            .launchIn(viewModelScope)
    }

    // Data Loading

    /**
     * Loads player detail — serves cache first then refreshes from network.
     * [forceRefresh] bypasses cache entirely (used on pull-to-refresh and
     * network reconnection).
     */
    fun loadPlayerDetail(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            getPlayerDetailUseCase(
                playerId     = playerId,
                forceRefresh = forceRefresh
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading    = it.playerDetail == null,
                                isRefreshing = it.playerDetail != null,
                                hasError     = false,
                                errorMessage = null,
                            )
                        }
                    }

                    is Resource.Success -> {
                        Timber.d(
                            "PlayerDetailViewModel: loaded player $playerId — " +
                                    "${resource.data?.shots?.size} shots"
                        )
                        _uiState.update {
                            it.copy(
                                playerDetail = resource.data,
                                isLoading    = false,
                                isRefreshing = false,
                                hasError     = false,
                                errorMessage = null,
                            )
                        }
                    }

                    is Resource.Error -> {
                        Timber.w(
                            "null%s", resource.message
                        )
                        _uiState.update {
                            it.copy(
                                isLoading           = false,
                                isRefreshing        = false,
                                hasError            = it.playerDetail == null,
                                errorMessage        = resource.message,
                                isConnectivityError = resource.isConnectivityError,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onRefresh() {
        loadPlayerDetail(forceRefresh = true)
    }

    fun toggleStatsExpanded() {
        _uiState.update { it.copy(isStatsExpanded = !it.isStatsExpanded) }
    }

    fun dismissError() {
        _uiState.update { it.copy(hasError = false, errorMessage = null) }
    }
}