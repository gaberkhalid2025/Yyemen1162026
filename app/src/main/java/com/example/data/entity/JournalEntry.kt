package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val mood: String, // "Peaceful", "Joyful", "Neutral", "Restless", "Overwhelmed"
    val timestamp: Long = System.currentTimeMillis(),
    val aiInsight: String? = null
)
