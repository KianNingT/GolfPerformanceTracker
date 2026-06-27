package com.play.golf.perf.tracker.presentation.playerlist

/**
 * Holds all non-paging UI state for the Player List screen.
 * Paging data is kept separately as a [Flow<PagingData<Player>>]
 * in the ViewModel to avoid restarting the pager on every state change.
 */
data class PlayerListUiState(
    /** Available club types for the filter chip row — populated from Room cache. */
    val availableClubs: List<String> = emptyList(),

    /** Currently active name search query. */
    val searchQuery: String = "",

    /** Currently active club filter — empty string means "All". */
    val selectedClub: String = "",

    /** True while the initial/refresh network call is in flight. */
    val isRefreshing: Boolean = false,

    /** Non-null when a refresh error should be shown in the banner. */
    val errorMessage: String? = null,

    /** True when the device has no internet — drives the offline banner. */
    val isOffline: Boolean = false,
)