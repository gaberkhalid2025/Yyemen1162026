package com.example.data.entity

data class Professional(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val rating: Float = 4.8f,
    val distance: String = "",
    val skills: String = "", // Comma-separated list
    val avatarEmoji: String = "🧘‍♂️",
    val galleryImages: String = "", // Comma-separated descriptors
    val contactPhone: String = "",
    val contactEmail: String = ""
)
