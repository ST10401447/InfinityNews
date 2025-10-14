package com.example.infinitynews.models

data class News(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val category: String,
    var isBookmarked: Boolean = false,
    val url: String? = null
)
