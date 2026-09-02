package com.ackileo.telematics.domain.model

import com.ackileo.telematics.data.repository.AuthRepository
import com.ackileo.telematics.data.repository.AuthRepositoryImpl
import com.ackileo.telematics.data.repository.DashboardRepository
import com.ackileo.telematics.data.repository.DashboardRepositoryImpl
import com.ackileo.telematics.data.repository.ProfileRepository
import com.ackileo.telematics.data.repository.ProfileRepositoryImpl
import com.ackileo.telematics.data.repository.RewardsSafetyAlertsRepository
import com.ackileo.telematics.data.repository.RewardsSafetyAlertsRepositoryImpl
import com.ackileo.telematics.data.repository.TrackingRepository
import com.ackileo.telematics.data.repository.TrackingRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindTrackingRepository(
        trackingRepositoryImpl: TrackingRepositoryImpl
    ): TrackingRepository

    @Binds
    @Singleton
    abstract fun bindRewardsSafetyAlertsRepository(
        repositoryImpl: RewardsSafetyAlertsRepositoryImpl
    ): RewardsSafetyAlertsRepository
}