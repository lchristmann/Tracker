package com.lchristmann.tracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lchristmann.tracker.ui.theme.TrackerTheme

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController() // Initialize NavController

            TrackerTheme {
                val items = listOf(
                    BottomNavigationItem(
                        title = "Locations",
                        selectedIcon = Icons.AutoMirrored.Outlined.List,
                        unselectedIcon = Icons.AutoMirrored.Outlined.List
                    ),
                    BottomNavigationItem(
                        title = "Home",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home
                    ),
                    BottomNavigationItem(
                        title = "Map",
                        selectedIcon = Icons.Filled.Place,
                        unselectedIcon = Icons.Outlined.Place
                    )
                )

                var selectedItemIndex by rememberSaveable { mutableIntStateOf(1) }

                Surface(
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                items.forEachIndexed { index, item -> 
                                    NavigationBarItem(
                                        selected = selectedItemIndex == index,
                                        onClick = {
                                            selectedItemIndex = index
                                            // Handle navigation to respective screen
                                            when (index) {
                                                0 -> navController.navigate(Screen.Locations.route)
                                                1 -> navController.navigate(Screen.Home.route)
                                                2 -> navController.navigate(Screen.Map.route)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if(index == selectedItemIndex) {
                                                    item.selectedIcon
                                                } else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        })
                                }
                            }
                        }
                    ) {
                        // Set up navigation
                        NavHost(navController = navController, startDestination = Screen.Home.route) {
                            composable(Screen.Home.route) {
                                HomeScreen()  // HomeScreen composable
                            }
                            composable(Screen.Locations.route) {
                                LocationsScreen()  // LocationsScreen composable
                            }
                            composable(Screen.Map.route) {
                                MapScreen()  // MapScreen composable
                            }
                        }
                    }
                }
            }
        }
    }
}

// Define the screen routes in a sealed class
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Locations : Screen("locations")
    object Map : Screen("map")
}