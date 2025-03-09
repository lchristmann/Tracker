package com.lchristmann.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationsViewModel(application: Application) : AndroidViewModel(application) {

    private val _locations = MutableLiveData<List<LocationEntity>>(emptyList())
    val locations: LiveData<List<LocationEntity>> = _locations

    private val locationRepository: LocationRepository

    init {
        val database = LocationDatabase.getDatabase(application)
        locationRepository = LocationRepository(database.locationDao())
    }

    fun fetchLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            val locations = locationRepository.getLastFiftyLocationsDesc()
            withContext(Dispatchers.Main) {
                _locations.value = locations
            }
        }
    }
}

