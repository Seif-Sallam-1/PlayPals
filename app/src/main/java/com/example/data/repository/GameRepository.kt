// File: app/src/main/java/com/example/data/repository/GameRepository.kt
package com.example.data.repository

import android.util.Log
import com.example.data.model.GameRoom
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface GameRepository {
    suspend fun createRoom(hostId: String, hostName: String): Result<String>
    suspend fun joinRoom(roomId: String, playerId: String, playerName: String): Result<Unit>
    fun observeRoom(roomId: String): Flow<GameRoom?>
    fun observeAvailableRooms(): Flow<List<GameRoom>>
    suspend fun updateRoom(roomId: String, room: GameRoom): Result<Unit>
    suspend fun leaveRoom(roomId: String, playerId: String): Result<Unit>
}

class FirebaseGameRepository : GameRepository {
    private val database = try {
        val app = com.google.firebase.FirebaseApp.getInstance()
        val url = app.options.databaseUrl?.takeIf { it.isNotEmpty() && !it.contains("fake-project") } 
            ?: "https://playpals-e7b51-default-rtdb.europe-west1.firebasedatabase.app/"
        FirebaseDatabase.getInstance(app, url)
    } catch (e: Throwable) {
        Log.e("FirebaseGameRepository", "Failed to retrieve FirebaseDatabase instance", e)
        null
    }
    private val roomsRef = database?.getReference("rooms")

    override suspend fun createRoom(hostId: String, hostName: String): Result<String> {
        val ref = roomsRef ?: return Result.failure(Exception("Multiplayer service is currently unavailable."))
        return try {
            val roomId = generateRoomId()
            val newRoom = GameRoom(
                id = roomId,
                playerXId = hostId,
                playerXName = hostName,
                turnPlayerId = hostId, // Host starts
                board = List(9) { "" },
                status = "WAITING"
            )
            ref.child(roomId).setValue(newRoom).await()
            Result.success(roomId)
        } catch (e: Throwable) {
            Log.e("GameRepository", "Failed to create room", e)
            Result.failure(Exception(e))
        }
    }

    override suspend fun joinRoom(roomId: String, playerId: String, playerName: String): Result<Unit> {
        val ref = roomsRef ?: return Result.failure(Exception("Multiplayer service is currently unavailable."))
        return try {
            val snapshot = ref.child(roomId).get().await()
            if (!snapshot.exists()) {
                throw Exception("Room $roomId does not exist.")
            }
            val room = snapshot.getValue(GameRoom::class.java) ?: throw Exception("Failed to parse room data.")
            
            if (room.playerXId == playerId) {
                // Rejoining as host, that's fine
                return Result.success(Unit)
            }
            
            if (room.playerOId.isNotEmpty() && room.playerOId != playerId) {
                throw Exception("Room $roomId is already full.")
            }

            // Join as Player O
            val updatedRoom = room.copy(
                playerOId = playerId,
                playerOName = playerName,
                status = "PLAYING" // Room is now active!
            )
            ref.child(roomId).setValue(updatedRoom).await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e("GameRepository", "Failed to join room", e)
            Result.failure(Exception(e))
        }
    }

    override fun observeRoom(roomId: String): Flow<GameRoom?> = callbackFlow {
        val ref = roomsRef
        if (ref == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(GameRoom::class.java)
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.child(roomId).addValueEventListener(listener)
        awaitClose {
            ref.child(roomId).removeEventListener(listener)
        }
    }

    override fun observeAvailableRooms(): Flow<List<GameRoom>> = callbackFlow {
        val ref = roomsRef
        if (ref == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = mutableListOf<GameRoom>()
                for (child in snapshot.children) {
                    val room = child.getValue(GameRoom::class.java)
                    if (room != null && room.status == "WAITING" && room.playerOId.isEmpty()) {
                        rooms.add(room)
                    }
                }
                trySend(rooms)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    override suspend fun updateRoom(roomId: String, room: GameRoom): Result<Unit> {
        val ref = roomsRef ?: return Result.failure(Exception("Multiplayer service is currently unavailable."))
        return try {
            ref.child(roomId).setValue(room).await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e("GameRepository", "Failed to update room", e)
            Result.failure(Exception(e))
        }
    }

    override suspend fun leaveRoom(roomId: String, playerId: String): Result<Unit> {
        val ref = roomsRef ?: return Result.failure(Exception("Multiplayer service is currently unavailable."))
        return try {
            val snapshot = ref.child(roomId).get().await()
            if (snapshot.exists()) {
                val room = snapshot.getValue(GameRoom::class.java)
                if (room != null) {
                    if (room.playerXId == playerId) {
                        // Host left - set room status to ABANDONED or remove it
                        if (room.playerOId.isEmpty()) {
                            ref.child(roomId).removeValue().await()
                        } else {
                            ref.child(roomId).setValue(room.copy(status = "ABANDONED")).await()
                        }
                    } else if (room.playerOId == playerId) {
                        // Guest left
                        ref.child(roomId).setValue(room.copy(status = "ABANDONED")).await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e("GameRepository", "Failed to leave room", e)
            Result.failure(Exception(e))
        }
    }

    private fun generateRoomId(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..5)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
