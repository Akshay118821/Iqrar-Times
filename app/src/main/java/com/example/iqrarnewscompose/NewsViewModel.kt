package com.example.iqrarnewscompose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iqrarnewscompose.api.ApiNewsArticle
import com.example.iqrarnewscompose.api.NewsRepository
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val repo = NewsRepository()

    val newsList = mutableStateListOf<ApiNewsArticle>()
    val categories = mutableStateListOf<CategoryItem>()

    var isLoading = mutableStateOf(false)

    private val newsCache = mutableMapOf<String, List<ApiNewsArticle>>()

    fun loadCategories(lang: String) {
        viewModelScope.launch {
            try {
                val response = repo.fetchCategories(lang)
                categories.clear()
                categories.addAll(response.data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadNews(category: String, onDataLoaded: () -> Unit = {}) {
        viewModelScope.launch {
            if (newsCache.containsKey(category)) {
                newsList.clear()
                newsList.addAll(newsCache[category]!!)
                onDataLoaded()
                return@launch
            }
            try {
                isLoading.value = true
                newsList.clear()
                val data = if (category == "Home" || category == "HOME") {
                    repo.getAllNews()
                } else {
                    repo.getNewsByCategory(category)
                }
                newsCache[category] = data
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
                val data = if (category == "Home" || category == "HOME") {
                    repo.getAllNews()
                } else {
                    repo.getNewsByCategory(category)
                }
                data.forEach {
                    android.util.Log.d("VIDEO_DEBUG", "youtube: ${it.youtube_url}, video: ${it.video}")
                }
                onDataLoaded(data)
            } catch (e: Exception) {
                e.printStackTrace()
                onDataLoaded(emptyList())
            }
        }
    }
}