package com.example.data.entity

data class PendingProvider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val categoryName: String = "",
    val address: String = "",
    val area: String = "",
    val city: String = "صنعاء",
    val gpsCoords: String = "",
    val selfieImage: String = "", // Base64 representation of the selfie/avatar
    val docImage: String = "", // Base64 representation of national ID
    val status: String = "pending", // pending, approved, rejected
    val rejectionReason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
