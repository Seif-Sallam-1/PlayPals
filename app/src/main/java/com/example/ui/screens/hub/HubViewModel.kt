// File: app/src/main/java/com/example/ui/screens/hub/HubViewModel.kt
package com.example.ui.screens.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.AuthRepository

class HubViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val username: String
        get() = authRepository.currentUsername

    fun signOut() {
        authRepository.signOut()
    }

    companion object {
        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HubViewModel(authRepository) as T
                }
            }
    }
}
