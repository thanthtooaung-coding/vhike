package com.vinn.vhike.di

import android.content.Context
import com.vinn.vhike.data.db.HikeDao
import com.vinn.vhike.data.db.HikeDatabase
import com.vinn.vhike.data.repository.HikeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHikeDatabase(@ApplicationContext appContext: Context): HikeDatabase {
        return HikeDatabase.getDatabase(appContext)
    }

    @Provides
    @Singleton
    fun provideHikeDao(database: HikeDatabase): HikeDao {
        return database.hikeDao()
    }

    @Provides
    @Singleton
    fun provideHikeRepository(hikeDao: HikeDao): HikeRepository {
        return HikeRepository(hikeDao)
    }
}