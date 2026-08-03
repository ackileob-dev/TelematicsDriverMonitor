package com.ackileo.telematics.data.di
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule { // Changed from 'object' to 'abstract class'


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