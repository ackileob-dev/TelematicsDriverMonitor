package com.ackileo.telematics.data.di

// Use your actual DI package name

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // You might also need Firestore or Storage later:
    // @Provides
    // @Singleton
    // fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}