package com.memora.di

import android.content.Context
import com.memora.data.db.MemoryDao
import com.memora.data.db.MemoryDatabase
import com.memora.data.repository.SearchRepository
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
    fun provideDatabase(@ApplicationContext context: Context): MemoryDatabase {
        return MemoryDatabase.getDatabase(context)
    }

    @Provides
    fun provideMemoryDao(database: MemoryDatabase): MemoryDao {
        return database.memoryDao()
    }

    @Provides
    @Singleton
    fun provideSearchRepository(memoryDao: MemoryDao): SearchRepository {
        return SearchRepository(memoryDao)
    }
}
