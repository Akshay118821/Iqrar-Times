package com.example.iqrarnewscompose.api

import retrofit2.Response

// 🔥 డేటా మోడల్స్
data class EPaperListResponse(
    val success: Boolean? = null,
    val data: List<EPaperItem>? = null
)

data class EPaperItem(
    val image: String? = null,
    val date: String? = null,
    val page_number: Int? = null
)

class NewsRepository {

    private val api = RetrofitInstance.api

    suspend fun getAllNews(langParam: String): List<ApiNewsArticle> {
        return try {
            val response = api.getAllNews(langParam)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNewsByCategory(categoryId: String, langParam: String): List<ApiNewsArticle> {
        return try {
            val response = api.getNewsByCategory(categoryId, langParam)
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

    // ✅ FIXED: Takes token now
    suspend fun fetchComments(token: String, newsId: String): List<Comment> {
        return try {
            val response = api.getNewsComments("Bearer $token", newsId)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun postComment(token: String, newsId: String, comment: String): Boolean {
        return try {
            val response = api.postComment("Bearer $token", PostCommentRequest(newsId, comment))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // ✅ FIXED: Restored fetchEPaper which was accidentally deleted
    suspend fun fetchEPaper(lang: String, date: String): List<EPaperItem> {
        return try {
            val response = api.getEPaper(lang, date)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}