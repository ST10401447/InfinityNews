package com.example.infinitynews.models

data class NotificationItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val url: String,
    val timestamp: Long
)