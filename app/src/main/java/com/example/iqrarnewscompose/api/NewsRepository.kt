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

    suspend fun fetchComments(newsId: String): List<Comment> {
        return try {
            val response = api.getNewsComments(newsId)
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

    // 🔥 ఇక్కడ మార్పు చేశాను - ఎర్రర్ రాకుండా 'date' ని కూడా యాడ్ చేశాను
    suspend fun fetchEPaper(lang: String, date: String): List<EPaperItem> {
        return try {
            // పైన పారామీటర్ లో ఉన్న 'date' ని ఇక్కడ api కి పంపిస్తున్నాం
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