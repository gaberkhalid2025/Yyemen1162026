package com.example.data.entity

data class Banner(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val link: String = "",
    val mediaType: String = "image", // image, video, text
    val mediaUrl: String = "", // Base64 or asset descriptor
    val duration: Int = 5,
    val isActive: Boolean = true
)
