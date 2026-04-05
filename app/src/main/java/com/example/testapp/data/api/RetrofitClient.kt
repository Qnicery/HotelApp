package com.example.testapp.data.api

import com.example.testapp.data.api.model.UserCreateRequest
import com.example.testapp.data.api.model.UserLoginRequest
import com.example.testapp.data.api.model.UserLoginResponse
import com.example.testapp.data.api.model.UserResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit клиент для работы с API
 * 
 * Base URL: http://localhost:8080
 * Для реального устройства использовать 10.0.2.2 (эмулятор Android)
 * или IP вашего компьютера
 */
object RetrofitClient {

    // Для эмулятора Android используйте 10.0.2.2 вместо localhost
    // Для физического устройства используйте IP вашего компьютера
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
}
