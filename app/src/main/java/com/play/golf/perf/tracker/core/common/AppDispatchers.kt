package com.play.golf.perf.tracker.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the standard [Dispatchers] into an injectable class so that
 * ViewModels and repositories can swap in test dispatchers in unit tests
 * without touching production code.
 */
@Singleton
class AppDispatchers @Inject constructor() {
    /** CPU-bound work — ViewModel logic, mapping, sorting, filtering. */
    val default: CoroutineDispatcher = Dispatchers.Default

    /** I/O-bound work — Room queries, DataStore reads, file access. */
    val io: CoroutineDispatcher = Dispatchers.IO

    /** Main thread — UI updates, StateFlow emissions collected by Compose. */
    val main: CoroutineDispatcher = Dispatchers.Main

    /** Main thread, immediate — avoids frame delay when already on main. */
    val mainImmediate: CoroutineDispatcher = Dispatchers.Main.immediate
}