package com.play.golf.perf.tracker.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.play.golf.perf.tracker.presentation.playerdetail.PlayerDetailScreen
import com.play.golf.perf.tracker.presentation.playerlist.PlayerListScreen
import com.play.golf.perf.tracker.presentation.shotdetail.ShotDetailScreen

// ── Sealed Route Definitions ──────────────────────────────────────────────────

/**
 * Sealed class hierarchy representing every screen in the golf nav graph.
 * Route strings are the single source of truth — used in both the
 * [NavGraphBuilder] and the [NavHostController] navigate() calls.
 */
sealed class GolfScreen(val route: String) {

    /** Screen 1 — Player list with search and filter. */
    object PlayerList : GolfScreen(route = "player_list")

    /** Screen 2 — Full player detail with stats and shot list. */
    object PlayerDetail : GolfScreen(
        route = "player_detail/{${GolfNavArgs.PLAYER_ID}}"
    ) {
        /**
         * Builds the resolved navigation route string with the actual [playerId].
         * Usage: navController.navigate(GolfScreen.PlayerDetail.createRoute(player.id))
         */
        fun createRoute(playerId: Int): String =
            "player_detail/$playerId"
    }

    /** Screen 3 — Full shot metrics detail. */
    object ShotDetail : GolfScreen(
        route = "shot_detail/{${GolfNavArgs.PLAYER_ID}}/{${GolfNavArgs.SHOT_ID}}"
    ) {
        /**
         * Builds the resolved navigation route string with [playerId] and [shotId].
         * Usage: navController.navigate(GolfScreen.ShotDetail.createRoute(playerId, shotId))
         */
        fun createRoute(playerId: Int, shotId: Int): String =
            "shot_detail/$playerId/$shotId"
    }
}

// ── Graph Route Constants ─────────────────────────────────────────────────────

object GolfGraph {
    const val ROOT  = "golf_root_graph"
    const val GOLF  = "golf_graph"
}

// ── NavGraphBuilder Extension ─────────────────────────────────────────────────

/**
 * Registers the entire golf navigation sub-graph.
 * Called from [RootNavGraph] — keeps nav logic modular and testable.
 *
 * @param navController      shared nav controller from [RootNavGraph]
 * @param onToggleTheme      propagated from [MainViewModel] to Screen 1 top bar
 * @param isDarkTheme        current theme state — drives the toggle icon on Screen 1
 */
fun NavGraphBuilder.golfNavGraph(
    navController: NavHostController,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
) {
    navigation(
        route            = GolfGraph.GOLF,
        startDestination = GolfScreen.PlayerList.route,
    ) {

        // ── Screen 1 — Player List ────────────────────────────────────────────
        composable(route = GolfScreen.PlayerList.route) {
            PlayerListScreen(
                onPlayerClick = { playerId ->
                    navController.navigate(
                        GolfScreen.PlayerDetail.createRoute(playerId)
                    )
                },
                onToggleTheme = onToggleTheme,
                isDarkTheme   = isDarkTheme,
            )
        }

        // ── Screen 2 — Player Detail ──────────────────────────────────────────
        composable(
            route     = GolfScreen.PlayerDetail.route,
            arguments = listOf(
                navArgument(GolfNavArgs.PLAYER_ID) {
                    type = NavType.IntType
                }
            ),
        ) {
            PlayerDetailScreen(
                onBackClick = { navController.popBackStack() },
                onShotClick = { playerId, shotId ->
                    navController.navigate(
                        GolfScreen.ShotDetail.createRoute(playerId, shotId)
                    )
                },
            )
        }

        // ── Screen 3 — Shot Detail ────────────────────────────────────────────
        composable(
            route     = GolfScreen.ShotDetail.route,
            arguments = listOf(
                navArgument(GolfNavArgs.PLAYER_ID) {
                    type = NavType.IntType
                },
                navArgument(GolfNavArgs.SHOT_ID) {
                    type = NavType.IntType
                },
            ),
        ) {
            ShotDetailScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}