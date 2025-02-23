package com.lchristmann.tracker

class LocationRepository(private val locationDao: LocationDao) {

    suspend fun insertLocation(location: LocationEntity) {
        locationDao.insertLocation(location)
    }

}
