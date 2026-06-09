package cc.harmonizerlabs.app.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import cc.harmonizerlabs.app.model.HarmonizerMode
import cc.harmonizerlabs.app.ui.SharedTrackViewModel
import cc.harmonizerlabs.app.ui.player.PlayerScreen
import cc.harmonizerlabs.app.ui.processing.ProcessingScreen
import cc.harmonizerlabs.app.ui.upload.UploadScreen

sealed class Screen(val route: String) {
    object Upload     : Screen("upload")
    object Processing : Screen("processing/{trackId}/{modeKey}") {
        fun createRoute(trackId: String, modeKey: String) = "processing/$trackId/$modeKey"
    }
    object Player     : Screen("player")
}

@Composable
fun HarmonizerNavGraph(
    navController: NavHostController = rememberNavController(),
    sharedVm: SharedTrackViewModel = hiltViewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Upload.route,
    ) {

        // ── Upload ─────────────────────────────────────────────────────────────
        composable(Screen.Upload.route) {
            UploadScreen(
                onTrackReady = { trackId, mode ->
                    navController.navigate(Screen.Processing.createRoute(trackId, mode.key)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ── Processing / analysis loader ───────────────────────────────────────
        composable(
            route = Screen.Processing.route,
            arguments = listOf(
                navArgument("trackId") { type = NavType.StringType },
                navArgument("modeKey") { type = NavType.StringType },
            )
        ) { backEntry ->
            val trackId = backEntry.arguments?.getString("trackId") ?: ""
            val modeKey = backEntry.arguments?.getString("modeKey") ?: "canon"
            val mode    = HarmonizerMode.entries.firstOrNull { it.key == modeKey } ?: HarmonizerMode.CANON

            ProcessingScreen(
                trackId = trackId,
                onReady = { trackData ->
                    sharedVm.setTrack(trackData, mode)
                    navController.navigate(Screen.Player.route) {
                        popUpTo(Screen.Upload.route)
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Player ─────────────────────────────────────────────────────────────
        composable(Screen.Player.route) {
            val track by sharedVm.track.collectAsState()
            val mode  by sharedVm.mode.collectAsState()

            track?.let { td ->
                PlayerScreen(
                    track       = td,
                    initialMode = mode,
                    onBack = {
                        navController.navigate(Screen.Upload.route) {
                            popUpTo(Screen.Upload.route) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
