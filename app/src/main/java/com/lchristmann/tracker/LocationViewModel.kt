package com.lchristmann.tracker

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(application: Application): AndroidViewModel(application) {
    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    private val _isTracking = mutableStateOf(false)
    val isTracking: State<Boolean> = _isTracking

    // Initialize Repository
    private val locationRepository = LocationRepository(
        LocationDatabase.getDatabase(application).locationDao()
    )


    fun updateLocation(newLocation: LocationData) {
        _location.value = newLocation
        saveLocation(newLocation) // Save to database
    }

    // Save Location to Database
    private fun saveLocation(locationData: LocationData) {
        // Explanation: https://stackoverflow.com/questions/55974539/viewmodelscope-launchdispatchers-io-purpose
        viewModelScope.launch(Dispatchers.IO) {
            locationRepository.insertLocation(locationData.toEntity())
        }
    }

    fun startTracking() {
        _isTracking.value = true
    }

    fun stopTracking() {
        _isTracking.value = false
    }
}