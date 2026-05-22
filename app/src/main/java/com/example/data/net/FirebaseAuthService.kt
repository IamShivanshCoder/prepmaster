package com.example.data.net

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class FirebaseSignInResponse(
    @Json(name = "idToken") val idToken: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "registered") val registered: Boolean = false,
    @Json(name = "error") val error: FirebaseErrorBody? = null
)

@JsonClass(generateAdapter = true)
data class FirebaseErrorBody(
    @Json(name = "code") val code: Int = 0,
    @Json(name = "message") val message: String = ""
)

interface FirebaseAuthService {
    @FormUrlEncoded
    @POST("v1/accounts:signInWithPassword")
    suspend fun signInWithPassword(
        @Query("key") apiKey: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("returnSecureToken") returnSecureToken: Boolean = true
    ): FirebaseSignInResponse

    companion object {
        fun create(): FirebaseAuthService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://identitytoolkit.googleapis.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            return retrofit.create(FirebaseAuthService::class.java)
        }
    }
}
