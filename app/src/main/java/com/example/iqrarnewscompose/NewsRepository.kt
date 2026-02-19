package com.example.iqrarnewscompose.api

class NewsRepository {

    private val api = RetrofitInstance.api


    suspend fun getAllNews(): List<ApiNewsArticle> {
        return try {
            val response = api.getAllNews()
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend  fun getNewsByCategory(categoryId: String): List<ApiNewsArticle> {
        return try {
            val response = api.getNewsByCategory(categoryId)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun createNews(newsData: Map<String, Any>): Boolean {
        return try {
            val payload = newsData.toMutableMap()
            payload["status"] = "Pending"

            val response = api.createNews(payload)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }


    suspend fun fetchCategories(lang: String) =
        api.getCategories(lang)
}
