package com.ackileo.telematics.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ackileo.telematics.ui.viewmodel.AuthViewModel
import com.ackileo.telematics.ui.viewmodel.DashboardViewModel

/**
 * Extension function to define all navigation destinations.
 * This is called from the NavHost inside MainActivity.kt
 */
fun NavGraphBuilder.appNavGraph(navController: NavHostController) {

    // 1. Splash Screen

//1. Splash Screen
    composable(Screen.Splash.route) {
        val authViewModel: AuthViewModel = hiltViewModel()
        SplashScreen(
            navController = navController, // <--- ADD THIS LINE
            onNavigateNext = {
                val destination = if (authViewModel.isUserLoggedIn) {
                    Screen.Dashboard.route
                } else {
                    Screen.Login.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        )
    }
    // 2. Login Screen
    composable(Screen.Login.route) {
        val authViewModel: AuthViewModel = hiltViewModel()
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            onNavigateToRegister = {
                navController.navigate(Screen.Register.route)
            }
        )
    }

    // 3. Register Screen
    composable(Screen.Register.route) {
        val authViewModel: AuthViewModel = hiltViewModel()
        RegisterScreen(
            viewModel = authViewModel,
            onRegisterSuccess = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            },
            onBackClick = {
                navController.popBackStack()
            }
        )
    }

    // 4. Dashboard Screen
    composable(Screen.Dashboard.route) {
        val dashboardViewModel: DashboardViewModel = hiltViewModel()
        DashboardScreen(
            viewModel = dashboardViewModel,
            onNavigateToTrips = { navController.navigate("trip_history") },
            onNavigateToTracking = { navController.navigate(Screen.Tracking.route) },
            onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
        )
    }

    // 5. Tracking Screen
    composable(Screen.Tracking.route) {
        TrackingScreen(
            onBack = { navController.popBackStack() }
        )
    }

    // 6. Rewards Screen
    composable(Screen.Rewards.route) {
        RewardsScreen()
    }

    // 7. Profile Screen
    composable(Screen.Profile.route) {
        val authViewModel: AuthViewModel = hiltViewModel()
        ProfileScreen(
            navController = navController,
            viewModel = authViewModel,
            onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // 8. Trip History
    composable("trip_history") {
        PlaceholderScreen("Trip History")
    }
}

/**
 * A simple placeholder for screens not yet fully built
 */
@Composable
fun PlaceholderScreen(name: String) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = name, style = MaterialTheme.typography.headlineLarge)
        }
    }
}