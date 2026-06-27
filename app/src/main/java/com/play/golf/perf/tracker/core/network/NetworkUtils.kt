package com.play.golf.perf.tracker.core.network

import java.net.UnknownHostException

/**
 * Returns true when the [Throwable] represents a connectivity failure
 * (no internet, DNS resolution failure, or refused connection).
 */
fun Throwable.isConnectivityError(): Boolean {
    return this is UnknownHostException || this is java.net.ConnectException
}