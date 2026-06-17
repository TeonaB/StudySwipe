package com.example.studyswipe.network.dto

import com.google.gson.annotations.SerializedName

// Login request and response mappings for https://reqres.in/
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

// Users page response mapping
data class UsersResponse(
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int,
    val data: List<UserDTO>
)
