// File: app/src/main/java/com/example/navigation/Screen.kt
package com.example.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object MainHub : Screen("main_hub")
    object XoOffline : Screen("xo_offline")
    object MatchmakingLobby : Screen("matchmaking_lobby")
    object XoOnline : Screen("xo_online/{roomId}") {
        fun createRoute(roomId: String): String = "xo_online/$roomId"
    }
}
