package com.ackileo.telematics.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ackileo.telematics.ui.viewmodel.AuthState
import com.ackileo.telematics.ui.viewmodel.AuthViewModel

/**
 * The Stateful Register Screen that connects to the ViewModel
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Side effect to handle Success or Errors
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> onRegisterSuccess()
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((authState as AuthState.Error).message)
            }
            else -> Unit
        }
    }

    // Wrap in Surface to provide a solid background and prevent black screen
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RegisterScreenContent(
            authState = authState,
            snackbarHostState = snackbarHostState,
            onBackClick = onBackClick,
            onRegisterClick = { name, email, nid, lNum, lClass, phone, pass ->
                viewModel.register(name, email, phone, pass, nid, lNum, lClass)
            }
        )
    }
}

/**
 * The Stateless UI Content for the Register Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(
    authState: AuthState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRegisterClick: (String, String, String, String, String, String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var licenseClass by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val errorMessage = (authState as? AuthState.Error)?.message

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Ensures background consistency
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Personal Information")

            RegistrationTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                icon = Icons.Default.Person,
                enabled = authState !is AuthState.Loading
            )

            RegistrationTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                enabled = authState !is AuthState.Loading
            )

            RegistrationTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                enabled = authState !is AuthState.Loading
            )

            SectionHeader("License Details")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RegistrationTextField(
                    value = nationalId,
                    onValueChange = { nationalId = it },
                    label = "National ID",
                    icon = Icons.Default.Badge,
                    modifier = Modifier.weight(1f),
                    enabled = authState !is AuthState.Loading
                )
                RegistrationTextField(
                    value = licenseClass,
                    onValueChange = { licenseClass = it },
                    label = "Class",
                    icon = Icons.Default.DriveEta,
                    modifier = Modifier.weight(0.6f),
                    enabled = authState !is AuthState.Loading
                )
            }

            RegistrationTextField(
                value = licenseNumber,
                onValueChange = { licenseNumber = it },
                label = "License Number",
                icon = Icons.Default.ConfirmationNumber,
                enabled = authState !is AuthState.Loading
            )

            DocumentPlaceholder(
                label = "Upload Driving License Photo",
                icon = Icons.Default.CloudUpload
            )

            SectionHeader("Security")

            RegistrationTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                icon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                enabled = authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onRegisterClick(fullName, email, nationalId, licenseNumber, licenseClass, phoneNumber, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = authState !is AuthState.Loading &&
                        fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Complete Registration", fontWeight = FontWeight.Bold)
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun DocumentPlaceholder(label: String, icon: ImageVector) {
    OutlinedCard(
        onClick = { /* Handle Upload */ },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onPasswordVisibilityChange) {
                    val visibilityIcon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(visibilityIcon, contentDescription = null)
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreenContent(
            authState = AuthState.Idle,
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onRegisterClick = { _, _, _, _, _, _, _ -> }
        )
    }
}