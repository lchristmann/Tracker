package com.lchristmann.tracker

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.lchristmann.tracker.ui.theme.TrackerTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("API_KEY", BuildConfig.API_KEY)
        setContent {
            val viewModel: LocationViewModel = viewModel()
            TrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MyApp(viewModel: LocationViewModel) {
    val context = LocalContext.current
    val locationUtils = LocationUtils()
    LocationDisplay(locationUtils = locationUtils, viewModel, context = context)
}

@Composable
fun LocationDisplay(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    context: Context
) {
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

    // Request permissions: in case we get them enqueue LocationTracking work, else explain why they're necessary
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // I have access to location
                enqueueLocationTrackingWork()
            } else {
                // Show rationale why we want the permissions if required
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
            Text(text = "Location is being tracked.")
            Button(onClick = {
                WorkManager.getInstance(context).cancelUniqueWork("LocationTracking")
                viewModel.stopTracking()
            }) { Text(text = "Stop Tracking") }
        } else {
            Text(text = "Location is not tracked.")
            Button(onClick = {
                if (locationUtils.hasLocationPermission(context)) {
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

        Spacer(modifier = Modifier.height(32.dp))
        // A button to do a single tracking action after a 20s delay
        Button(onClick = {
            if (locationUtils.hasLocationPermission(context)) {
                val oneTimeRequest = OneTimeWorkRequestBuilder<LocationWorker>()
                    .setInitialDelay(20, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(context)
                    .enqueue(oneTimeRequest)
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) { Text(text = "Test Immediate Tracking with 20s Delay") }

    }
}