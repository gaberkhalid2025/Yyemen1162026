package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booking_slots")
data class BookingSlot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val time: String,
    val isBooked: Boolean = false,
    val bookedBy: String? = null,
    val isEnabled: Boolean = true
)
