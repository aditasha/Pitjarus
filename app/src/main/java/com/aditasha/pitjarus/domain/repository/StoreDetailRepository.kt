package com.aditasha.pitjarus.domain.repository

import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import kotlinx.coroutines.flow.Flow

interface StoreDetailRepository {
    fun fetchStore(roomId: Int): Flow<Result<StoreEntity>>
}