// File: app/src/main/java/com/example/ui/screens/xo/OnlineXoViewModel.kt
package com.example.ui.screens.xo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.GameRoom
import com.example.data.repository.AuthRepository
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnlineXoViewModel(
    private val roomId: String,
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _roomState = MutableStateFlow<GameRoom?>(null)
    val roomState: StateFlow<GameRoom?> = _roomState.asStateFlow()

    private val _isLeaving = MutableStateFlow(false)
    val isLeaving: StateFlow<Boolean> = _isLeaving.asStateFlow()

    val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            gameRepository.observeRoom(roomId).collect { room ->
                _roomState.value = room
            }
        }
    }

    val isPlayerX: Boolean
        get() = _roomState.value?.playerXId == currentUserId

    val isPlayerO: Boolean
        get() = _roomState.value?.playerOId == currentUserId

    val isMyTurn: Boolean
        get() {
            val room = _roomState.value ?: return false
            return room.status == "PLAYING" && room.turnPlayerId == currentUserId
        }

    val playerSymbol: String
        get() = when {
            isPlayerX -> "X"
            isPlayerO -> "O"
            else -> ""
        }

    val opponentName: String
        get() {
            val room = _roomState.value ?: return "Opponent"
            return if (isPlayerX) {
                if (room.playerOName.isEmpty()) "Waiting..." else room.playerOName
            } else {
                room.playerXName
            }
        }

    fun makeMove(cellIndex: Int) {
        val room = _roomState.value ?: return
        if (room.board[cellIndex].isNotEmpty() || !isMyTurn) return

        val mySymbol = playerSymbol
        val updatedBoard = room.board.toMutableList()
        updatedBoard[cellIndex] = mySymbol

        // Calculate win combination / game outcome
        val winComb = calculateWin(updatedBoard)
        var nextStatus = "PLAYING"
        var nextTurn = if (isPlayerX) room.playerOId else room.playerXId
        var winnerName = ""

        if (winComb != null) {
            nextStatus = if (isPlayerX) "WON_X" else "WON_O"
            winnerName = if (isPlayerX) room.playerXName else room.playerOName
            nextTurn = "" // Game over
        } else if (updatedBoard.none { it.isEmpty() }) {
            nextStatus = "DRAW"
            nextTurn = "" // Game over
        }

        val updatedRoom = room.copy(
            board = updatedBoard,
            status = nextStatus,
            turnPlayerId = nextTurn,
            winnerName = winnerName
        )

        viewModelScope.launch {
            gameRepository.updateRoom(roomId, updatedRoom)
        }
    }

    fun rematch() {
        val room = _roomState.value ?: return
        
        // Reset room state for a new game
        val updatedRoom = room.copy(
            board = List(9) { "" },
            status = "PLAYING",
            turnPlayerId = room.playerXId, // Host starts first in rematches
            winnerName = ""
        )

        viewModelScope.launch {
            gameRepository.updateRoom(roomId, updatedRoom)
        }
    }

    fun leaveMatch() {
        if (_isLeaving.value) return
        _isLeaving.value = true
        
        viewModelScope.launch {
            gameRepository.leaveRoom(roomId, currentUserId)
        }
    }

    private fun calculateWin(b: List<String>): List<Int>? {
        val winLines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Cols
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (line in winLines) {
            if (b[line[0]].isNotEmpty() && b[line[0]] == b[line[1]] && b[line[0]] == b[line[2]]) {
                return line
            }
        }
        return null
    }

    companion object {
        fun provideFactory(
            roomId: String,
            authRepository: AuthRepository,
            gameRepository: GameRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OnlineXoViewModel(roomId, authRepository, gameRepository) as T
            }
        }
    }
}
