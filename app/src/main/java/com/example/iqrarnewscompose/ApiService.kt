package com.example.iqrarnewscompose.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query
import com.example.iqrarnewscompose.CategoryResponse
import com.example.iqrarnewscompose.api.NewsResponse

// IDHI CORRECT PLACE INTERFACE KI
interface ApiService {

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
    suspend fun createNews(@Body newsData: Map<String, Any>): Response<Map<String, Any>>
}