package com.example.data.entity

data class BookingSlot(
    val id: String = "",
    val date: String = "",
    val time: String = "",
    val isBooked: Boolean = false,
    val bookedBy: String? = null,
    val isEnabled: Boolean = true
)
