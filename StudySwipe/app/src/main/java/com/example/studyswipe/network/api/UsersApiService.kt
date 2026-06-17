package com.example.studyswipe.network.api

import com.example.studyswipe.network.dto.UsersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UsersApiService {
    @GET("api/users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): UsersResponse
}