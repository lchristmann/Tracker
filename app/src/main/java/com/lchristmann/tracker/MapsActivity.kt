package com.lchristmann.tracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapsActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load OSMDroid config
        getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        // Setup MapView
        mapView = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
        setContentView(mapView)

        // Fetch and display locations
        fetchAndDisplayLocations()
    }

    private fun fetchAndDisplayLocations() {
        lifecycleScope.launch(Dispatchers.IO) {
            val database = LocationDatabase.getDatabase(applicationContext)
            val locationRepository = LocationRepository(database.locationDao())
            val locations = locationRepository.getLastFiftyLocationsDesc()

            withContext(Dispatchers.Main) {
                if (locations.isNotEmpty()) {
                    Log.d("MapsActivity", "Fetched ${locations.size} locations")
                    addMarkersToMap(locations)
                } else {
                    Log.d("MapsActivity", "No locations stored.")
                }
            }
        }
    }

    private fun addMarkersToMap(locations: List<LocationEntity>) {
        val mapController = mapView.controller
        val markers = mutableListOf<Marker>()
        var redMarker: Marker? = null  // To store the red marker separately

        Log.d("MapsActivity", "Adding markers to map...")

        // Add the red marker first
        locations.firstOrNull()?.let { firstLocation ->
            redMarker = Marker(mapView).apply {
                position = GeoPoint(firstLocation.latitude, firstLocation.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = firstLocation.timestamp.toReadableDate()
                icon = getRedMarker()
                markers.add(this)  // Store the red marker in the list
                Log.d("MapsActivity", "Adding red marker at: ${firstLocation.latitude}, ${firstLocation.longitude}")
            }
        }

        // Add the rest of the markers in reverse order
        locations.drop(1).reversed().forEach { location ->
            Marker(mapView).apply {
                position = GeoPoint(location.latitude, location.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = location.timestamp.toReadableDate()
                icon = getDefaultMarker()
                markers.add(this)
                Log.d("MapsActivity", "Adding marker at: ${location.latitude}, ${location.longitude}")
            }
        }

        // Clear the overlays before adding markers
        mapView.overlays.clear()

        // Add all markers to the map (re-add the red marker last, so it's on top of any previous ones in the exact same place)
        markers.forEach { marker ->
            mapView.overlays.add(marker)
        }
        mapView.overlays.add(redMarker)

        // Zoom to the red marker
        redMarker?.let {
            mapController.setZoom(15.0)
            mapController.setCenter(it.position)  // Set the center to the red marker's position
            Log.d("MapsActivity", "Zooming to red marker at: ${it.position.latitude}, ${it.position.longitude}")
        }

        mapView.invalidate()  // Refresh map
    }

    private fun getDefaultMarker() = getDrawable(R.drawable.marker_blue)
    private fun getRedMarker() = getDrawable(R.drawable.marker_red)
}

// Format timestamp to readable date
private fun Long.toReadableDate(): String {
    val dateFormat = SimpleDateFormat("d.M. HH:mm", Locale.getDefault())
    return dateFormat.format(Date(this))
}