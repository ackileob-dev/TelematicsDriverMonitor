package com.ackileo.telematics.domain.model

import com.ackileo.telematics.data.repository.AuthRepository
import com.ackileo.telematics.data.repository.AuthRepositoryImpl
import com.ackileo.telematics.data.repository.TripRepository
import com.ackileo.telematics.data.repository.TripRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}