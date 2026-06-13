package com.example.data.entity

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val recipient: String? = null,
    val category: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
