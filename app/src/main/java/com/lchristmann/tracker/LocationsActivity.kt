package com.lchristmann.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.lchristmann.tracker.ui.theme.TrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = LocationDatabase.getDatabase(applicationContext)
        val locationRepository = LocationRepository(database.locationDao())

        lifecycleScope.launch(Dispatchers.IO) {
            val locations: List<LocationEntity> = locationRepository.getLastFiftyLocationsDesc()
            withContext(Dispatchers.Main) {
                setContent {
                    TrackerTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize()
                                .windowInsetsPadding(WindowInsets.systemBars),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LocationsDisplay(locations)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationsDisplay(locations: List<LocationEntity>) {

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp),) {
        // Page title
        Text(
            text = "Tracked Locations (Last 50)",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                // Table Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Time", modifier = Modifier.weight(1f))
                    Text(text = "Latitude", modifier = Modifier.weight(1f))
                    Text(text = "Longitude", modifier = Modifier.weight(1f))
                    Text(text = "Synced", modifier = Modifier.weight(0.7f))
                }
            }

            items(locations) { location ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = location.timestamp.toReadableDate(),
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = location.latitude.toString(), modifier = Modifier.weight(1f))
                    Text(text = location.longitude.toString(), modifier = Modifier.weight(1f))
                    Text(text = if (location.synced) "    ✅" else "    ❌", modifier = Modifier.weight(0.7f))
                }
            }
        }
    }
}

private fun Long.toReadableDate(): String {
    val dateFormat = SimpleDateFormat("d.M. HH:mm", Locale.getDefault())
    val date = Date(this)
    return dateFormat.format(date)
}