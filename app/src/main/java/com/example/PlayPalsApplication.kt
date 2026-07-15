// File: app/src/main/java/com/example/PlayPalsApplication.kt
package com.example

import android.app.Application
import android.util.Log
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class PlayPalsApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        // Gracefully initialize Firebase programmatically if google-services.json is missing or incomplete
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyCs9HkAd64T6qtFlyrbSpNniXizAjGaSVQ")
                    .setApplicationId("1:532352985723:android:dd0758eb7c8dc29f7d50de")
                    .setDatabaseUrl("https://playpals-e7b51-default-rtdb.europe-west1.firebasedatabase.app/")
                    .setProjectId("playpals-e7b51")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.i("PlayPalsApplication", "FirebaseApp initialized programmatically with production credentials.")
            }
        } catch (e: Exception) {
            Log.e("PlayPalsApplication", "Failed to initialize FirebaseApp programmatically", e)
        }

        container = DefaultAppContainer(this)
    }
}

