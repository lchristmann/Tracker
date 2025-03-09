package com.lchristmann.tracker.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lchristmann.tracker.data.LocationEntity
import com.lchristmann.tracker.R
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MapScreen(viewModel: LocationsViewModel = viewModel()) {
    val locations by viewModel.locations.observeAsState(emptyList())
    val context = LocalContext.current

    // Load OSMDroid configuration
    getInstance().load(
        context,
        context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    )

    // Fetch locations when the Composable is first launched
    LaunchedEffect(Unit) {
        viewModel.fetchLocations()  // Fetch locations from the ViewModel
    }

    // UI structure for the MapScreen
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                }.also { it.onResume() }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.onResume()  // Ensure map resumes correctly

                if (locations.isNotEmpty()) {
                    addMarkersToMap(context, mapView, locations)
                }
            }
        )
    }
}

// Function to add markers to the map
private fun addMarkersToMap(
    context: Context,
    mapView: MapView,
    locations: List<LocationEntity>
) {
    val mapController = mapView.controller
    val markers = mutableListOf<Marker>()
    var redMarker: Marker? = null

    // Add the red marker first (first location in the list)
    locations.firstOrNull()?.let { firstLocation ->
        redMarker = Marker(mapView).apply {
            position = GeoPoint(firstLocation.latitude, firstLocation.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = firstLocation.timestamp.toReadableDate()
            icon = context.getDrawable(R.drawable.marker_red)  // Custom red marker
            markers.add(this)
            Log.d("MapScreen", "Adding red marker at: ${firstLocation.latitude}, ${firstLocation.longitude}")
        }
    }

    // Add the rest of the markers in reverse order
    locations.drop(1).reversed().forEach { location ->
        Marker(mapView).apply {
            position = GeoPoint(location.latitude, location.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = location.timestamp.toReadableDate()
            icon = context.getDrawable(R.drawable.marker_blue)  // Custom blue marker
            markers.add(this)
            Log.d("MapScreen", "Adding marker at: ${location.latitude}, ${location.longitude}")
        }
    }

    // Clear previous overlays before adding new ones
    mapView.overlays.clear()

    // Add all markers to the map
    markers.forEach { marker ->
        mapView.overlays.add(marker)
    }
    mapView.overlays.add(redMarker)  // Ensure the red marker is on top

    // Zoom to the red marker if available
    redMarker?.let {
        mapController.setZoom(16.0)
        mapController.setCenter(it.position)
        Log.d("MapScreen", "Zooming to red marker at: ${it.position.latitude}, ${it.position.longitude}")
    }

    mapView.invalidate()  // Refresh the map
}

// Format timestamp to readable date
private fun Long.toReadableDate(): String {
    val dateFormat = SimpleDateFormat("d.M. HH:mm", Locale.getDefault())
    return dateFormat.format(Date(this))
}
