// File: app/src/main/java/com/example/data/repository/AuthRepository.kt
package com.example.data.repository

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val currentUser: FirebaseUser?
    val authState: StateFlow<FirebaseUser?>
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun signUpWithEmail(email: String, username: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogleToken(idToken: String): Result<FirebaseUser>
    fun signOut()
    val currentUsername: String
}

class FirebaseAuthRepository : AuthRepository {
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.e("FirebaseAuthRepository", "Failed to retrieve FirebaseAuth instance", e)
        null
    }
    
    private val _authState = MutableStateFlow<FirebaseUser?>(firebaseAuth?.currentUser)
    override val authState: StateFlow<FirebaseUser?> = _authState.asStateFlow()

    init {
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                _authState.value = auth.currentUser
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Failed to set up AuthStateListener", e)
        }
    }

    private fun checkDemoMode(): Boolean {
        return try {
            val app = FirebaseApp.getInstance()
            app.options.apiKey == "AIzaSyFakeKeyForLocalFallbackPurposeOnly"
        } catch (e: Exception) {
            false
        }
    }

    private val demoModeError = "Firebase is currently in demo mode with fallback local credentials. Please click 'Play Offline as Guest' below to start playing, or upload your real 'google-services.json' file to the project to enable full online multiplayer features!"

    override val currentUser: FirebaseUser?
        get() = firebaseAuth?.currentUser

    override val currentUsername: String
        get() = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Player"

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Authentication service is currently unavailable."))
        if (checkDemoMode()) {
            return Result.failure(Exception(demoModeError))
        }
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null after successful login.")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, username: String, password: String): Result<FirebaseUser> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Authentication service is currently unavailable."))
        if (checkDemoMode()) {
            return Result.failure(Exception(demoModeError))
        }
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null after successful registration.")
            
            // Save username to displayName
            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }
            user.updateProfile(profileUpdates).await()
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleToken(idToken: String): Result<FirebaseUser> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Authentication service is currently unavailable."))
        if (checkDemoMode()) {
            return Result.failure(Exception(demoModeError))
        }
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("User is null after successful Google login.")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google sign in failed", e)
            Result.failure(e)
        }
    }

    override fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Failed to sign out", e)
        }
    }
}
