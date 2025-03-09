package com.lchristmann.tracker.screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
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
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        // Info boxes
        InfoBox(title = "Setup", description = "When prompted, give the app permission to access your location and select 'Allow always'.")
        InfoBox(title = "Usage", description = """
            Start and stop tracking as you like. You can completely close the app and tracking still works.
            Internet is only needed for uploading locations and you can manually upload all via button.
        """.trimIndent())
        InfoBox(title = "Tracking", description = "When tracking is activated, every +/- 15 minutes your location is stored and if possible uploaded.")

        Spacer(modifier = Modifier.height(48.dp))

        // If tracking is active, show the "Stop Tracking" button, else show the "Start Tracking" button
        if (viewModel.isTracking.value) {
            OutlinedButton(
                onClick = {
                    WorkManager.getInstance(context).cancelUniqueWork("LocationTracking")
                    viewModel.stopTracking()
                },
                modifier = Modifier.height(56.dp)) { Text(text = "Stop Tracking") }
        } else {
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
            },
            modifier = Modifier.height(56.dp)) { Text(text = "Start Tracking") }
        }

        Spacer(modifier = Modifier.height(36.dp))

        ElevatedButton(onClick = {
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
        },
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 2.dp),
        modifier = Modifier.height(72.dp)) { Text(text = "Track once now and\nupload all locations") }
    }
}