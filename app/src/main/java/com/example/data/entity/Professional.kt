package com.example.data.entity

data class Professional(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val rating: Float = 4.8f,
    val distance: String = "قريب منك",
    val skills: String = "", // Comma-separated list
    val avatarEmoji: String = "🧘‍♂️",
    val galleryImages: String = "", // Comma-separated descriptors
    val contactPhone: String = "",
    val contactEmail: String = "",
    val whatsapp: String = "",
    val city: String = "Sana'a", // Default city
    val area: String = "Al-Tahreer", // Default neighborhood
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false,
    val isSubscribed: Boolean = false,
    val subscriptionStatus: String = "none", // none, pending_payment, subscribed
    val paymentReceipt: String = "", // transaction reference/details
    val totalRatingPoints: Float = 0f,
    val ratingCount: Int = 0,
    val latitude: Double = 15.3694, // Default HQ map coordinates
    val longitude: Double = 44.1910
)
