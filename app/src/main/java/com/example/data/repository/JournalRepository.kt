package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.database.JournalDao
import com.example.data.entity.*
import com.example.data.gemini.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class JournalRepository(
    private val journalDao: JournalDao,
    private val context: Context
) {
    // Local state flows replacing SharedPrefs with Firestore real-time flows
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: Flow<List<Category>> = _categories.asStateFlow()

    private val _professionals = MutableStateFlow<List<Professional>>(emptyList())
    val allProfessionals: Flow<List<Professional>> = _professionals.asStateFlow()

    private val _pendingProviders = MutableStateFlow<List<PendingProvider>>(emptyList())
    val allPendingProviders: Flow<List<PendingProvider>> = _pendingProviders.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val allReviews: Flow<List<Review>> = _reviews.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val allBanners: Flow<List<Banner>> = _banners.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val allChatMessages: Flow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _appSettings = MutableStateFlow<AppSettings>(AppSettings())
    val appSettingsFlow: Flow<AppSettings> = _appSettings.asStateFlow()

    private val _admins = MutableStateFlow<List<AdminUser>>(emptyList())
    val allAdmins: Flow<List<AdminUser>> = _admins.asStateFlow()

    private val _bookings = MutableStateFlow<List<BookingSlot>>(emptyList())
    val allBookings: Flow<List<BookingSlot>> = _bookings.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val allNotifications: Flow<List<AppNotification>> = _notifications.asStateFlow()

    // Listener registrations for lifecycle control
    private var categoriesListener: ListenerRegistration? = null
    private var professionalsListener: ListenerRegistration? = null
    private var pendingProvidersListener: ListenerRegistration? = null
    private var reviewsListener: ListenerRegistration? = null
    private var bannersListener: ListenerRegistration? = null
    private var chatMessagesListener: ListenerRegistration? = null
    private var adminsListener: ListenerRegistration? = null
    private var appSettingsListener: ListenerRegistration? = null
    private var bookingsListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null

    init {
        // Initialize Firebase Firestore Settings (Optimized Firestore Settings)
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            // Already initialized settings in this run
        }

        // Load and sync Firestore listeners
        recreateAllSnapshotListeners()
    }

    // Journal Entry (Room local diary) logic remains fully functional
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

    /**
     * Clear and remove all active Firestore snapshot listeners to prevent memory leaks or duplicate notifications
     */
    fun clearAllListeners() {
        categoriesListener?.remove()
        categoriesListener = null
        professionalsListener?.remove()
        professionalsListener = null
        pendingProvidersListener?.remove()
        pendingProvidersListener = null
        reviewsListener?.remove()
        reviewsListener = null
        bannersListener?.remove()
        bannersListener = null
        chatMessagesListener?.remove()
        chatMessagesListener = null
        adminsListener?.remove()
        adminsListener = null
        appSettingsListener?.remove()
        appSettingsListener = null
        bookingsListener?.remove()
        bookingsListener = null
        notificationsListener?.remove()
        notificationsListener = null
    }

    /**
     * Set up Firestore snapshot listeners correctly.
     * Implements immediate fetch with .get() (for immediate cache display) before attaching Snapshot Listeners.
     */
    fun recreateAllSnapshotListeners() {
        clearAllListeners()
        val db = FirebaseFirestore.getInstance()

        // 1. Categories
        db.collection("categories").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _categories.value = snapshot.documents.mapNotNull { it.toObject(Category::class.java)?.copy(id = it.id) }
            }
        }
        categoriesListener = db.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _categories.value = snapshot.documents.mapNotNull { it.toObject(Category::class.java)?.copy(id = it.id) }
                }
            }

        // 2. Professionals
        db.collection("professionals").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _professionals.value = snapshot.documents.mapNotNull { it.toObject(Professional::class.java)?.copy(id = it.id) }
            }
        }
        professionalsListener = db.collection("professionals")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _professionals.value = snapshot.documents.mapNotNull { it.toObject(Professional::class.java)?.copy(id = it.id) }
                }
            }

        // 3. Pending Providers
        db.collection("pending_providers").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _pendingProviders.value = snapshot.documents.mapNotNull { it.toObject(PendingProvider::class.java)?.copy(id = it.id) }
            }
        }
        pendingProvidersListener = db.collection("pending_providers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _pendingProviders.value = snapshot.documents.mapNotNull { it.toObject(PendingProvider::class.java)?.copy(id = it.id) }
                }
            }

        // 4. Reviews
        db.collection("reviews").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _reviews.value = snapshot.documents.mapNotNull { it.toObject(Review::class.java)?.copy(id = it.id) }
            }
        }
        reviewsListener = db.collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _reviews.value = snapshot.documents.mapNotNull { it.toObject(Review::class.java)?.copy(id = it.id) }
                }
            }

        // 5. Banners
        db.collection("banners").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _banners.value = snapshot.documents.mapNotNull { it.toObject(Banner::class.java)?.copy(id = it.id) }
            }
        }
        bannersListener = db.collection("banners")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _banners.value = snapshot.documents.mapNotNull { it.toObject(Banner::class.java)?.copy(id = it.id) }
                }
            }

        // 6. Chat Messages
        db.collection("chat_messages").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _chatMessages.value = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java)?.copy(id = it.id) }
            }
        }
        chatMessagesListener = db.collection("chat_messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _chatMessages.value = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java)?.copy(id = it.id) }
                }
            }

        // 7. Admins
        db.collection("admins").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _admins.value = snapshot.documents.mapNotNull { it.toObject(AdminUser::class.java)?.copy(id = it.id) }
            }
        }
        adminsListener = db.collection("admins")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _admins.value = snapshot.documents.mapNotNull { it.toObject(AdminUser::class.java)?.copy(id = it.id) }
                }
            }

        // 8. AppSettings
        db.collection("app_settings").document("globals").get().addOnSuccessListener { dDoc ->
            if (dDoc != null && dDoc.exists()) {
                val settings = dDoc.toObject(AppSettings::class.java)
                if (settings != null) {
                    _appSettings.value = settings
                }
            }
        }
        appSettingsListener = db.collection("app_settings").document("globals")
            .addSnapshotListener { dDoc, error ->
                if (error != null) return@addSnapshotListener
                if (dDoc != null && dDoc.exists()) {
                    val settings = dDoc.toObject(AppSettings::class.java)
                    if (settings != null) {
                        _appSettings.value = settings
                    }
                }
            }

        // 9. Bookings
        db.collection("bookings").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _bookings.value = snapshot.documents.mapNotNull { it.toObject(BookingSlot::class.java)?.copy(id = it.id) }
            }
        }
        bookingsListener = db.collection("bookings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _bookings.value = snapshot.documents.mapNotNull { it.toObject(BookingSlot::class.java)?.copy(id = it.id) }
                }
            }

        // 10. Notifications
        db.collection("notifications").get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                _notifications.value = snapshot.documents.mapNotNull { it.toObject(AppNotification::class.java)?.copy(id = it.id) }
            }
        }
        notificationsListener = db.collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _notifications.value = snapshot.documents.mapNotNull { it.toObject(AppNotification::class.java)?.copy(id = it.id) }
                }
            }
    }

    /**
     * Forces recreation of connectivity and snapshot subscription (for network reconnect)
     */
    suspend fun reconnectFirestore() = withContext(Dispatchers.Main) {
        recreateAllSnapshotListeners()
    }

    // Category Firestore CRUD
    suspend fun addCategory(category: Category) = withContext(Dispatchers.IO) {
        val id = category.id.ifBlank { UUID.randomUUID().toString() }
        val updatedCategory = category.copy(id = id)
        FirebaseFirestore.getInstance().collection("categories").document(id).set(updatedCategory)
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        addCategory(category)
    }

    suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("categories").document(id).delete()
    }

    // Professional Firestore CRUD
    suspend fun insertAllProfessionals(professionals: List<Professional>) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        for (p in professionals) {
            val id = p.id.ifBlank { UUID.randomUUID().toString() }
            val updated = p.copy(id = id)
            db.collection("professionals").document(id).set(updated)
        }
    }

    suspend fun insertProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        val id = professional.id.ifBlank { UUID.randomUUID().toString() }
        val updated = professional.copy(id = id)
        FirebaseFirestore.getInstance().collection("professionals").document(id).set(updated)
    }

    suspend fun updateProfessional(professional: Professional) = withContext(Dispatchers.IO) {
        insertProfessional(professional)
    }

    suspend fun deleteProfessionalById(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("professionals").document(id).delete()
    }

    // PendingProvider Firestore CRUD
    suspend fun submitPendingProvider(provider: PendingProvider) = withContext(Dispatchers.IO) {
        val id = provider.id.ifBlank { UUID.randomUUID().toString() }
        val updated = provider.copy(id = id)
        FirebaseFirestore.getInstance().collection("pending_providers").document(id).set(updated)
    }

    suspend fun updatePendingProvider(provider: PendingProvider) = withContext(Dispatchers.IO) {
        submitPendingProvider(provider)
    }

    suspend fun deletePendingProviderById(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("pending_providers").document(id).delete()
    }

    // Review Firestore CRUD
    suspend fun addReview(review: Review) = withContext(Dispatchers.IO) {
        val id = review.id.ifBlank { UUID.randomUUID().toString() }
        val updated = review.copy(id = id)
        FirebaseFirestore.getInstance().collection("reviews").document(id).set(updated)
    }

    suspend fun updateReview(review: Review) = withContext(Dispatchers.IO) {
        addReview(review)
    }

    suspend fun deleteReviewById(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("reviews").document(id).delete()
    }

    // Banner Firestore CRUD
    suspend fun addBanner(banner: Banner) = withContext(Dispatchers.IO) {
        val id = banner.id.ifBlank { UUID.randomUUID().toString() }
        val updated = banner.copy(id = id)
        FirebaseFirestore.getInstance().collection("banners").document(id).set(updated)
    }

    suspend fun updateBanner(banner: Banner) = withContext(Dispatchers.IO) {
        addBanner(banner)
    }

    suspend fun deleteBannerById(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("banners").document(id).delete()
    }

    // Chat Message Firestore CRUD
    suspend fun sendChatMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        val id = message.id.ifBlank { UUID.randomUUID().toString() }
        val updated = message.copy(id = id)
        FirebaseFirestore.getInstance().collection("chat_messages").document(id).set(updated)
    }

    suspend fun deleteChatMessageById(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("chat_messages").document(id).delete()
    }

    suspend fun clearOldChats() = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        db.collection("chat_messages").get().addOnSuccessListener { snapshot ->
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }
        }
    }

    // AppSettings Firestore CRUD
    suspend fun updateAppSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("app_settings").document("globals").set(settings)
    }

    // AdminUser Firestore CRUD
    suspend fun addAdminUser(admin: AdminUser) = withContext(Dispatchers.IO) {
        val id = admin.id.ifBlank { UUID.randomUUID().toString() }
        val updated = admin.copy(id = id)
        FirebaseFirestore.getInstance().collection("admins").document(id).set(updated)
    }

    suspend fun updateAdminUser(admin: AdminUser) = withContext(Dispatchers.IO) {
        addAdminUser(admin)
    }

    suspend fun deleteAdminUser(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("admins").document(id).delete()
    }

    // Bookings Firestore CRUD
    suspend fun saveBooking(booking: BookingSlot) = withContext(Dispatchers.IO) {
        val id = booking.id.ifBlank { UUID.randomUUID().toString() }
        val updated = booking.copy(id = id)
        FirebaseFirestore.getInstance().collection("bookings").document(id).set(updated)
    }

    suspend fun deleteBooking(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("bookings").document(id).delete()
    }

    // Notifications Firestore CRUD
    suspend fun addNotification(notification: AppNotification) = withContext(Dispatchers.IO) {
        val id = notification.id.ifBlank { UUID.randomUUID().toString() }
        val updated = notification.copy(id = id)
        FirebaseFirestore.getInstance().collection("notifications").document(id).set(updated)
    }

    suspend fun deleteNotification(id: String) = withContext(Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("notifications").document(id).delete()
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
