package com.example.studyswipe.network.api

import com.example.studyswipe.network.dto.LoginRequest
import com.example.studyswipe.network.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
