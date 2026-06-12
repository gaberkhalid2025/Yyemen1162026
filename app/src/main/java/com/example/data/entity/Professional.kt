package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "professionals")
data class Professional(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val specialty: String,
    val rating: Float,
    val distance: String,
    val skills: String, // Comma-separated list
    val avatarEmoji: String,
    val galleryImages: String, // Comma-separated descriptors
    val contactPhone: String,
    val contactEmail: String
)
