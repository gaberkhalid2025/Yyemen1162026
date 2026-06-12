package com.example.data.repository

import com.example.BuildConfig
import com.example.data.database.JournalDao
import com.example.data.entity.JournalEntry
import com.example.data.entity.Professional
import com.example.data.entity.BookingSlot
import com.example.data.gemini.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class JournalRepository(
    private val journalDao: JournalDao
) {

    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    // Real-time Firestore snapshot listener for Professionals
    val allProfessionals: Flow<List<Professional>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("professionals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Professional::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    // Real-time Firestore snapshot listener for Booking Slots
    val allSlots: Flow<List<BookingSlot>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("booking_slots")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(BookingSlot::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

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

    // Professional Firestore access methods
    suspend fun insertAllProfessionals(professionals: List<Professional>) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        for (prof in professionals) {
            val docRef = if (prof.id.isEmpty()) {
                db.collection("professionals").document()
            } else {
                db.collection("professionals").document(prof.id)
            }
            docRef.set(prof.copy(id = docRef.id)).await()
        }
    }

    suspend fun insertProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (professional.id.isEmpty()) {
            db.collection("professionals").document()
        } else {
            db.collection("professionals").document(professional.id)
        }
        docRef.set(professional.copy(id = docRef.id)).await()
    }

    suspend fun updateProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("professionals").document(professional.id).set(professional).await()
    }

    // BookingSlot Firestore access methods
    suspend fun insertSlot(slot: BookingSlot) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (slot.id.isEmpty()) {
            db.collection("booking_slots").document()
        } else {
            db.collection("booking_slots").document(slot.id)
        }
        docRef.set(slot.copy(id = docRef.id)).await()
    }

    suspend fun updateSlot(slot: BookingSlot) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("booking_slots").document(slot.id).set(slot).await()
    }

    suspend fun deleteSlotById(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("booking_slots").document(id).delete().await()
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
