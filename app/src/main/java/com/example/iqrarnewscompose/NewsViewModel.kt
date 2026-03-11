package com.example.iqrarnewscompose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iqrarnewscompose.api.ApiNewsArticle
import com.example.iqrarnewscompose.api.Comment
import com.example.iqrarnewscompose.api.NewsRepository
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val repo = NewsRepository()

    val newsList = mutableStateListOf<ApiNewsArticle>()
    val categories = mutableStateListOf<CategoryItem>()
    val commentsList = mutableStateListOf<Comment>()
    val epaperList = mutableStateListOf<com.example.iqrarnewscompose.api.EPaperItem>()

    var isLoading = mutableStateOf(false)
    private val newsCache = mutableMapOf<String, List<ApiNewsArticle>>()

    fun loadCategories(lang: String) {
        viewModelScope.launch {
            try {
                val response = repo.fetchCategories(lang)
                val priorityCategories = response.data
                    .filter { it.priority!! > 0 }
                    .sortedBy { it.priority }
                val zeroPriorityCategories = response.data
                    .filter { it.priority == 0 }
                val finalSortedCategories = priorityCategories + zeroPriorityCategories
                categories.clear()
                categories.addAll(finalSortedCategories)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadNews(category: String, langParam: String, onDataLoaded: () -> Unit = {}) {
        viewModelScope.launch {
            val cacheKey = "$category-$langParam"
            if (newsCache.containsKey(cacheKey)) {
                newsList.clear()
                newsList.addAll(newsCache[cacheKey]!!)
                onDataLoaded()
                return@launch
            }
            try {
                isLoading.value = true
                newsList.clear()
                val data = if (category == "Home" || category == "HOME" || category == "") {
                    repo.getAllNews(langParam)
                } else {
                    repo.getNewsByCategory(category, langParam)
                }
                newsCache[cacheKey] = data
                newsList.addAll(data)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
                onDataLoaded()
            }
        }
    }

    fun loadNewsSeparate(category: String, onDataLoaded: (List<ApiNewsArticle>) -> Unit) {
        viewModelScope.launch {
            try {
                val data = repo.getAllNews("HINDI")
                onDataLoaded(data)
            } catch (e: Exception) {
                onDataLoaded(emptyList())
            }
        }
    }

    // ✅ Signature fixed
    fun loadComments(token: String, newsId: String) {
        viewModelScope.launch {
            try {
                val data = repo.fetchComments(token, newsId)
                commentsList.clear()
                commentsList.addAll(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ FIXED: Passing token to loadComments after posting
    fun postComment(token: String, newsId: String, comment: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repo.postComment(token, newsId, comment)
                if (success) {
                    loadComments(token, newsId) // 🔥 Huna token kuda pass cheyali ikkada
                }
                onResult(success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun loadEPaper(lang: String, date: String) {
        viewModelScope.launch {
            try {
                epaperList.clear()
                val data = repo.fetchEPaper(lang, date)
                val filteredData = data.filter { it.date == date }
                epaperList.addAll(filteredData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}