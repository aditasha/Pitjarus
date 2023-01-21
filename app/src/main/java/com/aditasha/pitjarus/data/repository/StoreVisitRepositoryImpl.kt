package com.aditasha.pitjarus.data.repository

import com.aditasha.pitjarus.data.room.StoreDao
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import com.aditasha.pitjarus.domain.repository.StoreVisitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StoreVisitRepositoryImpl @Inject constructor(private val storeDao: StoreDao) :
    StoreVisitRepository {

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

    override fun updatePicture(roomId: Int, picture: String): Flow<Result<Any>> = flow {
        try {
            emit(Result.Loading)
            storeDao.updatePicture(roomId, picture)
            emit(Result.Success(Any()))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }

    override fun updateVisited(
        roomId: Int,
        visited: Boolean,
        lastVisited: Long
    ): Flow<Result<Long>> = flow {
        try {
            emit(Result.Loading)
            storeDao.updateVisited(roomId, visited, lastVisited)
            emit(Result.Success(lastVisited))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }
}