package com.play.golf.perf.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.play.golf.perf.tracker.navigation.RootNavGraph
import com.play.golf.perf.tracker.presentation.main.MainViewModel
import com.play.golf.perf.tracker.ui.theme.GolfPerfTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity — hosts the entire Compose navigation tree.
 * Responsibilities:
 *  - Install the splash screen
 *  - Enable edge-to-edge rendering
 *  - Read the persisted theme preference and apply [GolfPerfTrackerTheme]
 *  - Create the [NavHostController] and hand it to [RootNavGraph]
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate() to attach the splash screen
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel = hiltViewModel<MainViewModel>()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            GolfPerfTrackerTheme(darkTheme = isDarkTheme) {
                RootNavGraph(
                    navController = navController,
                    mainViewModel = mainViewModel,
                )
            }
        }
    }
}