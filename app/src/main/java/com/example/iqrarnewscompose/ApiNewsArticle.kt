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
    val youtube_url: List<String>?
) {
    val icon: String
        get() = image?.firstOrNull() ?: ""
}
