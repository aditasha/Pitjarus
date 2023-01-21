package com.aditasha.pitjarus.domain.repository

import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import kotlinx.coroutines.flow.Flow

interface StoreListRepository {
    fun fetchStoreList(): Flow<Result<List<StoreEntity>>>
}