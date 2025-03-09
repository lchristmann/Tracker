package com.lchristmann.tracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LocationsScreen(viewModel: LocationsViewModel = viewModel()) {
    val locations by viewModel.locations.observeAsState(emptyList())

    // Fetch locations when the Composable first launches
    LaunchedEffect(Unit) {
        viewModel.fetchLocations()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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

            itemsIndexed(locations) { index, location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = location.timestamp.toReadableDate(),
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = location.latitude.toString(), modifier = Modifier.weight(1f))
                    Text(text = location.longitude.toString(), modifier = Modifier.weight(1f))
                    // Display the synced status using real icons
                    Icon(
                        imageVector = if (location.synced) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (location.synced) "Synced" else "Not Synced",
                        modifier = Modifier.weight(0.7f).size(24.dp), // Size the icon appropriately
                        tint = if (location.synced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Helper function for formatting timestamps
private fun Long.toReadableDate(): String {
    val dateFormat = SimpleDateFormat("d.M. HH:mm", Locale.getDefault())
    return dateFormat.format(Date(this))
}
