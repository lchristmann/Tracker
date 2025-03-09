package com.lchristmann.tracker

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class TrackingViewModel(application: Application): AndroidViewModel(application) {

    private val _isTracking = mutableStateOf(false)
    val isTracking: State<Boolean> = _isTracking

    init {
        // Load tracking state from SharedPreferences when ViewModel is created
        val sharedPref = getApplication<Application>()
            .getSharedPreferences("tracking_prefs", MODE_PRIVATE)
        val tracking = sharedPref.getBoolean("isTracking", false)
        _isTracking.value = tracking
    }

    fun startTracking() {
        _isTracking.value = true
        persistTrackingState(true)
    }

    fun stopTracking() {
        _isTracking.value = false
        persistTrackingState(false)
    }

    private fun persistTrackingState(isTracking: Boolean) {
        val sharedPref = getApplication<Application>()
            .getSharedPreferences("tracking_prefs", MODE_PRIVATE)
        sharedPref.edit().putBoolean("isTracking", isTracking).apply()
    }
}