package com.aditasha.pitjarus.data.network

import com.aditasha.pitjarus.domain.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitApiService {
    @FormUrlEncoded
    @POST(".")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
}