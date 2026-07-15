// File: app/src/main/java/com/example/ui/screens/xo/MatchmakingViewModel.kt
package com.example.ui.screens.xo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.GameRoom
import com.example.data.repository.AuthRepository
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatchmakingUiState(
    val roomCodeInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinedRoomId: String? = null
)

class MatchmakingViewModel(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchmakingUiState())
    val uiState: StateFlow<MatchmakingUiState> = _uiState.asStateFlow()

    // Real-time flow of open rooms waiting for an opponent
    val availableRooms: StateFlow<List<GameRoom>> = gameRepository.observeAvailableRooms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onRoomCodeInputChanged(value: String) {
        if (value.length <= 5) {
            _uiState.value = _uiState.value.copy(roomCodeInput = value.uppercase(), error = null)
        }
    }

    fun clearNavigation() {
        _uiState.value = _uiState.value.copy(joinedRoomId = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun createRoom() {
        val userId = authRepository.currentUser?.uid ?: "anonymous"
        val userName = authRepository.currentUsername

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            gameRepository.createRoom(userId, userName)
                .onSuccess { roomId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        joinedRoomId = roomId
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "Failed to create game room."
                    )
                }
        }
    }

    fun joinRoomByCode() {
        val roomId = _uiState.value.roomCodeInput
        if (roomId.length != 5) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid 5-digit room code.")
            return
        }

        performJoin(roomId)
    }

    fun joinRoomDirect(roomId: String) {
        performJoin(roomId)
    }

    private fun performJoin(roomId: String) {
        val userId = authRepository.currentUser?.uid ?: "anonymous"
        val userName = authRepository.currentUsername

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            gameRepository.joinRoom(roomId, userId, userName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        joinedRoomId = roomId
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "Could not join game room."
                    )
                }
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            gameRepository: GameRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MatchmakingViewModel(authRepository, gameRepository) as T
            }
        }
    }
}
