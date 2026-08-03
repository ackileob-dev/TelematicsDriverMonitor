package com.ackileo.telematics.data.di
import com.ackileo.telematics.data.local.AppDatabase
import android.content.Context
import androidx.room.Room
import com.ackileo.telematics.data.local.dao.DriverDao
import com.ackileo.telematics.data.local.dao.TripDao
 // Ensure this is imported
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "telematics_db"
        ).fallbackToDestructiveMigration() // Recommended during development
            .build()
    }

    @Provides
    @Singleton //  can be singletons as well
    fun provideTripDao(db: AppDatabase): TripDao {
        return db.tripDao()
    }

    @Provides
    @Singleton
    fun provideDriverDao(db: AppDatabase): DriverDao {
        return db.driverDao()
    }
}


