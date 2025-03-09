package com.lchristmann.tracker

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.lchristmann.tracker.api.LocationUploadRequest
import com.lchristmann.tracker.api.TrackerApiService
import com.lchristmann.tracker.data.LocationData
import com.lchristmann.tracker.data.LocationDatabase
import com.lchristmann.tracker.data.LocationRepository
import com.lchristmann.tracker.data.toEntity
import com.lchristmann.tracker.utils.LocationUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationWorker(
    context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context, workerParams) {

    private val locationUtils = LocationUtils()
    private val db = LocationDatabase.getDatabase(applicationContext)
    private val repository = LocationRepository(db.locationDao())

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        Log.d("LocationWorker", "doWork started")

        // Check location permission
        if (!locationUtils.hasLocationPermission(applicationContext) || !locationUtils.hasBackgroundLocationPermission(applicationContext)) {
            Log.d("LocationWorker", "Location permission not granted, failing work.")
            return Result.failure()
        }

        // Get the last known location
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
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
            // Save location to the database (the toEntity() method includes the timestamp)
            repository.insertLocation(locationData.toEntity())
            Log.d("LocationWorker", "Location saved to database")

            // Check network connectivity before attempting to upload
            if (isNetworkAvailable(applicationContext)) {
                // Build Retrofit instance
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://x7b9yw5krd.execute-api.ap-southeast-2.amazonaws.com/prod/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val locationApi = retrofit.create(TrackerApiService::class.java)

                val unsyncedLocations = repository.getUnsyncedLocations()
                for (locationEntity in unsyncedLocations) {
                    val request = LocationUploadRequest(
                        timestamp = locationEntity.timestamp.toString(),
                        latitude = locationEntity.latitude,
                        longitude = locationEntity.longitude
                    )
                    try {
                        val response = locationApi.uploadLocation(request)
                        if (response.isSuccessful) {
                            repository.markLocationSynced(locationEntity.id)
                            Log.d("LocationWorker", "Location ${locationEntity.id} uploaded and marked as synced")
                        } else {
                            Log.d("LocationWorker", "Failed to upload location ${locationEntity.id}: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("LocationWorker", "Error uploading location ${locationEntity.id}: ${e.message}")
                    }
                }
            } else {
                Log.d("LocationWorker", "Network not available, skipping upload.")
            }

            Result.success()
        } else {
            Log.d("LocationWorker", "Location is null, retrying")
            Result.failure()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}