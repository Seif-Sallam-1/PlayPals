// File: app/src/main/java/com/example/navigation/NavGraph.kt
package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.hub.MainHubScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.xo.MatchmakingLobbyScreen
import com.example.ui.screens.xo.XoOfflineScreen
import com.example.ui.screens.xo.XoOnlineScreen

@Composable
fun PlayPalsNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Welcome Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Firebase Authentication Screen
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.MainHub.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Casual Game Hub Screen
        composable(Screen.MainHub.route) {
            MainHubScreen(
                onNavigateToXoOffline = {
                    navController.navigate(Screen.XoOffline.route)
                },
                onNavigateToMatchmaking = {
                    navController.navigate(Screen.MatchmakingLobby.route)
                },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.MainHub.route) { inclusive = true }
                    }
                }
            )
        }

        // Offline Pass-and-Play Tic-Tac-Toe
        composable(Screen.XoOffline.route) {
            XoOfflineScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Online Matchmaking Room Lobby
        composable(Screen.MatchmakingLobby.route) {
            MatchmakingLobbyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onJoinRoom = { roomId ->
                    navController.navigate(Screen.XoOnline.createRoute(roomId))
                }
            )
        }

        // Online Sync Play Screen
        composable(
            route = Screen.XoOnline.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: "00000"
            XoOnlineScreen(
                roomId = roomId,
                onNavigateBack = {
                    navController.navigate(Screen.MainHub.route) {
                        popUpTo(Screen.MainHub.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
