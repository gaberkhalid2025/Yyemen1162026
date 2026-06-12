package com.example.data.repository

import com.example.BuildConfig
import com.example.data.database.JournalDao
import com.example.data.database.ProfessionalDao
import com.example.data.database.BookingSlotDao
import com.example.data.entity.JournalEntry
import com.example.data.entity.Professional
import com.example.data.entity.BookingSlot
import com.example.data.gemini.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class JournalRepository(
    private val journalDao: JournalDao,
    private val professionalDao: ProfessionalDao,
    private val bookingSlotDao: BookingSlotDao
) {

    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    val allProfessionals: Flow<List<Professional>> = professionalDao.getAllProfessionals()
    val allSlots: Flow<List<BookingSlot>> = bookingSlotDao.getAllSlots()

    suspend fun insert(entry: JournalEntry) = withContext(Dispatchers.IO) {
        journalDao.insertEntry(entry)
    }

    suspend fun update(entry: JournalEntry) = withContext(Dispatchers.IO) {
        journalDao.updateEntry(entry)
    }

    suspend fun delete(entry: JournalEntry) = withContext(Dispatchers.IO) {
        journalDao.deleteEntry(entry)
    }

    suspend fun deleteById(id: Int) = withContext(Dispatchers.IO) {
        journalDao.deleteEntryById(id)
    }

    // Professional access methods
    suspend fun insertAllProfessionals(professionals: List<Professional>) = withContext(Dispatchers.IO) {
        professionalDao.insertAll(professionals)
    }

    suspend fun insertProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        professionalDao.insert(professional)
    }

    suspend fun updateProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        professionalDao.update(professional)
    }

    // BookingSlot access methods
    suspend fun insertSlot(slot: BookingSlot) = withContext(Dispatchers.IO) {
        bookingSlotDao.insert(slot)
    }

    suspend fun updateSlot(slot: BookingSlot) = withContext(Dispatchers.IO) {
        bookingSlotDao.update(slot)
    }

    suspend fun deleteSlotById(id: Int) = withContext(Dispatchers.IO) {
        bookingSlotDao.deleteById(id)
    }

    /**
     * Generates a tailored mindfulness insight based on user journal text and selected mood.
     */
    suspend fun generateZenInsight(text: String, mood: String, customApiKey: String? = null): String = withContext(Dispatchers.IO) {
        // Fallback checks for API Key
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey
            else -> try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is not configured. Please configure your Gemini API Key in the settings panel to enable AI coaching."
        }

        val prompt = "The user is experiencing a mood of '$mood' today. Here is what they wrote in their mindfulness journal:\n" +
                "\"$text\"\n\n" +
                "Respond as ZenCoach inside a beautiful frame: Offer a compassionate, deeply encouraging, and grounding zen mindfulness reflection (maximum 3 concise sentences), followed by a simple, practical breathing/grounding tip."

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are ZenCoach, a compassionate, poetic, and wise AI mindfulness guide. You offer comforting thoughts and gentle breathing exercises to anchor the soul. Keep your responses serene, beautiful, and under 80 words."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            resultText ?: "Rest your mind. Allow your breath to settle into a natural rhythm."
        } catch (e: Exception) {
            "ZenCoach is in deep meditation. (${e.localizedMessage ?: "Please check your network connection."})"
        }
    }
}
