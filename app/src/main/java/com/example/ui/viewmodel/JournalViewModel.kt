package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JournalRepository
    val allEntries: StateFlow<List<JournalEntry>>
    
    // Firestore-backed Live-syncing Flows
    val allCategories: StateFlow<List<Category>>
    val allProfessionals: StateFlow<List<Professional>>
    val allPendingProviders: StateFlow<List<PendingProvider>>
    val allReviews: StateFlow<List<Review>>
    val allBanners: StateFlow<List<Banner>>
    val allChatMessages: StateFlow<List<ChatMessage>>
    val allAdmins: StateFlow<List<AdminUser>>
    val appSettings: StateFlow<AppSettings>

    private val sharedPrefs = application.getSharedPreferences("zenmind_prefs", Context.MODE_PRIVATE)

    // Current active system username
    private val _currentUserSession = MutableStateFlow(sharedPrefs.getString("logged_username", "زائر") ?: "زائر")
    val currentUserSession: StateFlow<String> = _currentUserSession.asStateFlow()

    private val _currentUserRole = MutableStateFlow(sharedPrefs.getString("logged_user_role", "guest") ?: "guest") // guest, provider, admin, owner
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _customApiKey = MutableStateFlow(sharedPrefs.getString("api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Assistant / Client States
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _activeInsightResponse = MutableStateFlow<String?>(null)
    val activeInsightResponse: StateFlow<String?> = _activeInsightResponse.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = JournalRepository(database.journalDao())

        // Room
        allEntries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Firestore Real-time Flows
        allCategories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allProfessionals = repository.allProfessionals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allPendingProviders = repository.allPendingProviders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allReviews = repository.allReviews.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBanners = repository.allBanners.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allChatMessages = repository.allChatMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAdmins = repository.allAdmins.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        appSettings = repository.appSettingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

        // Populate Databases with default yemeni data if completely blank
        prepopulateDefaults()
    }

    private fun prepopulateDefaults() {
        viewModelScope.launch {
            try {
                // Prepopulate Categories if empty
                val cats = repository.allCategories.first()
                if (cats.isEmpty()) {
                    val defaultCategories = listOf(
                        Category("cat_1", "سباكة وأعمال صحية", "Plumbing & Sanitation", "🚰", 10),
                        Category("cat_2", "تمديدات كهربائية", "Electrical works", "⚡", 20),
                        Category("cat_3", "نجارة وأثاث", "Carpentry & Wooden Design", "🪚", 30),
                        Category("cat_4", "بناء ومقاولات عامة", "Masonry & Civil jobs", "🏗️", 40),
                        Category("cat_5", "تكييف وتبريد وفلاتر", "AC & Appliance filters", "❄️", 50),
                        Category("cat_6", "غسيل وصيانة غسالات", "Washing machine repairs", "🔌", 60)
                    )
                    for (c in defaultCategories) {
                        repository.addCategory(c)
                    }
                }

                // Prepopulate Professionals if empty
                val profs = repository.allProfessionals.first()
                if (profs.isEmpty()) {
                    val defaultProviders = listOf(
                        Professional(
                            id = "prof_1",
                            name = "المهندس ماهر طاهر",
                            specialty = "كبير مهندسي الكهرباء وتمديدات ذكية للمباني",
                            rating = 4.9f,
                            distance = "صنعاء القديمة - 0.5 كم",
                            skills = "تمديد كيبلات, صيانة لوحات تحكم, تركيب طاقة شمسية كلياً, أنظمة إنذار",
                            avatarEmoji = "⚡",
                            contactPhone = "777644670",
                            contactEmail = "maher@alyemen.services",
                            whatsapp = "777644670",
                            city = "صنعاء",
                            area = "التحرير",
                            isPinned = true,
                            isRecommended = true,
                            isVerified = true,
                            isSubscribed = true,
                            ratingCount = 1,
                            totalRatingPoints = 5f
                        ),
                        Professional(
                            id = "prof_2",
                            name = "المعلم عبد الله اليماني",
                            specialty = "أعمال السباكة الحديثة وصيانة شبكات المياه",
                            rating = 4.7f,
                            distance = "شارع الستين - 1.2 كم",
                            skills = "تسليك مجاري, تركيب سخانات شمسية, صيانة مضخات, عزل حمامات",
                            avatarEmoji = "🚰",
                            contactPhone = "777123456",
                            contactEmail = "abdullah@alyemen.services",
                            whatsapp = "777123456",
                            city = "صنعاء",
                            area = "الستين",
                            isRecommended = true,
                            isVerified = true,
                            ratingCount = 1,
                            totalRatingPoints = 4f
                        ),
                        Professional(
                            id = "prof_3",
                            name = "الحرفي عمار ياسر",
                            specialty = "صناعة وفك وتركيب غرف النوم والأثاث اليمني",
                            rating = 4.8f,
                            distance = "المنصورة - 2.0 كم",
                            skills = "تفصيل أثاث دمياطي, ترميم أبواب وشبابيك عتيقة, تركيب ديكورات خشبية",
                            avatarEmoji = "🪚",
                            contactPhone = "733665544",
                            contactEmail = "ammar@alyemen.services",
                            whatsapp = "733665544",
                            city = "عدن",
                            area = "المنصورة",
                            isVerified = true,
                            ratingCount = 2,
                            totalRatingPoints = 10f
                        )
                    )
                    repository.insertAllProfessionals(defaultProviders)
                }

                // Prepopulate Admins list with credentials
                val admins = repository.allAdmins.first()
                if (admins.isEmpty()) {
                    val defaultAdmin = AdminUser(
                        id = "admin_main",
                        username = "WAM2026",
                        password = "maher736462",
                        editCategories = true,
                        deleteProviders = true,
                        manageSettings = true
                    )
                    repository.addAdminUser(defaultAdmin)
                }

                // Prepopulate general settings if empty
                val currentSet = repository.appSettingsFlow.firstOrNull()
                if (currentSet == null || currentSet.id != "globals") {
                    repository.updateAppSettings(AppSettings())
                }

                // Prepopulate Banners if empty
                val banners = repository.allBanners.first()
                if (banners.isEmpty()) {
                    repository.addBanner(
                        Banner(
                            id = "banner_1",
                            title = "معرض المهن المجانية الأول",
                            content = "احصل على خصم 20% عند طلب أول فني سباكة من التطبيق برعاية مبادرة MAW!",
                            link = "https://t.me/YemenServices",
                            mediaType = "image",
                            duration = 6,
                            isActive = true
                        )
                    )
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    // Role, session, login actions
    fun attemptLogin(username: String, pass: String): Boolean {
        // Main default master admin login WAM2026/maher736462
        if (username == "WAM2026" && pass == "maher736462") {
            setSession("WAM2026", "admin")
            return true
        }

        // Check synced Admins collection
        val foundAdmin = allAdmins.value.find { it.username == username && it.password == pass }
        if (foundAdmin != null) {
            setSession(foundAdmin.username, "admin")
            return true
        }

        // Check if a normal provider is logging in
        val foundProvider = allProfessionals.value.find { it.name == username && it.contactPhone == pass }
        if (foundProvider != null) {
            setSession(foundProvider.name, "provider")
            return true
        }

        return false
    }

    fun setSession(username: String, role: String) {
        _currentUserSession.value = username
        _currentUserRole.value = role
        sharedPrefs.edit()
            .putString("logged_username", username)
            .putString("logged_user_role", role)
            .apply()
    }

    fun logout() {
        setSession("زائر", "guest")
    }

    fun saveApiKey(key: String) {
        _customApiKey.value = key
        sharedPrefs.edit().putString("api_key", key).apply()
    }

    // Settings actions (sync to Firestore for all devices)
    fun updateSystemSettings(settings: AppSettings) {
        viewModelScope.launch {
            repository.updateAppSettings(settings)
        }
    }

    // Categories actions
    fun persistCategory(category: Category) {
        viewModelScope.launch {
            repository.addCategory(category)
        }
    }

    fun removeCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    // Professional/Provider actions
    fun persistProfessional(professional: Professional) {
        viewModelScope.launch {
            repository.insertProfessional(professional)
        }
    }

    fun removeProfessional(id: String) {
        viewModelScope.launch {
            repository.deleteProfessionalById(id)
        }
    }

    // Reviews actions
    fun leaveRating(providerId: String, stars: Float, comment: String, reviewerName: String) {
        viewModelScope.launch {
            val review = Review(
                providerId = providerId,
                rating = stars,
                comment = comment,
                authorName = reviewerName.ifBlank { "مواطن يمني" },
                date = "اليوم"
            )
            repository.addReview(review)

            // Recalculate average stars on provider
            val targetProf = allProfessionals.value.find { it.id == providerId }
            if (targetProf != null) {
                val newCount = targetProf.ratingCount + 1
                val newPoints = targetProf.totalRatingPoints + stars
                val avg = String.format("%.1f", newPoints / newCount).replace(",", ".").toFloatOrNull() ?: stars
                repository.insertProfessional(
                    targetProf.copy(
                        rating = avg,
                        ratingCount = newCount,
                        totalRatingPoints = newPoints
                    )
                )
            }
        }
    }

    fun removeReview(id: String) {
        viewModelScope.launch {
            repository.deleteReviewById(id)
        }
    }

    fun updateReview(review: Review) {
        viewModelScope.launch {
            repository.updateReview(review)
        }
    }

    // Banners actions
    fun persistBanner(banner: Banner) {
        viewModelScope.launch {
            repository.addBanner(banner)
        }
    }

    fun removeBanner(id: String) {
        viewModelScope.launch {
            repository.deleteBannerById(id)
        }
    }

    // Pending Applications
    fun applyAsProvider(
        name: String,
        phone: String,
        category: String,
        city: String,
        area: String,
        address: String,
        gps: String,
        selfieBase64: String,
        idCardBase64: String
    ) {
        viewModelScope.launch {
            val request = PendingProvider(
                name = name,
                phone = phone,
                categoryName = category,
                city = city,
                area = area,
                address = address,
                gpsCoords = gps,
                selfieImage = selfieBase64,
                docImage = idCardBase64,
                status = "pending"
            )
            repository.submitPendingProvider(request)
        }
    }

    fun approvePendingProvider(app: PendingProvider) {
        viewModelScope.launch {
            // Write provider object to service_providers
            val newProf = Professional(
                name = app.name,
                specialty = "فني ${app.categoryName} معتمد",
                contactPhone = app.phone,
                whatsapp = app.phone,
                city = app.city,
                area = app.area,
                skills = "صيانة وتجهيزات ${app.categoryName}",
                avatarEmoji = when {
                    app.categoryName.contains("سبا") -> "🚰"
                    app.categoryName.contains("كهرب") -> "⚡"
                    app.categoryName.contains("نجار") -> "🪚"
                    app.categoryName.contains("بناء") -> "🏗️"
                    app.categoryName.contains("تكيي") -> "❄️"
                    else -> "⚒️"
                },
                distance = "قريب من ${app.area}",
                galleryImages = app.selfieImage.takeIf { it.isNotEmpty() } ?: "",
                isVerified = true
            )
            repository.insertProfessional(newProf)
            // Delete application
            repository.deletePendingProviderById(app.id)
        }
    }

    fun rejectPendingProvider(app: PendingProvider, reason: String) {
        viewModelScope.launch {
            repository.updatePendingProvider(app.copy(status = "rejected", rejectionReason = reason))
        }
    }

    fun removePendingRequest(id: String) {
        viewModelScope.launch {
            repository.deletePendingProviderById(id)
        }
    }

    // Chat actions
    fun postChatMessage(chatId: String, text: String, senderName: String, receiverId: String = "") {
        viewModelScope.launch {
            val msg = ChatMessage(
                chatId = chatId,
                senderId = _currentUserSession.value,
                senderName = senderName,
                receiverId = receiverId,
                message = text
            )
            repository.sendChatMessage(msg)
        }
    }

    fun clearOldChats() {
        viewModelScope.launch {
            repository.clearOldChats()
        }
    }

    // Supervisors/Admins
    fun persistAdminUser(admin: AdminUser) {
        viewModelScope.launch {
            repository.addAdminUser(admin)
        }
    }

    fun removeAdminUser(id: String) {
        viewModelScope.launch {
            repository.deleteAdminUser(id)
        }
    }

    // Assistant Chat Assistant (Works offline & online)
    fun askAssistant(question: String, onAnswer: (String) -> Unit) {
        val normalized = question.trim().lowercase()
        
        // 1. Check offline responses first (for robust offline functionality as requested!)
        val offlineAnswer = when {
            normalized.contains("أقسام") || normalized.contains("اقسام") || normalized.contains("departments") -> {
                val names = allCategories.value.map { it.nameAr }.joinToString("\n• ")
                "الأقسام المتوفرة في دليل اليمن الشامل للخدمات:\n• $names"
            }
            normalized.contains("اتصل") || normalized.contains("تواصل") || normalized.contains("phone") -> {
                "يمكنك الاتصال الفوري بمقدمي الخدمة بالدخول لصفحة مقدم الخدمة والضغط على أيقونة الاتصال بالهاتف أو الواتساب مباشرة وصاحب الطلب يتفق مع مقدم الخدمة."
            }
            normalized.contains("رقم الدعم") || normalized.contains("رقم دعم") || normalized.contains("support number") -> {
                "رقم دعم مبادرة دليل الخدمات الفني الشامل باليمن هو: 777644670"
            }
            normalized.contains("مطور") || normalized.contains("برمجة") || normalized.contains("maw") -> {
                "تمت البرمجة والتحضير الفني بواسطة MAW 777644670 لخدمة المجتمع اليمني."
            }
            normalized.contains("حجز") || normalized.contains("موعد") -> {
                "لحجز فني، افتح الملف الشخصي لمرفق العمل، ثم اضغط على موعد متاح أو الدردشة المباشرة للاتفاق الفوري."
            }
            else -> null
        }

        if (offlineAnswer != null) {
            onAnswer(offlineAnswer)
            return
        }

        // If online, use Gemini API
        viewModelScope.launch {
            _isGenerating.value = true
            val response = repository.generateZenInsight(question, "NormalInfo", _customApiKey.value)
            _isGenerating.value = false
            onAnswer(response)
        }
    }
}
