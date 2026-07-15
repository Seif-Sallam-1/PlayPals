// File: app/src/main/java/com/example/ui/screens/xo/OfflineXoViewModel.kt
package com.example.ui.screens.xo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OfflineXoViewModel : ViewModel() {

    private val _board = MutableStateFlow(List(9) { "" })
    val board: StateFlow<List<String>> = _board.asStateFlow()

    private val _isXTurn = MutableStateFlow(true)
    val isXTurn: StateFlow<Boolean> = _isXTurn.asStateFlow()

    private val _winner = MutableStateFlow<String?>(null) // "X", "O", "DRAW", or null
    val winner: StateFlow<String?> = _winner.asStateFlow()

    private val _winningLine = MutableStateFlow<List<Int>?>(null) // Indices of the winning combination
    val winningLine: StateFlow<List<Int>?> = _winningLine.asStateFlow()

    private val _scoreX = MutableStateFlow(0)
    val scoreX: StateFlow<Int> = _scoreX.asStateFlow()

    private val _scoreO = MutableStateFlow(0)
    val scoreO: StateFlow<Int> = _scoreO.asStateFlow()

    private val _scoreDraws = MutableStateFlow(0)
    val scoreDraws: StateFlow<Int> = _scoreDraws.asStateFlow()

    fun makeMove(index: Int) {
        // Ignore move if cell is taken or game is already over
        if (_board.value[index].isNotEmpty() || _winner.value != null) {
            return
        }

        val currentTurnSymbol = if (_isXTurn.value) "X" else "O"
        val newBoard = _board.value.toMutableList()
        newBoard[index] = currentTurnSymbol
        _board.value = newBoard

        // Check if this move won
        val winCombination = checkWinner(newBoard)
        if (winCombination != null) {
            _winner.value = currentTurnSymbol
            _winningLine.value = winCombination
            if (currentTurnSymbol == "X") {
                _scoreX.value += 1
            } else {
                _scoreO.value += 1
            }
        } else if (newBoard.none { it.isEmpty() }) {
            // No empty cells left: It's a draw!
            _winner.value = "DRAW"
            _scoreDraws.value += 1
        } else {
            // Switch turn
            _isXTurn.value = !_isXTurn.value
        }
    }

    fun resetBoard() {
        _board.value = List(9) { "" }
        _isXTurn.value = true
        _winner.value = null
        _winningLine.value = null
    }

    fun resetScores() {
        resetBoard()
        _scoreX.value = 0
        _scoreO.value = 0
        _scoreDraws.value = 0
    }

    private fun checkWinner(b: List<String>): List<Int>? {
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
}
