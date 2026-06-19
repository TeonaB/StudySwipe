package com.example.studyswipe.network

import com.example.studyswipe.network.api.AuthApiService
import com.example.studyswipe.network.api.UsersApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://reqres.in/"

    private const val LOCAL_BASE_URL = "http://10.0.2.2:3000"
    private const val API_KEY = "pub_8bbc4417fc23e1363baae08d2e3ddb16df0e97a7501c6765c612325d8d17acce"

    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain
            .request()
            .newBuilder()
            .addHeader("x-api-key", API_KEY)
            .build()
        chain.proceed(request)
    }

    private val authTokenInterceptor = Interceptor { chain ->
        // Temporarily mocked until AuthDataStore / SharedPreferences is implemented
        val token = "" 
        val request = chain
            .request()
            .newBuilder()
            .addHeader("Authorization", token)
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val okHttpClientAuthorized = OkHttpClient
        .Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(authTokenInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val usersApi : UsersApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientAuthorized)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsersApiService::class.java)
    }

    val usersLocalApi : UsersApiService by lazy {
        Retrofit.Builder()
            .baseUrl(LOCAL_BASE_URL)
            .client(okHttpClientAuthorized)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsersApiService::class.java)
    }

    val authApi : AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}