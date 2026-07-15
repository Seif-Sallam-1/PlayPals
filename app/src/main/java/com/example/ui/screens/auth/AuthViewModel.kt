// File: app/src/main/java/com/example/ui/screens/auth/AuthViewModel.kt
package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val needsEmailVerification: Boolean = false
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onUsernameChanged(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isSignUpMode = !_uiState.value.isSignUpMode,
            errorMessage = null,
            username = "",
            needsEmailVerification = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun performAuth() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email and Password cannot be empty.")
            return
        }
        
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 6 characters.")
            return
        }

        if (state.isSignUpMode && state.username.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username is required for sign up.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            if (state.isSignUpMode) {
                authRepository.signUpWithEmail(state.email, state.username, state.password)
                    .onSuccess { user ->
                        // Send verification email automatically
                        authRepository.sendEmailVerification()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            needsEmailVerification = true,
                            errorMessage = "Account created! A verification link has been sent to ${state.email}. Please verify your email to log in."
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Sign up failed."
                        )
                    }
            } else {
                authRepository.signInWithEmail(state.email, state.password)
                    .onSuccess { user ->
                        if (user.isEmailVerified) {
                            _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                needsEmailVerification = true,
                                errorMessage = "Please verify your email address to log in. We sent a verification link to your email."
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Login failed."
                        )
                    }
            }
        }
    }

    fun sendVerificationEmail() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            authRepository.sendEmailVerification()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "A new verification link has been sent to your inbox!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to send verification link: ${error.localizedMessage}"
                    )
                }
        }
    }

    fun checkEmailVerificationStatus() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            authRepository.reloadUser()
                .onSuccess { user ->
                    if (user.isEmailVerified) {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, errorMessage = null)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Your email is still not verified. Please check your inbox and click the verification link, then click check status again."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to refresh verification status: ${error.localizedMessage}"
                    )
                }
        }
    }

    fun cancelVerificationFlow() {
        authRepository.signOut()
        _uiState.value = _uiState.value.copy(
            needsEmailVerification = false,
            errorMessage = null,
            isSuccess = false
        )
    }

    fun signInWithGoogleToken(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            authRepository.signInWithGoogleToken(idToken)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Google Sign-In failed."
                    )
                }
        }
    }

    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }

    // Factory pattern for providing the ViewModel with dependencies
    companion object {
        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
    }
}
