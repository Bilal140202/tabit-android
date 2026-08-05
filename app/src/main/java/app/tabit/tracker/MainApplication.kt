package app.tabit.tracker

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        try {
            super.onCreate()
        } catch (e: Exception) {
            Log.e("Tabit", "Failed to initialize application", e)
        }
    }
}
