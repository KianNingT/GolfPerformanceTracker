package com.play.golf.perf.tracker.navigation

/**
 * Centralised nav argument key constants.
 * Referenced by both the nav graph route definitions and
 * [SavedStateHandle] reads in ViewModels — keeping the keys
 * in one place prevents typo mismatches.
 */
object GolfNavArgs {
    const val PLAYER_ID = "playerId"
    const val SHOT_ID   = "shotId"
}