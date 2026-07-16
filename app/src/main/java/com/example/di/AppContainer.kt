// File: app/src/main/java/com/example/di/AppContainer.kt
package com.example.di

import android.content.Context
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.GameRepository
import com.example.data.repository.FirebaseGameRepository

interface AppContainer {
    val authRepository: AuthRepository
    val gameRepository: GameRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(context)
    }
    
    override val gameRepository: GameRepository by lazy {
        FirebaseGameRepository()
    }
}
