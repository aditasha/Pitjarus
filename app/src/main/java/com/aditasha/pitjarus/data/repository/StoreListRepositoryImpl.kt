package com.aditasha.pitjarus.data.repository

import com.aditasha.pitjarus.data.room.StoreDao
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.model.StoreEntity
import com.aditasha.pitjarus.domain.repository.StoreListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StoreListRepositoryImpl @Inject constructor(
    private val storeDao: StoreDao
) : StoreListRepository {

    override fun fetchStoreList(): Flow<Result<List<StoreEntity>>> = flow {
        try {
            emit(Result.Loading)
            val storeList = storeDao.fetchAllStore()
            emit(Result.Success(storeList))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }
}