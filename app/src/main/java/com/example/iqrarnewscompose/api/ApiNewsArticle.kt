package com.example.iqrarnewscompose.api

import com.google.gson.annotations.SerializedName

data class ApiNewsArticle(

    @SerializedName("id")
    val id: String?,

    @SerializedName("title")
    val name: String?,

    @SerializedName("image")
    val image: List<String>?,

    @SerializedName("created_at")
    val date: String?,

    @SerializedName("created_by")
    val author: String?,

    @SerializedName("description")
    val content: String?,

    @SerializedName("categories")
    val categories: List<String>?,

    @SerializedName("video")
    val video: List<String>?,

    @SerializedName("youtube_url")
    val youtube_url: List<String>?,

    @SerializedName("views_count")
    val viewcount: Int?

) {

    val category_name: String
        get() = categories?.firstOrNull() ?: ""

    val icon: String
        get() = image?.firstOrNull() ?: ""

    val formattedViewCount: String
        get() {
            val count = viewcount ?: 0
            if (count >= 1000000) return String.format(java.util.Locale.US, "%.1fM", count / 1000000.0).replace(".0M", "M")
            if (count >= 1000) return String.format(java.util.Locale.US, "%.1fK", count / 1000.0).replace(".0K", "K")
            return count.toString()
        }
}