package com.ackileo.telematics.data.di
import com.ackileo.telematics.data.local.SessionStateStore
import com.ackileo.telematics.data.local.TokenManager
import com.ackileo.telematics.utils.DrivingSensorManager
import com.ackileo.telematics.utils.DrivingSensorManagerPort
import com.ackileo.telematics.utils.LocationTracker
import com.ackileo.telematics.utils.LocationTrackerPort
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule { // Changed from 'object' to 'abstract class'

    @Binds
    @Singleton
    abstract fun bindSessionStateStore(tokenManager: TokenManager): SessionStateStore

    @Binds
    @Singleton
    abstract fun bindLocationTrackerPort(locationTracker: LocationTracker): LocationTrackerPort

    @Binds
    @Singleton
    abstract fun bindDrivingSensorManagerPort(sensorManager: DrivingSensorManager): DrivingSensorManagerPort


    // ========================================================================
    // 2. STATIC PROVIDERS (@Provides)
    // Placed in a companion object to allow @Binds and @Provides in the same module.
    // ========================================================================

    companion object {


        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }
    }
}