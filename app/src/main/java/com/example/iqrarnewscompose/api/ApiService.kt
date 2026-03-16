package com.example.iqrarnewscompose.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import com.example.iqrarnewscompose.CategoryResponse
import retrofit2.http.Header

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

    @GET("news/news/comment")
    suspend fun getNewsComments(
        @Header("Authorization") token: String,    //  ADD THIS
        @Query("news_id") newsId: String
    ): Response<CommentResponse>

    @POST("news/news/comment")
    suspend fun postComment(
        @Header("Authorization") token: String,
        @Body request: PostCommentRequest
    ): Response<CommonResponse>

    // 🔥 ఇక్కడ మార్పు చేశాను - పాత లైన్ ని దీనితో రీప్లేస్ చేశాను
    @GET("backoffice/epaper")
    suspend fun getEPaper(
        @Query("language") language: String,
        @Query("date") date: String
    ): Response<EPaperListResponse>
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

data class PostCommentRequest(
    val news_id: String,
    val comment: String
)


// ------------------------------------
// COMMON RESPONSE MODEL
// ------------------------------------

data class CommonResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val token: String? = null,
    val data: TokenData? = null
)

data class TokenData(
    val token: String? = null,
    val accessToken: String? = null,
    val auth_token: String? = null,
    val access: String? = null,
    val refresh: String? = null,
    val username: String? = null,
    val id: String? = null,
    val user_id: String? = null,
    val name: String? = null
)

data class CommentResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: List<Comment>? = null
)

data class Comment(
    val id: String? = null,
    val user_name: String? = null,
    val comment: String? = null,
    val created_at: String? = null
)