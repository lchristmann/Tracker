package com.lchristmann.tracker

data class LocationUploadRequest(
    val timestamp: String,
    val latitude: Double,
    val longitude: Double
)
