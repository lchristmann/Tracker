package com.lchristmann.tracker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        Log.d("LocationWorker", "doWork started")

        // Check location permission
        val locationUtils = LocationUtils()
        if (!locationUtils.hasLocationPermission(applicationContext)) {
            Log.d("LocationWorker", "Location permission not granted, failing work.")
            return Result.failure()
        }

        // Get the FusedLocationProviderClient
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)

        // Retrieve the last known location as a suspend function
        val location = suspendCoroutine { cont ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { cont.resume(null) }
        }

        return if (location != null) {
            Log.d("LocationWorker", "Location retrieved: ${location.latitude}, ${location.longitude}")
            val locationData = LocationData(
                latitude = location.latitude,
                longitude = location.longitude
            )
            // Save location to the database
            val db = LocationDatabase.getDatabase(applicationContext)
            val repository = LocationRepository(db.locationDao())
            repository.insertLocation(locationData.toEntity())

            Log.d("LocationWorker", "Location saved to database")
            Result.success()
        } else {
            // If location is null, you might want to retry later
            Log.d("LocationWorker", "Location is null, retrying")
            Result.retry()
        }
    }

}