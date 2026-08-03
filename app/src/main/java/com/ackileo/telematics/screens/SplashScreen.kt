package com.ackileo.telematics.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons  // Added missing Icons import
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

 // <--- This expects a function
    // ... logic
    @Composable
    fun SplashScreen(navController: NavHostController, onNavigateNext: () -> Unit) {

        // Animation states
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }

    // Navigation logic and Animation Trigger
    LaunchedEffect(key1 = true) {
        // Run animations in parallel
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Total delay including animation (2.5 seconds)
        delay(1500)

        // Navigate to Login and clear backstack
        navController.navigate(route = Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // Splash UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary, // Blue
                        MaterialTheme.colorScheme.secondary // Green
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // App Icon Placeholder (Circle with Car Icon)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Telematics",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "Driver Monitor",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
        }

        // Bottom Branding / Version
        Text(
            text = "TRUST • SAFETY • TECHNOLOGY",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            letterSpacing = 3.sp
        )
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen(
            navController = rememberNavController(),
            onNavigateNext = {} // Provide an empty lambda for the preview
        )
    }
}