package com.aditasha.pitjarus.di.module

import com.aditasha.pitjarus.data.repository.LoginRepositoryImpl
import com.aditasha.pitjarus.data.repository.StoreDetailRepositoryImpl
import com.aditasha.pitjarus.data.repository.StoreListRepositoryImpl
import com.aditasha.pitjarus.data.repository.StoreVisitRepositoryImpl
import com.aditasha.pitjarus.domain.repository.LoginRepository
import com.aditasha.pitjarus.domain.repository.StoreDetailRepository
import com.aditasha.pitjarus.domain.repository.StoreListRepository
import com.aditasha.pitjarus.domain.repository.StoreVisitRepository
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
    abstract fun bindLoginRepository(loginRepositoryImpl: LoginRepositoryImpl): LoginRepository

    @Binds
    @Singleton
    abstract fun bindStoreListRepository(storeListRepositoryImpl: StoreListRepositoryImpl): StoreListRepository

    @Binds
    @Singleton
    abstract fun bindStoreVisitRepository(storeVisitRepositoryImpl: StoreVisitRepositoryImpl): StoreVisitRepository

    @Binds
    @Singleton
    abstract fun bindStoreDetailRepository(storeDetailRepositoryImpl: StoreDetailRepositoryImpl): StoreDetailRepository
}