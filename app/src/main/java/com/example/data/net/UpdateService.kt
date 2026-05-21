package com.example.data.net

import com.example.data.model.RemoteConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface UpdateService {
    @GET
    suspend fun fetchRemoteConfig(@Url url: String): RemoteConfig

    companion object {
        fun create(): UpdateService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                // Base URL is required by Retrofit, but we can pass dynamic full URLs to @Url
                .baseUrl("https://raw.githubusercontent.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(UpdateService::class.java)
        }
    }
}
