package com.example.iqrarnewscompose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iqrarnewscompose.api.ApiNewsArticle
import com.example.iqrarnewscompose.api.Comment
import com.example.iqrarnewscompose.api.NewsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewsViewModel : ViewModel() {

    private val repo = NewsRepository()

    val newsList = mutableStateListOf<ApiNewsArticle>()
    val categories = mutableStateListOf<CategoryItem>()
    val commentsList = mutableStateListOf<Comment>()
    val epaperList = mutableStateListOf<com.example.iqrarnewscompose.api.EPaperItem>()

    var isLoading = mutableStateOf(false)
    private val newsCache = mutableMapOf<String, List<ApiNewsArticle>>()

    // 🔥 FLIP NEWS STATE
    val flipNewsList = mutableStateListOf<ApiNewsArticle>()
    var isLoadingFlipNews = mutableStateOf(false)

    // 🔥 Track which date's ePaper is actually being shown
    var epaperActualDate = mutableStateOf("")
    var epaperIsSearching = mutableStateOf(false)

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

    // 🔥 PULL ALL SELECTED CATEGORIES FOR FLIP NEWS
    fun loadFlipNewsForSelectedCategories(selectedCategories: Set<String>, langParam: String) {
        viewModelScope.launch {
            try {
                isLoadingFlipNews.value = true
                flipNewsList.clear()
                
                if (selectedCategories.isEmpty()) {
                    // No categories selected, fallback to all news
                    val allNews = repo.getAllNews(langParam)
                    flipNewsList.addAll(allNews)
                } else {
                    // Fetch all selected categories concurrently
                    val fetchJobs = selectedCategories.map { categoryId ->
                        async {
                            repo.getNewsByCategory(categoryId, langParam)
                        }
                    }
                    val results: List<List<ApiNewsArticle>> = fetchJobs.awaitAll()
                    
                    // Flatten list and remove duplicates by ID
                    val combinedNews: List<ApiNewsArticle> = results.flatten().distinctBy { it.id }
                    
                    // Sort descending by date (assume string ISO dates sort alphabetically)
                    val sortedNews = combinedNews.sortedByDescending { it.date }
                    flipNewsList.addAll(sortedNews)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingFlipNews.value = false
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

    // 🔥 UPDATED: If today's ePaper not found, fallback to nearest previous date (up to 7 days back)
    fun loadEPaper(lang: String, date: String) {
        viewModelScope.launch {
            try {
                epaperList.clear()
                epaperActualDate.value = date
                epaperIsSearching.value = true

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.time = formatter.parse(date) ?: return@launch

                // Try the selected date first, then go back up to 7 days
                for (i in 0..7) {
                    val tryDate = formatter.format(calendar.time)
                    val data = repo.fetchEPaper(lang, tryDate)
                    val filteredData = data.filter { it.date == tryDate }

                    if (filteredData.isNotEmpty()) {
                        epaperList.addAll(filteredData)
                        epaperActualDate.value = tryDate
                        epaperIsSearching.value = false
                        return@launch
                    }

                    // Go back one day
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }

                // No data found in 7 days — list stays empty
                epaperIsSearching.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                epaperIsSearching.value = false
            }
        }
    }
}