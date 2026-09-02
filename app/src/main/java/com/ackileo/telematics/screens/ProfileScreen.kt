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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.ackileo.telematics.data.remote.dto.DriverDto
import com.ackileo.telematics.ui.components.AppBottomBar
import com.ackileo.telematics.ui.viewmodel.AuthState
import com.ackileo.telematics.ui.viewmodel.ProfileState
import com.ackileo.telematics.ui.viewmodel.ProfileViewModel

/**
 * STATEFUL: Use this in your NavGraph.
 * Handles Hilt ViewModel and Navigation logic.
 */
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val profileState = viewModel.profileState.collectAsState()
    val actionState = viewModel.actionState.collectAsState()

    LaunchedEffect(actionState.value) {
        if (actionState.value == AuthState.Success) {
            viewModel.resetActionState()
            onLogout()
        }
    }

    ProfileContent(
        navController = navController,
        profileState = profileState.value,
        actionState = actionState.value,
        onLogoutClick = {
            viewModel.logout()
        },
        onRetry = {
            viewModel.loadUserProfile()
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
    profileState: ProfileState = ProfileState.Idle,
    actionState: AuthState = AuthState.Idle,
    onLogoutClick: () -> Unit = {},
    onRetry: () -> Unit = {}
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
            when (profileState) {
                is ProfileState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProfileState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Failed to load profile",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = profileState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is ProfileState.Success -> {
                    ProfileContentSuccess(
                        driver = profileState.driver,
                        navController = navController,
                        onLogoutClick = onLogoutClick,
                        isLoggingOut = actionState == AuthState.Loading,
                        paddingValues = paddingValues
                    )
                }

                else -> {
                    // Idle state - show loading
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContentSuccess(
    driver: DriverDto,
    navController: NavHostController,
    onLogoutClick: () -> Unit = {},
    isLoggingOut: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Header Section with Profile Picture ---
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

        // --- 2. User Information ---
        Text(
            text = driver.fullName ?: "Unknown Driver",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (!driver.id.isNullOrBlank()) {
            Text(
                text = "Driver ID: ${driver.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. Contact Details Card ---
        InfoCard(title = "Contact Details") {
            if (!driver.email.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = driver.email
                )
            }
            if (!driver.phone.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "Phone",
                    value = driver.phone
                )
            }
            if (driver.email.isNullOrBlank() && driver.phone.isNullOrBlank()) {
                Text(
                    text = "No contact information available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. Vehicle Information Card (if available) ---
        if (driver.vehicle != null) {
            VehicleInfoCard(vehicle = driver.vehicle)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 5. Action Buttons ---
        Button(
            onClick = { /* Edit Profile Logic */ },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoggingOut
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Edit Profile")
        }

        OutlinedButton(
            onClick = { /* Password Logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = !isLoggingOut
        ) {
            Text("Change Password")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 6. Logout Button ---
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            enabled = !isLoggingOut
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onError,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text("Logout", color = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
fun VehicleInfoCard(
    title: String = "Vehicle Information",
    vehicle: com.ackileo.telematics.data.remote.dto.VehicleDto
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (!vehicle.make.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.DirectionsCar,
                    label = "Make",
                    value = vehicle.make
                )
            }

            if (!vehicle.model.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.DirectionsCar,
                    label = "Model",
                    value = vehicle.model
                )
            }

            if (!vehicle.plateNumber.isNullOrBlank()) {
                InfoRow(
                    icon = Icons.Default.Numbers,
                    label = "Plate Number",
                    value = vehicle.plateNumber
                )
            }

            if (vehicle.make.isNullOrBlank() && vehicle.model.isNullOrBlank() && vehicle.plateNumber.isNullOrBlank()) {
                Text(
                    text = "No vehicle information available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            navController = rememberNavController(),
            profileState = ProfileState.Success(
                driver = DriverDto(
                    id = "DL-99887766",
                    fullName = "Dao Ackileo",
                    email = "ackileo.doe@telematics.com",
                    phone = "+256770567890",
                    vehicle = com.ackileo.telematics.data.remote.dto.VehicleDto(
                        id = "VEH-123456",
                        make = "Toyota",
                        model = "Camry",
                        plateNumber = "UG 123 ABC"
                    )
                )
            )
        )
    }
}