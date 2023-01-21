package com.aditasha.pitjarus.domain.repository

import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import kotlinx.coroutines.flow.Flow

interface StoreVisitRepository {
    fun fetchStore(roomId: Int): Flow<Result<StoreEntity>>
    fun updatePicture(roomId: Int, picture: String): Flow<Result<Any>>
    fun updateVisited(roomId: Int, visited: Boolean, lastVisited: Long): Flow<Result<Long>>
}