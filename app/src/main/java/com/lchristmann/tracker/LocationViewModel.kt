package com.lchristmann.tracker

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LocationViewModel: ViewModel() {
    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    private val _isTracking = mutableStateOf(false)
    val isTracking: State<Boolean> = _isTracking

    fun updateLocation(newLocation: LocationData) {
        _location.value = newLocation
    }

    fun startTracking() {
        _isTracking.value = true
    }

    fun stopTracking() {
        _isTracking.value = false
    }
}