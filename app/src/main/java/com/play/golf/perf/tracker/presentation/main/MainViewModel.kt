package com.play.golf.perf.tracker.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.golf.perf.tracker.core.datastore.ThemeDataStore
import com.play.golf.perf.tracker.core.network.NetworkObserver
import com.play.golf.perf.tracker.core.network.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val themeDataStore: ThemeDataStore,
    val networkObserver: NetworkObserver,
) : ViewModel() {

    /**
     * Persisted dark theme preference.
     * Collected in [MainActivity] and passed down to [GolfPerfTrackerTheme].
     */
    val isDarkTheme: StateFlow<Boolean> = themeDataStore.isDarkTheme
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.Eagerly,
            initialValue  = false
        )

    /**
     * Real-time network connectivity state.
     * Collected in [MainActivity] and passed to screens that need to react
     * to connectivity changes (e.g. auto-refresh player list on reconnect).
     */
    val networkStatus: StateFlow<NetworkStatus> = networkObserver.networkStatus
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Eagerly,
            initialValue = NetworkStatus.Unavailable
        )

    /** Toggles and persists the dark/light theme preference. */
    fun toggleTheme() {
        viewModelScope.launch {
            val newValue = !isDarkTheme.value
            Timber.d("MainViewModel: toggling theme → isDark=$newValue")
            themeDataStore.setDarkTheme(newValue)
        }
    }
}