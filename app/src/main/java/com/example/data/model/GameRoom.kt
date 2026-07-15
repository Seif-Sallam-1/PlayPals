// File: app/src/main/java/com/example/data/model/GameRoom.kt
package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GameRoom(
    val id: String = "",
    val playerXId: String = "",
    val playerXName: String = "",
    val playerOId: String = "",
    val playerOName: String = "",
    val turnPlayerId: String = "", // UID of whose turn it is
    val board: List<String> = List(9) { "" }, // 9 cells
    val status: String = "WAITING", // "WAITING", "PLAYING", "WON_X", "WON_O", "DRAW", "ABANDONED"
    val winnerName: String = ""
) {
    // Convenience constructor for Firebase Realtime Database deserialization
    constructor() : this("", "", "", "", "", "", List(9) { "" }, "WAITING", "")
}
