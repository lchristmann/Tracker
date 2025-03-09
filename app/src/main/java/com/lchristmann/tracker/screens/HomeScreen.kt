package com.lchristmann.tracker.screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lchristmann.tracker.utils.LocationUtils
import com.lchristmann.tracker.LocationWorker
import com.lchristmann.tracker.MainActivity
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val locationUtils = LocationUtils()
    val viewModel: TrackingViewModel = viewModel()

    // Reusable function to enqueue periodic location tracking work
    fun enqueueLocationTrackingWork() {
        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationWorker>(
            15, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "LocationTracking",
                ExistingPeriodicWorkPolicy.UPDATE,
                locationWorkRequest
            )
        viewModel.startTracking()
    }

    // Launcher for background permission
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted: Boolean ->
            if (granted) {
                enqueueLocationTrackingWork()
            } else {
                // For Android 11+ this permission typically directs users to settings.
                Toast.makeText(
                    context,
                    "Background location permission is required. Please enable it in Settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    // Launcher for foreground permissions (Fine and Coarse)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // Foreground permissions granted, now check background permission.
                if (locationUtils.hasBackgroundLocationPermission(context)) {
                    enqueueLocationTrackingWork()
                } else {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                // Explain why permissions are necessary
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (rationaleRequired) {
                    Toast.makeText(context,
                        "Location Permission is required for this feature to work", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(context,
                        "Location Permission is required. Please enable it in the Android Settings", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    )

    // --------------------------------- The UI of the App ---------------------------------
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        // If tracking is active, show the "Stop Tracking" button, else show the "Start Tracking" button
        if (viewModel.isTracking.value) {
            Text(text = "Location is tracked every 15 min.")
            Button(onClick = {
                WorkManager.getInstance(context).cancelUniqueWork("LocationTracking")
                viewModel.stopTracking()
            }) { Text(text = "Stop Tracking") }
        } else {
            Text(text = "Location is not tracked.")
            Button(onClick = {
                if (locationUtils.hasLocationPermission(context) && locationUtils.hasBackgroundLocationPermission(context)) {
                    enqueueLocationTrackingWork()
                } else {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }) { Text(text = "Start Tracking") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // A button to do a single tracking action after a 20s delay
        Button(onClick = {
            Log.d("MainActivity.kt", "Track once now and upload all locations clicked")
            if (locationUtils.hasLocationPermission(context) && locationUtils.hasBackgroundLocationPermission(context)) {
                val oneTimeRequest = OneTimeWorkRequestBuilder<LocationWorker>().build()
                WorkManager.getInstance(context).enqueue(oneTimeRequest)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) { Text(text = "Track once now and upload all locations") }

    }
}