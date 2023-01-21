package com.aditasha.pitjarus.domain.repository

import com.aditasha.pitjarus.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface LoginRepository {
    fun login(username: String, password: String): Flow<Result<Any>>
}