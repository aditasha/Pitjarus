package com.aditasha.pitjarus.data.repository

import com.aditasha.pitjarus.data.network.RetrofitApiService
import com.aditasha.pitjarus.data.room.StoreDao
import com.aditasha.pitjarus.domain.model.LoginResponse
import com.aditasha.pitjarus.domain.model.Result
import com.aditasha.pitjarus.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val retrofitApiService: RetrofitApiService,
    private val storeDao: StoreDao
) : LoginRepository {
    override fun login(username: String, password: String): Flow<Result<Any>> = flow {
        try {
            emit(Result.Loading)
            val result = retrofitApiService.login(username, password)
            if (result.isSuccessful) {
                val loginResponse = result.body() as LoginResponse
                if (loginResponse.stores != null) {
                    val entityList = DataMapper.mapResponseToEntity(loginResponse.stores)
                    storeDao.deleteAllStore()
                    storeDao.insertStoreList(entityList)
                    emit(Result.Success(Any()))
                } else emit(Result.Error(loginResponse.message.toString()))
            } else {
                val jsonObj = JSONObject(result.errorBody()!!.charStream().readText())
                val message = jsonObj.getString("message")
                emit(Result.Error(message))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(e.localizedMessage!!))
        }
    }
}