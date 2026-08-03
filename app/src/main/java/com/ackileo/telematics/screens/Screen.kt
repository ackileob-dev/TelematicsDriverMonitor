package com.ackileo.telematics.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {

    //new
    // Auth Flow
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Main Flow (Bottom Nav Items)
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    data object Tracking : Screen("tracking", "Tracking", Icons.Default.LocationOn)
    data object Rewards : Screen("rewards", "Rewards", Icons.Default.Star)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Non-bottom-nav screens
    data object TripHistory : Screen("trip_history", "History", Icons.Default.History)
}

// List used to generate the Bottom Navigation Bar
val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Tracking,
    Screen.Rewards,
    Screen.Profile
)