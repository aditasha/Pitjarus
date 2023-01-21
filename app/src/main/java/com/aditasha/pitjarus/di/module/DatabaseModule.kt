package com.aditasha.pitjarus.di.module

import android.content.Context
import androidx.room.Room
import com.aditasha.pitjarus.data.room.StoreDao
import com.aditasha.pitjarus.data.room.StoreDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideStoreDatabase(@ApplicationContext context: Context): StoreDatabase {
        return Room.databaseBuilder(context, StoreDatabase::class.java, "Store_Room_Db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideStoreDao(db: StoreDatabase): StoreDao {
        return db.storeDao()
    }
}