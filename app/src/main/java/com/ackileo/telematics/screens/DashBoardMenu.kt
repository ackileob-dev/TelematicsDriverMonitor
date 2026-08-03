package com.ackileo.telematics.screens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ackileo.telematics.ui.viewmodel.TripHistoryViewModel

// REMOVED the import of TripHistoryScreen to avoid conflict with the placeholder below

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem("dashboard_home", "Dashboard", Icons.Default.Dashboard)
    data object Trips : BottomNavItem("trip_history", "Trips", Icons.Default.History)
    data object Live : BottomNavItem("live_tracking", "Live", Icons.Default.LocationOn)
    data object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun DashBoardMenu() {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Trips,
        BottomNavItem.Live,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardHomeScreen()
            }

            composable(BottomNavItem.Trips.route) {
                val viewModel: TripHistoryViewModel = hiltViewModel()
                TripHistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Live.route) {
                // FIXED: Changed name to match the placeholder below
                LiveTrackingScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

// --- Screen Implementations (Placeholders) ---

@Composable
fun DashboardHomeScreen() {
    PlaceholderCenteredText("Dashboard Content (Scores, Stats)")
}

@Composable
fun TripHistoryScreen(
    viewModel: TripHistoryViewModel,
    onBack: () -> Unit
) {
    PlaceholderCenteredText("Trip History List (Data from ViewModel)")
}

@Composable
fun LiveTrackingScreen(
    onBack: () -> Unit
) {
    PlaceholderCenteredText("Live Map Tracking")
}

@Composable
fun ProfileScreen() {
    PlaceholderCenteredText("User Profile & Settings")
}

@Composable
private fun PlaceholderCenteredText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}