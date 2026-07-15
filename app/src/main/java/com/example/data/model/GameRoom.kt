// File: app/src/main/java/com/example/data/model/GameRoom.kt
package com.example.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GameRoom(
    var id: String = "",
    var playerXId: String = "",
    var playerXName: String = "",
    var playerOId: String = "",
    var playerOName: String = "",
    var turnPlayerId: String = "", // UID of whose turn it is
    var board: List<String> = List(9) { "" }, // 9 cells
    var status: String = "WAITING", // "WAITING", "PLAYING", "WON_X", "WON_O", "DRAW", "ABANDONED"
    var winnerName: String = ""
) {
    // Convenience constructor for Firebase Realtime Database deserialization
    constructor() : this("", "", "", "", "", "", List(9) { "" }, "WAITING", "")
}
