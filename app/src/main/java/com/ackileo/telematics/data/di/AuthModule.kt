package com.ackileo.telematics.data.di




import dagger.Module

import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    companion object {
        // This provides the external Firebase dependency

    }
}