package com.ackileo.telematics.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ackileo.telematics.ui.components.AppBottomBar
import com.ackileo.telematics.ui.viewmodel.AuthViewModel

/**
 * STATEFUL: Use this in your NavGraph.
 * Handles Hilt ViewModel and Navigation logic.
 */
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    // Collect specific data from your ViewModel here if necessary
    // Example: val user by viewModel.userState.collectAsState()

    ProfileContent(
        navController = navController,
        onLogoutClick = {
            viewModel.logout()
            onLogout()
        }
    )
}

/**
 * STATELESS: The actual UI.
 * This is "Debugged" because it doesn't depend on Hilt, making Previews work.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    navController: NavHostController,
    onLogoutClick: () -> Unit = {},
    userName: String = "Dao Ackileo",
    userId: String = "DL-99887766"
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                AppBottomBar(
                    navController = navController,
                    currentRoute = Screen.Profile.route
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. Header Section ---
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "ID: $userId", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                // --- 2. Statistics Row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Safety Score", value = "98", icon = Icons.Default.Shield)
                    StatItem(label = "Total Trips", value = "154", icon = Icons.Default.Route)
                    StatItem(label = "Points", value = "1,250", icon = Icons.Default.Star)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 3. Details Cards ---
                InfoCard(title = "Contact Details") {
                    InfoRow(icon = Icons.Default.Phone, label = "Phone", value = "+256770 567 890")
                    InfoRow(icon = Icons.Default.Email, label = "Email", value = "ackileo.doe@telematics.com")
                }

                Spacer(modifier = Modifier.height(16.dp))

                InfoCard(title = "License Information") {
                    InfoRow(icon = Icons.Default.Badge, label = "License Class", value = "Class B1")
                    InfoRow(icon = Icons.Default.CalendarToday, label = "Expiry Date", value = "12 / 2028")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 4. Action Buttons ---
                Button(
                    onClick = { /* Edit Profile Logic */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Profile")
                }

                OutlinedButton(
                    onClick = { /* Password Logic */ },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Change Password")
                }

                TextButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// --- HELPER COMPONENTS (Cleaned and Restored) ---

@Composable
fun StatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        // We call Content directly so Hilt isn't required for the Preview to render
        ProfileContent(
            navController = rememberNavController()
        )
    }
}