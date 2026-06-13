package com.example.data.entity

data class ChatMessage(
    val id: String = "",
    val chatId: String = "", // Unique ID grouping user & practitioner/admin (e.g., "user_with_provider")
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
