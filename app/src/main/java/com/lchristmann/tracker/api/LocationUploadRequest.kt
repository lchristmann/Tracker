package com.lchristmann.tracker.api

data class LocationUploadRequest(
    val timestamp: String,
    val latitude: Double,
    val longitude: Double
)
