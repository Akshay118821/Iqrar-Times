package com.example.iqrarnewscompose.api

class NewsRepository {

    private val api = RetrofitInstance.api

    // 🔥 GET ALL NEWS
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

    // 🔥 FETCH CATEGORIES
    suspend fun fetchCategories(lang: String) =
        api.getCategories(lang)
}
