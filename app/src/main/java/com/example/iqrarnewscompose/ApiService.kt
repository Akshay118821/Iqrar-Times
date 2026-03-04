package com.example.iqrarnewscompose.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import com.example.iqrarnewscompose.CategoryResponse

// ------------------------------------
// API SERVICE INTERFACE
// ------------------------------------
interface ApiService {

    // -------------------------------
    // NEWS APIs
    // -------------------------------

    @GET("news/news")
    suspend fun getAllNews(
        @Query("language") language: String
    ): Response<NewsResponse>

    @GET("news/news")
    suspend fun getNewsByCategory(
        @Query("category") categoryId: String,
        @Query("language") language: String
    ): Response<NewsResponse>

    @GET("news/category")
    suspend fun getCategories(
        @Query("language") language: String
    ): CategoryResponse

    @POST("news/create-news")
    suspend fun createNews(
        @Body newsData: Map<String, Any>
    ): Response<Map<String, Any>>



    // -------------------------------
    // AUTH APIs (LOGIN WITH EMAIL)
    // -------------------------------

    @POST("user/send-email-otp")
    suspend fun sendEmailOtp(
        @Body request: SendOtpRequest
    ): Response<CommonResponse>


    @POST("user/verify-email-otp")
    suspend fun verifyEmailOtp(
        @Body request: VerifyOtpRequest
    ): Response<CommonResponse>
}



// ------------------------------------
// REQUEST MODELS
// ------------------------------------

data class SendOtpRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)


// ------------------------------------
// COMMON RESPONSE MODEL
// ------------------------------------

data class CommonResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val token: String? = null
)