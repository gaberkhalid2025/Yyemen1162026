package com.example.data.repository

import com.example.BuildConfig
import com.example.data.database.JournalDao
import com.example.data.entity.*
import com.example.data.gemini.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class JournalRepository(
    private val journalDao: JournalDao
) {
    init {
        // Enable offline persistence in Firestore so the app works flawlessly offline
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            // Might be already configured or during test
        }
    }

    // Journal Entry (Room) logic
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

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

    // Firestore Categories Flow
    val allCategories: Flow<List<Category>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("categories")
            .orderBy("displayOrder")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Category::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun addCategory(category: Category) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (category.id.isEmpty()) {
            db.collection("categories").document()
        } else {
            db.collection("categories").document(category.id)
        }
        docRef.set(category.copy(id = docRef.id)).await()
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("categories").document(category.id).set(category).await()
    }

    suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("categories").document(id).delete().await()
    }

    // Firestore Professionals (Service Providers) Flow
    val allProfessionals: Flow<List<Professional>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("service_providers")
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

    suspend fun insertAllProfessionals(professionals: List<Professional>) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        for (prof in professionals) {
            val docRef = if (prof.id.isEmpty()) {
                db.collection("service_providers").document()
            } else {
                db.collection("service_providers").document(prof.id)
            }
            docRef.set(prof.copy(id = docRef.id)).await()
        }
    }

    suspend fun insertProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (professional.id.isEmpty()) {
            db.collection("service_providers").document()
        } else {
            db.collection("service_providers").document(professional.id)
        }
        docRef.set(professional.copy(id = docRef.id)).await()
    }

    suspend fun updateProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("service_providers").document(professional.id).set(professional).await()
    }

    suspend fun deleteProfessionalById(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("service_providers").document(id).delete().await()
    }

    // Firestore Pending Providers Application Flow
    val allPendingProviders: Flow<List<PendingProvider>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("pending_providers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PendingProvider::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun submitPendingProvider(provider: PendingProvider) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (provider.id.isEmpty()) {
            db.collection("pending_providers").document()
        } else {
            db.collection("pending_providers").document(provider.id)
        }
        docRef.set(provider.copy(id = docRef.id)).await()
    }

    suspend fun updatePendingProvider(provider: PendingProvider) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pending_providers").document(provider.id).set(provider).await()
    }

    suspend fun deletePendingProviderById(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pending_providers").document(id).delete().await()
    }

    // Firestore Reviews Flow
    val allReviews: Flow<List<Review>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Review::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun addReview(review: Review) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (review.id.isEmpty()) {
            db.collection("reviews").document()
        } else {
            db.collection("reviews").document(review.id)
        }
        docRef.set(review.copy(id = docRef.id)).await()
    }

    suspend fun updateReview(review: Review) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("reviews").document(review.id).set(review).await()
    }

    suspend fun deleteReviewById(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("reviews").document(id).delete().await()
    }

    // Firestore Banners Flow
    val allBanners: Flow<List<Banner>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("banners")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Banner::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun addBanner(banner: Banner) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (banner.id.isEmpty()) {
            db.collection("banners").document()
        } else {
            db.collection("banners").document(banner.id)
        }
        docRef.set(banner.copy(id = docRef.id)).await()
    }

    suspend fun updateBanner(banner: Banner) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("banners").document(banner.id).set(banner).await()
    }

    suspend fun deleteBannerById(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("banners").document(id).delete().await()
    }

    // Firestore Chats Flow
    val allChatMessages: Flow<List<ChatMessage>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("chats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun sendChatMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("chats").document()
        docRef.set(message.copy(id = docRef.id)).await()
    }

    suspend fun deleteChatMessageById(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("chats").document(id).delete().await()
    }

    suspend fun clearOldChats() = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val querySnapshot = db.collection("chats").get().await()
        for (doc in querySnapshot.documents) {
            doc.reference.delete().await()
        }
    }

    // Firestore App Settings Flow
    val appSettingsFlow: Flow<AppSettings> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("app_settings").document("globals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Suppress or handle appropriately
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val settings = snapshot.toObject(AppSettings::class.java)
                    if (settings != null) {
                        trySend(settings)
                    }
                } else {
                    // Init empty settings
                    trySend(AppSettings())
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun updateAppSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("app_settings").document("globals").set(settings).await()
    }

    // Firestore Admins (Supervisors) Flow
    val allAdmins: Flow<List<AdminUser>> = callbackFlow {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("admins")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AdminUser::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun addAdminUser(admin: AdminUser) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val docRef = if (admin.id.isEmpty()) {
            db.collection("admins").document()
        } else {
            db.collection("admins").document(admin.id)
        }
        docRef.set(admin.copy(id = docRef.id)).await()
    }

    suspend fun updateAdminUser(admin: AdminUser) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("admins").document(admin.id).set(admin).await()
    }

    suspend fun deleteAdminUser(id: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("admins").document(id).delete().await()
    }

    /**
     * Call Gemini chat/advice system.
     */
    suspend fun generateZenInsight(text: String, mood: String, customApiKey: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey
            else -> try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "المساعد اليمني: تواصل مع خدمات المساعدة على الواتساب 777644670 للحصول على الدعم والمشورة."
        }

        val prompt = "User asks the specialized Yemen guide directory: '$text'. The context is: mood '$mood'. Give an informative helper guide response in lovely Arabic style (max 3 concise sentences)."

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are the smart guide. Call yourself: المساعد الذكي اليمني. You know about plumbing, electricity, construction, and yemeni workers. Give fast, dynamic answers under 50 words."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "مرحبا بك! فضلا تصفح قائمة الأقسام المتاحة أو اتصل بخدمة الدعم الفني: 777644670"
        } catch (e: Exception) {
            "مرحباً! نوصي بالتواصل مع أقرب فني متوفر في منطقتك عبر الأقسام. رقم الدعم الموحد: 777644670"
        }
    }
}
