package com.lchristmann.tracker

// Converts LocationData (UI Model) to LocationEntity (Database Model)
fun LocationData.toEntity(): LocationEntity {
    return LocationEntity(
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = System.currentTimeMillis()
    )
}

// Converts LocationEntity (Database Model) to LocationData (UI Model)
fun LocationEntity.toData(): LocationData {
    return LocationData(
        latitude = this.latitude,
        longitude = this.longitude
    )
}