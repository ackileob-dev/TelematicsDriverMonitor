# Profile Screen Integration with Backend Authentication System

## Overview
Successfully integrated the Profile screen with the TelematicsDriverMonitor backend authentication system. The profile screen now retrieves authenticated user information using the `GET /api/auth/me` endpoint and displays user details including name, email, driver information, and vehicle information where available.

## Files Created

### 1. **ProfileViewModel.kt**
**Location:** `app/src/main/java/com/ackileo/telematics/ui/viewmodel/ProfileViewModel.kt`

**Purpose:** Manages the Profile screen's UI state and handles data fetching and logout operations.

**Key Features:**
- `loadUserProfile()`: Fetches the current authenticated user's profile from the backend
- `logout()`: Comprehensive logout that clears all authentication state
- State management via `profileState` and `actionState` flows
- Handles Loading, Success, and Error states

**Architecture:**
- Uses Hilt ViewModel for dependency injection
- Coroutine-based async operations via `viewModelScope`
- Clean separation of concerns between UI and business logic

### 2. **ProfileRepository.kt**
**Location:** `app/src/main/java/com/ackileo/telematics/data/repository/ProfileRepository.kt`

**Purpose:** Handles all profile-related data operations and API communication.

**Interface:**
```kotlin
interface ProfileRepository {
    suspend fun getCurrentUser(): Result<DriverDto>
    fun logout()
}
```

**Key Features:**
- `getCurrentUser()`: Calls `GET /api/auth/me` with JWT bearer token
  - Returns `Result<DriverDto>` for proper error handling
  - Automatically clears auth state on 401 responses
  - Handles network errors gracefully
- `logout()`: Performs comprehensive cleanup:
  1. Clears JWT tokens from encrypted storage (TokenManager)
  2. Clears all user profile data (SharedPrefManager)
  3. Signs out from Firebase (if integrated)
  4. Subsequent API requests will not contain old token because JwtInterceptor checks `tokenManager.getAccessToken()`

**Error Handling:**
- Specific error messages for different HTTP status codes (400, 401, 403, 404, 500)
- Custom `ProfileBackendException` for clear error tracking
- Network error detection and reporting

## Files Modified

### 1. **ProfileScreen.kt**
**Location:** `app/src/main/java/com/ackileo/telematics/screens/ProfileScreen.kt`

**Changes:**
- **Stateful Component**: Now uses `ProfileViewModel` with state collection
- **State Management**: Handles Loading, Error, and Success states
- **Real Data Display**: Shows actual user information from backend:
  - User name (fullName)
  - Email
  - Phone number
  - Driver ID
  - Vehicle information (make, model, plate number) when available
- **Logout Flow**: 
  - Logout button shows spinner while in progress
  - Navigates to Login screen after successful logout
  - LaunchedEffect triggers navigation callback after logout completes
- **UI Enhancements**:
  - Dynamic card display for contact details
  - Vehicle information card (only shown if vehicle data exists)
  - Null-safety checks for all displayed fields
  - Error state with retry button
  - Loading state with progress indicator

### 2. **NavGraph.kt**
**Location:** `app/src/main/java/com/ackileo/telematics/screens/NavGraph.kt`

**Changes:**
- Updated imports to include `ProfileViewModel`
- Changed Profile screen composable to use `ProfileViewModel` instead of `AuthViewModel`
- Maintains proper navigation on logout (popUpTo(0))

### 3. **RepositoryModule.kt**
**Location:** `app/src/main/java/com/ackileo/telematics/domain/model/RepositoryModule.kt`

**Changes:**
- Added import for `ProfileRepository` and `ProfileRepositoryImpl`
- Added Hilt binding:
  ```kotlin
  @Binds
  @Singleton
  abstract fun bindProfileRepository(
      profileRepositoryImpl: ProfileRepositoryImpl
  ): ProfileRepository
  ```

## API Integration

### Endpoint: GET /api/auth/me
- **Purpose**: Retrieve the authenticated user's profile
- **Authentication**: JWT Bearer token (automatically injected by JwtInterceptor)
- **Response**: `ApiResponse<DriverDto>`
- **Status Codes**:
  - 200: Success - returns user profile
  - 401: Unauthorized - token invalid/expired (triggers logout)
  - 500: Server error

### Response Structure
```json
{
  "success": true,
  "data": {
    "id": "DL-99887766",
    "fullName": "Dao Ackileo",
    "email": "ackileo.doe@telematics.com",
    "phone": "+256770567890",
    "vehicle": {
      "id": "VEH-123456",
      "make": "Toyota",
      "model": "Camry",
      "plateNumber": "UG 123 ABC"
    }
  }
}
```

## Logout Implementation Details

### Logout Flow:
1. User taps "Logout" button on Profile screen
2. `ProfileViewModel.logout()` is called
3. Action state changes to `AuthState.Loading` (UI shows spinner)
4. `ProfileRepository.logout()` executes:
   - `tokenManager.clearAuthState()`: Removes JWT tokens from encrypted storage
     - Clears: access_token, refresh_token, driver_id, active_trip info
   - `prefManager.clearAll()`: Clears all user profile data from shared preferences
     - Clears: auth_token, user_name, user_email
   - Firebase sign out (if applicable)
5. Action state changes to `AuthState.Success`
6. LaunchedEffect triggers `onLogout()` callback
7. NavGraph navigates to Login screen with `popUpTo(0)` to clear back stack

### Token Cleanup Mechanism:
The `JwtInterceptor` ensures old tokens are not sent after logout:
```kotlin
override fun intercept(chain: Interceptor.Chain): Response {
    val accessToken = tokenManager.getAccessToken()
    if (accessToken == null) {
        return chain.proceed(originalRequest)  // No token = no auth header
    }
    val requestWithToken = originalRequest.newBuilder()
        .addHeader("Authorization", "Bearer $accessToken")
        .build()
    return chain.proceed(requestWithToken)
}
```

After `logout()`, `tokenManager.getAccessToken()` returns `null`, so no auth header is added to subsequent requests.

## Security Considerations

### No Passwords Stored
- Passwords are **never** stored locally
- Only JWT tokens are stored in `EncryptedSharedPreferences`
- AES256-GCM encryption protects token storage

### Secure Token Management
- TokenManager uses `EncryptedSharedPreferences` with MasterKey scheme AES256_GCM
- PrefKeyEncryptionScheme: AES256_SIV
- PrefValueEncryptionScheme: AES256_GCM
- JwtInterceptor automatically handles token injection and removal

### Logout Security
- All tokens cleared from encrypted storage
- All user profile data cleared from regular preferences
- Firebase authentication cleared
- Subsequent API calls cannot access old token
- UI navigation clears activity back stack

## User States and UI Handling

### ProfileState
```kotlin
sealed class ProfileState {
    object Idle : ProfileState()           // Initial state
    object Loading : ProfileState()        // Fetching profile
    data class Success(val driver: DriverDto) : ProfileState()  // Profile loaded
    data class Error(val message: String) : ProfileState()      // Error occurred
}
```

### AuthState
```kotlin
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
```

## Data Models

### DriverDto
```kotlin
data class DriverDto(
    val id: String,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val vehicle: VehicleDto? = null
)
```

### VehicleDto
```kotlin
data class VehicleDto(
    val id: String,
    val make: String?,
    val model: String?,
    val plateNumber: String?
)
```

## Navigation Integration

### Profile Screen in NavGraph
- **Route**: `Screen.Profile.route`
- **Entry Point**: Bottom navigation bar (AppBottomBar)
- **ViewModel**: `ProfileViewModel` (injected via Hilt)
- **Logout Callback**: Navigates to Login screen with `popUpTo(0)`

### Navigation on Logout
```kotlin
onLogout = {
    navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }  // Clear entire back stack
    }
}
```

## UI Components

### ProfileContent Composable
Main stateless UI component that displays:
- Loading state with spinner
- Error state with error message and retry button
- Success state with user information

### ProfileContentSuccess Composable
Displays user profile with:
- Profile avatar (circular icon)
- User name and driver ID
- Contact Details Card (email, phone)
- Vehicle Information Card (make, model, plate number)
- Edit Profile button
- Change Password button
- Logout button (with spinner while loading)

### Helper Components
- `InfoCard`: Reusable card wrapper with title and divider
- `InfoRow`: Displays icon, label, and value in a row
- `StatItem`: Shows statistics with icon and label
- `VehicleInfoCard`: Specialized card for vehicle information

## Error Handling Strategy

### Network Errors
- Caught and wrapped in `ProfileBackendException`
- User-friendly message: "Network error. Please check your internet connection."
- Retry button available in error state

### API Errors
- **401 Unauthorized**: "Unauthorized. Please sign in again." (triggers logout)
- **404 Not Found**: "Profile not found."
- **500 Server Error**: "Server error. Please try again later."
- **Other errors**: Falls back to API response message or generic message

### User Feedback
- Loading spinner during API calls
- Clear error messages with error icon
- Retry button for network errors
- Logout button shows spinner during logout process
- Buttons disabled while actions are in progress

## Testing

### Preview Support
ProfileScreen includes a preview composable that demonstrates:
- Loading the profile with sample data
- Display of all user information fields
- Vehicle information display

### Sample Data in Preview
```kotlin
DriverDto(
    id = "DL-99887766",
    fullName = "Dao Ackileo",
    email = "ackileo.doe@telematics.com",
    phone = "+256770567890",
    vehicle = VehicleDto(
        id = "VEH-123456",
        make = "Toyota",
        model = "Camry",
        plateNumber = "UG 123 ABC"
    )
)
```

## Usage

### From Other Screens
ProfileScreen is automatically navigated to via bottom navigation:
```kotlin
// In NavGraph
composable(Screen.Profile.route) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    ProfileScreen(
        navController = navController,
        viewModel = profileViewModel,
        onLogout = {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}
```

### Profile Data Access
ProfileViewModel automatically loads user data on creation via init block:
```kotlin
init {
    loadUserProfile()
}
```

The LaunchedEffect in the stateful ProfileScreen monitors action state changes:
```kotlin
LaunchedEffect(actionState.value) {
    if (actionState.value == AuthState.Success) {
        viewModel.resetActionState()
        onLogout()  // Triggers navigation
    }
}
```

## Dependency Injection

### Hilt Setup
- `ProfileViewModel`: Auto-injected via `@HiltViewModel`
- `ProfileRepository`: Bound in `RepositoryModule`
- Required dependencies: `ApiService`, `TokenManager`, `SharedPrefManager`, `FirebaseAuth`

### Singleton Scope
- `ProfileRepositoryImpl` is a `@Singleton`
- Ensures consistent state across app lifecycle
- Automatic lifecycle management by Hilt

## Architecture Overview

```
ProfileScreen (Stateful)
    ↓
ProfileViewModel (Hilt-injected)
    ↓
ProfileRepository (Interface)
    ↓
ProfileRepositoryImpl (Singleton)
    ↓
ApiService (Retrofit interface)
    ↓
JwtInterceptor (Auto-injects token)
    ↓
Backend API: GET /api/auth/me
```

## Future Enhancements

Possible extensions to this implementation:
1. **Edit Profile**: Implement the "Edit Profile" button to update user information
2. **Change Password**: Implement password change functionality
3. **Profile Picture**: Add profile picture upload capability
4. **Offline Support**: Cache profile data for offline access
5. **Auto-refresh**: Periodically refresh profile data
6. **License Information**: Add license expiry and class details to DriverDto

## Compliance with Requirements

✅ **Retrieve authenticated user using GET /api/auth/me**
- Implemented in `ProfileRepository.getCurrentUser()`

✅ **Display user information**
- Name (fullName)
- Email
- Driver information (ID)
- Vehicle information where available

✅ **Logout functionality that:**
1. ✅ JWT removed from local storage (via TokenManager.clearAuthState())
2. ✅ Authentication state becomes logged out (AuthState.Idle)
3. ✅ Cached sensitive user information cleared (via SharedPrefManager.clearAll())
4. ✅ User navigated to Login (via NavGraph popUpTo)
5. ✅ Subsequent protected API requests don't contain old token (via JwtInterceptor checking tokenManager.getAccessToken())

✅ **No passwords stored**
- Only JWT tokens in EncryptedSharedPreferences

✅ **Uses existing AuthViewModel/AuthRepository architecture**
- ProfileViewModel follows same patterns as AuthViewModel
- ProfileRepository follows same patterns as AuthRepository
- Integrated via RepositoryModule Hilt binding

