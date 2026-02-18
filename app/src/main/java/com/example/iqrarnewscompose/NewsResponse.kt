package com.example.iqrarnewscompose.api

import com.google.gson.annotations.SerializedName
import com.example.iqrarnewscompose.api.NewsRepository


data class NewsResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<ApiNewsArticle>?
)
