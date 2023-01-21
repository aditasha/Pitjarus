package com.aditasha.pitjarus.data.repository

import com.aditasha.pitjarus.data.room.StoreDao
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import com.aditasha.pitjarus.domain.repository.StoreDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StoreDetailRepositoryImpl @Inject constructor(private val storeDao: StoreDao) :
    StoreDetailRepository {

    override fun fetchStore(roomId: Int): Flow<Result<StoreEntity>> = flow {
        try {
            emit(Result.Loading)
            val entity = storeDao.fetchStore(roomId)
            emit(Result.Success(entity))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }
}