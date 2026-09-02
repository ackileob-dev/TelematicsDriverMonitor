# Quick Implementation Guide: Profile Screen Integration

## Summary of Changes

This guide explains the Profile screen integration with backend authentication. The implementation follows the existing AuthViewModel/AuthRepository pattern while adding new ProfileViewModel and ProfileRepository for better separation of concerns.

## Files Added

### 1. ProfileViewModel.kt
- **Location**: `app/src/main/java/com/ackileo/telematics/ui/viewmodel/ProfileViewModel.kt`
- **Purpose**: Manages profile UI state and operations (load profile, logout)
- **Dependencies**: ProfileRepository, TokenManager, SharedPrefManager

### 2. ProfileRepository.kt  
- **Location**: `app/src/main/java/com/ackileo/telematics/data/repository/ProfileRepository.kt`
- **Purpose**: Handles API calls and auth state management
- **Key Methods**:
  - `getCurrentUser()`: GET /api/auth/me - fetches authenticated user profile
  - `logout()`: Clears all tokens and user data

## Files Modified

### 1. ProfileScreen.kt
- **What Changed**: Complete UI redesign to use real data from ProfileViewModel
- **Display Elements**:
  - User name and driver ID
  - Email and phone (Contact Details card)
  - Vehicle info: make, model, plate number (Vehicle Information card)
  - Logout button with loading state
- **States Handled**:
  - Loading: Shows spinner
  - Success: Shows user data
  - Error: Shows error message with retry button

### 2. NavGraph.kt
- **What Changed**: Updated Profile screen to use ProfileViewModel instead of AuthViewModel
- **Impact**: Profile screen now properly loads and displays user data

### 3. RepositoryModule.kt (Hilt DI)
- **What Changed**: Added ProfileRepository binding
- ```kotlin
  @Binds
  @Singleton
  abstract fun bindProfileRepository(
      profileRepositoryImpl: ProfileRepositoryImpl
  ): ProfileRepository
  ```

## How It Works

### User Profile Loading Flow
```
ProfileScreen loads
    ↓
ProfileViewModel init → calls loadUserProfile()
    ↓
ProfileRepository.getCurrentUser()
    ↓
ApiService.getCurrentUser() → GET /api/auth/me (with JWT bearer token)
    ↓
Backend returns DriverDto (with optional vehicle info)
    ↓
ProfileState.Success(driver)
    ↓
UI displays user information
```

### Logout Flow
```
User taps "Logout" button
    ↓
ProfileViewModel.logout()
    ↓
ActionState.Loading (button shows spinner)
    ↓
ProfileRepository.logout()
    ├─ TokenManager.clearAuthState() → removes JWT tokens
    ├─ SharedPrefManager.clearAll() → removes user profile data
    └─ FirebaseAuth.signOut()
    ↓
ActionState.Success
    ↓
LaunchedEffect triggers onLogout() callback
    ↓
NavGraph navigates to Login screen with popUpTo(0)
```

## API Endpoint

### GET /api/auth/me
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Response**:
  ```json
  {
    "success": true,
    "data": {
      "id": "driver_id",
      "fullName": "John Doe",
      "email": "john@example.com",
      "phone": "+1234567890",
      "vehicle": {
        "id": "vehicle_id",
        "make": "Toyota",
        "model": "Camry",
        "plateNumber": "ABC123"
      }
    }
  }
  ```

## Security Features

1. **JWT Token Handling**
   - Automatically injected by JwtInterceptor
   - Stored in EncryptedSharedPreferences with AES256-GCM
   - Cleared on logout via TokenManager.clearAuthState()

2. **Password Security**
   - Passwords NEVER stored locally
   - Only JWT tokens used for authentication

3. **Logout Protection**
   - All tokens cleared from encrypted storage
   - All user data cleared from preferences
   - JwtInterceptor checks for token before adding auth header
   - No old token in subsequent requests

4. **401 Handling**
   - Automatic logout and state clear on unauthorized responses
   - User returned to login screen

## Data Models

### DriverDto (from backend)
```kotlin
data class DriverDto(
    val id: String,                    // Driver ID
    val fullName: String?,             // Driver's name
    val email: String?,                // Email address
    val phone: String?,                // Phone number
    val vehicle: VehicleDto? = null    // Associated vehicle (optional)
)
```

### VehicleDto (from backend)
```kotlin
data class VehicleDto(
    val id: String,              // Vehicle ID
    val make: String?,           // Manufacturer (Toyota, Honda, etc.)
    val model: String?,          // Model name (Camry, Accord, etc.)
    val plateNumber: String?     // License plate
)
```

## Testing

### Preview the Profile Screen
The ProfileScreen includes a preview that shows how it looks with data:
- Open `ProfileScreen.kt`
- See `ProfileScreenPreview()` at the bottom
- Uses sample data for all fields

### Manual Testing Checklist
- [ ] App loads Profile screen after login
- [ ] User data displays correctly (name, email, phone)
- [ ] Vehicle information shows if available
- [ ] Logout button works and navigates to login
- [ ] Logout clears all cached data
- [ ] Network errors show retry button
- [ ] Loading spinner appears during logout

## Troubleshooting

### Profile Data Not Loading
1. Check JWT token is valid (not expired)
2. Verify backend /api/auth/me endpoint returns correct format
3. Check network connectivity
4. Look for error state in UI - tap Retry

### Logout Not Working
1. Check TokenManager.clearAuthState() is being called
2. Verify SharedPrefManager.clearAll() clears preferences
3. Ensure NavGraph popUpTo(0) clears back stack
4. Check Firebase sign out not throwing errors

### UI Not Updating
1. Check ProfileViewModel state is being collected
2. Verify LaunchedEffect is monitoring actionState
3. Ensure ProfileScreen is using ProfileViewModel, not AuthViewModel

### Missing Vehicle Information
- This is expected if backend returns null for vehicle
- UI gracefully handles this with conditional rendering
- "No vehicle information available" message shown

## Future Enhancements

1. **Edit Profile**: Implement button to update user information
2. **Profile Picture**: Add avatar upload functionality
3. **Change Password**: Add password change feature
4. **License Details**: Extend DriverDto with license expiry, class
5. **Auto-Refresh**: Periodically refresh profile data
6. **Offline Support**: Cache profile data for offline viewing

## Integration Checklist

- ✅ ProfileViewModel created and uses ProfileRepository
- ✅ ProfileRepository implements current user fetch with error handling
- ✅ ProfileScreen updated to display real data
- ✅ Logout properly clears tokens and navigates to login
- ✅ ProfileRepository bound in RepositoryModule
- ✅ NavGraph uses ProfileViewModel for Profile screen
- ✅ No passwords stored (only JWT tokens)
- ✅ Error and loading states handled in UI
- ✅ JwtInterceptor ensures old tokens not sent after logout

## How to Verify Implementation

### 1. Check Dependencies Injected
Run the app and check console for Hilt injection messages:
- ProfileViewModel created
- ProfileRepository singleton bound
- ApiService available

### 2. Monitor API Calls
Enable network logging to see:
- GET /api/auth/me request with Bearer token
- Success response with user data

### 3. Check Local Storage Cleared
After logout, verify:
- TokenManager.getAccessToken() returns null
- SharedPrefManager.getUserName() returns null
- SharedPrefManager.getUserEmail() returns null

### 4. Verify Navigation
After logout:
- NavController navigates to Login route
- Back stack is cleared (can't navigate back to Profile)

## Code Examples

### How ProfileViewModel Uses ProfileRepository
```kotlin
fun loadUserProfile() {
    viewModelScope.launch {
        _profileState.value = ProfileState.Loading
        val result = repository.getCurrentUser()
        result.fold(
            onSuccess = { driver ->
                _profileState.value = ProfileState.Success(driver)
            },
            onFailure = { error ->
                _profileState.value = ProfileState.Error(error.message ?: "Failed")
            }
        )
    }
}
```

### How ProfileScreen Uses ProfileViewModel
```kotlin
val profileState = viewModel.profileState.collectAsState()
val actionState = viewModel.actionState.collectAsState()

LaunchedEffect(actionState.value) {
    if (actionState.value == AuthState.Success) {
        viewModel.resetActionState()
        onLogout()  // Navigate to login
    }
}
```

### How ProfileRepository Handles Logout
```kotlin
override fun logout() {
    tokenManager.clearAuthState()      // Clear JWT tokens
    prefManager.clearAll()              // Clear user profile data
    firebaseAuth.signOut()              // Clear Firebase auth
}
```

## Architecture Pattern

The implementation follows a clean architecture pattern:

```
UI Layer (Composable)
    └─ ProfileScreen
        └─ ProfileViewModel (StateFlow based)
            └─ ProfileRepository (Interface)
                └─ ProfileRepositoryImpl (Implementation)
                    ├─ ApiService (Retrofit)
                    ├─ TokenManager (Secure Storage)
                    └─ SharedPrefManager (User Cache)
```

This ensures:
- Separation of concerns
- Testability (mock repositories)
- Reusability (repository used by multiple VMs)
- Type safety (Kotlin + Hilt)
- Reactive updates (StateFlow)

## Support

For issues or questions:
1. Check PROFILE_INTEGRATION.md for detailed documentation
2. Verify all files are in correct directories
3. Ensure Hilt is properly configured
4. Check API endpoint returns correct format
5. Review error messages in Logcat

