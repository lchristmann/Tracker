package com.lchristmann.tracker.data

class LocationRepository(private val locationDao: LocationDao) {

    suspend fun insertLocation(location: LocationEntity) {
        locationDao.insertLocation(location)
    }

    suspend fun getUnsyncedLocations(): List<LocationEntity> {
        return locationDao.getUnsyncedLocations()
    }

    suspend fun markLocationSynced(id: Int) {
        locationDao.markLocationSynced(id)
    }

    suspend fun getLastFiftyLocationsDesc(): List<LocationEntity> {
        return locationDao.getLastFiftyLocationsDesc()
    }

}
