package com.play.golf.perf.tracker.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sealed type representing the connectivity state of the device.
 */
sealed class NetworkStatus {
    /** Device has an active internet-capable network. */
    object Available : NetworkStatus()

    /** Device has no usable network connection. */
    object Unavailable : NetworkStatus()
}

/**
 * Observes real-time network connectivity changes using [ConnectivityManager] callbacks
 * wrapped in a [callbackFlow]. The flow is hot for as long as it has collectors.
 *
 * Inject this as a [Singleton] — one shared callback registration for the whole app.
 */
@Singleton
class NetworkObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Emits [NetworkStatus.Available] when connectivity is gained and
     * [NetworkStatus.Unavailable] when it is lost. Emits the current
     * state immediately on collection.
     *
     * Uses [distinctUntilChanged] to avoid duplicate emissions when the
     * underlying network switches (e.g. WiFi → mobile) without an interruption.
     */
    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("NetworkObserver: network available — $network")
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                Timber.d("NetworkObserver: network lost — $network")
                trySend(NetworkStatus.Unavailable)
            }

            override fun onUnavailable() {
                Timber.d("NetworkObserver: network unavailable")
                trySend(NetworkStatus.Unavailable)
            }
        }

        // Emit the current connectivity state immediately so collectors
        // don't have to wait for the next change event
        val currentStatus = getCurrentNetworkStatus()
        Timber.d("NetworkObserver: initial status — $currentStatus")
        trySend(currentStatus)

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Unregister when the flow is cancelled (no more collectors)
        awaitClose {
            Timber.d("NetworkObserver: unregistering network callback")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * Synchronously checks the current network state.
     * Used for the immediate emission on flow collection.
     */
    fun getCurrentNetworkStatus(): NetworkStatus {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus.Unavailable
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkStatus.Unavailable

        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            NetworkStatus.Available
        } else {
            NetworkStatus.Unavailable
        }
    }

    /** Convenience property for one-shot checks (non-reactive). */
    val isConnected: Boolean
        get() = getCurrentNetworkStatus() == NetworkStatus.Available
}