package com.play.golf.perf.tracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.play.golf.perf.tracker.presentation.main.MainViewModel

/**
 * Root navigation host for GolfPerfTracker.
 *
 * Owns the [NavHost] and wires sub-graphs into it.
 * App-level state ([isDarkTheme], [networkStatus]) is read here from
 * [MainViewModel] and passed down to screens that need it — keeping
 * screens themselves free of direct ViewModel cross-dependencies.
 *
 * Pattern mirrors the [RootNavigationGraph] from your existing codebase.
 */
@Composable
fun RootNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
) {
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsStateWithLifecycle()

    NavHost(
        navController    = navController,
        route            = GolfGraph.ROOT,
        startDestination = GolfGraph.GOLF,
    ) {
        golfNavGraph(
            navController = navController,
            onToggleTheme = mainViewModel::toggleTheme,
            isDarkTheme   = isDarkTheme,
        )
    }
}