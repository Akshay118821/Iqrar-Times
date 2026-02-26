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
                newsList.clear() // Clear old data first

                // 2. Repo ki Language Pass Chestunnam
                val data = if (category == "Home" || category == "HOME" || category == "") {
                    // ⚠️ IMPORTANT: Repository function must accept language
                    repo.getAllNews(langParam)
                } else {
                    repo.getNewsByCategory(category, langParam)
                }

                // 3. Save to cache with language key
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

    // ... Video load function ...
    fun loadNewsSeparate(category: String, onDataLoaded: (List<ApiNewsArticle>) -> Unit) {
        // Indhulo kuda avasaram aithe lang param add cheyali later
        viewModelScope.launch {
            try {
                val data = repo.getAllNews("HINDI") // Default HINDI for now
                onDataLoaded(data)
            } catch (e: Exception) {
                onDataLoaded(emptyList())
            }
        }
    }
}