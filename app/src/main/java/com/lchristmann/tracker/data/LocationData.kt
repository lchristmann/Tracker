package com.lchristmann.tracker.data

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

// Converts LocationData (UI Model) to LocationEntity (Database Model)
fun LocationData.toEntity(): LocationEntity {
    return LocationEntity(
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = System.currentTimeMillis()
    )
}