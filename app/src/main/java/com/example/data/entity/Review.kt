package com.example.data.entity

data class Review(
    val id: String = "",
    val providerId: String = "",
    val rating: Float = 5.0f,
    val comment: String = "",
    val date: String = "",
    val authorName: String = "زائر كريم",
    val isBanned: Boolean = false
)
