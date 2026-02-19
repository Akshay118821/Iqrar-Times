package com.example.iqrarnewscompose.api

import retrofit2.Response
import retrofit2.http.*
import com.example.iqrarnewscompose.CategoryResponse

interface ApiService {


    @GET("news/news")
    suspend fun getAllNews(): Response<NewsResponse>


    @GET("news/news")
    suspend fun getNewsByCategory(
        @Query("category") categoryId: String
    ): Response<NewsResponse>

    @POST("backoffice/news")
    suspend fun createNews(
        @Body newsData: Map<String, Any>
    ): Response<NewsResponse>

    @GET("backoffice/category/")
    suspend fun getCategories(
        @Query("language") language: String
    ): CategoryResponse
}
