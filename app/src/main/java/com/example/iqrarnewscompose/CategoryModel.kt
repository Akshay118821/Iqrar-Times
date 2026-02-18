package com.example.iqrarnewscompose

data class CategoryResponse(
    val success: Boolean,
    val data: List<CategoryItem>
)


data class CategoryItem(
    val id: String,
    val name: String,
    val parent_id: String? = null,
    val priority: Int? = 0
)

