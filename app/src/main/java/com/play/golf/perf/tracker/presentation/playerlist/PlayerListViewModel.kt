package com.play.golf.perf.tracker.presentation.playerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.play.golf.perf.tracker.core.common.Resource
import com.play.golf.perf.tracker.core.network.NetworkObserver
import com.play.golf.perf.tracker.core.network.NetworkStatus
import com.play.golf.perf.tracker.domain.model.Player
import com.play.golf.perf.tracker.domain.usecase.GetPlayersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerListViewModel @Inject constructor(
    private val getPlayersUseCase: GetPlayersUseCase,
    private val networkObserver: NetworkObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerListUiState())
    val uiState: StateFlow<PlayerListUiState> = _uiState.asStateFlow()

    // Internal search and filter state flows — drive pager recomposition
    private val _searchQuery = MutableStateFlow("")
    private val _selectedClub = MutableStateFlow("")


    /**
     * Paged player flow — restarted automatically whenever search query
     * or club filter changes via [flatMapLatest].
     *
     * [debounce] on the search query avoids hammering Room on every keystroke.
     * [cachedIn] survives configuration changes without restarting the pager.
     */
    val pagedPlayers: Flow<PagingData<Player>> = combine(
        _searchQuery.debounce(300L).distinctUntilChanged(),
        _selectedClub
    ) { query, club -> Pair(query, club) }
        .flatMapLatest { (query, club) ->
            Timber.d("PlayerListViewModel: pager restarted — query='$query' club='$club'")
            getPlayersUseCase(nameQuery = query, clubFilter = club)
        }
        .cachedIn(viewModelScope)


    init {
        observeNetwork()
        refreshPlayers()
    }

    // ── Network Observer ──────────────────────────────────────────────────────

    /**
     * Observes connectivity changes.
     * Auto-refreshes the player list when the network comes back online
     * so the cache is always up to date after an offline period.
     */
    private fun observeNetwork() {
        networkObserver.networkStatus
            .onEach { status ->
                val isOffline = status == NetworkStatus.Unavailable
                _uiState.update { it.copy(isOffline = isOffline) }

                if (status == NetworkStatus.Available) {
                    Timber.d("PlayerListViewModel: network restored — triggering refresh")
                    refreshPlayers()
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Data Loading ──────────────────────────────────────────────────────────

    /**
     * Triggers a remote refresh of the player list.
     * On success, also refreshes the distinct club list for filter chips.
     */
    fun refreshPlayers() {
        viewModelScope.launch {
            getPlayersUseCase.refresh()
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(isRefreshing = true, errorMessage = null)
                            }
                        }

                        is Resource.Success -> {
                            Timber.d("PlayerListViewModel: refresh succeeded")
                            loadDistinctClubs()
                            _uiState.update {
                                it.copy(isRefreshing = false, errorMessage = null)
                            }
                        }

                        is Resource.Error -> {
                            Timber.w("PlayerListViewModel: refresh failed — ${resource.message}")
                            // Load clubs from cache even on network error
                            loadDistinctClubs()
                            _uiState.update {
                                it.copy(
                                    isRefreshing = false,
                                    errorMessage = if (resource.isConnectivityError) null
                                    else resource.message
                                )
                            }
                        }
                    }
                }
        }
    }

    private suspend fun loadDistinctClubs() {
        val clubs = getPlayersUseCase.getDistinctClubs()
        Timber.d("PlayerListViewModel: loaded ${clubs.size} distinct clubs")
        _uiState.update { it.copy(availableClubs = clubs) }
    }

    // ── Search & Filter ───────────────────────────────────────────────────────

    /**
     * Called on every keystroke from the SearchBar.
     * Updates both the internal pager-driving flow and the UI state.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Called when a club filter chip is tapped.
     * Tapping the active chip clears the filter (toggle behaviour).
     */
    fun onClubFilterSelected(club: String) {
        val newClub = if (_selectedClub.value == club) "" else club
        Timber.d("PlayerListViewModel: club filter → '$newClub'")
        _selectedClub.value = newClub
        _uiState.update { it.copy(selectedClub = newClub) }
    }

    /** Clears both search query and club filter. */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedClub.value = ""
        _uiState.update { it.copy(searchQuery = "", selectedClub = "") }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}