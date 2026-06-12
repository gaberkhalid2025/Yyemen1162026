package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.JournalEntry
import com.example.data.entity.Professional
import com.example.data.entity.BookingSlot
import com.example.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JournalRepository
    val allEntries: StateFlow<List<JournalEntry>>
    val allProfessionals: StateFlow<List<Professional>>
    val allSlots: StateFlow<List<BookingSlot>>

    // Custom API key storage (using SharedPreferences for local persistence)
    private val sharedPrefs = application.getSharedPreferences("zenmind_prefs", Context.MODE_PRIVATE)
    
    private val _customApiKey = MutableStateFlow(sharedPrefs.getString("api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // 1. Map enabled toggle for Admin dashboard
    private val _isMapEnabled = MutableStateFlow(sharedPrefs.getBoolean("map_enabled", true))
    val isMapEnabled: StateFlow<Boolean> = _isMapEnabled.asStateFlow()

    // 2. Profile section visibility toggles controlled by Admin Dashboard
    private val _showSkills = MutableStateFlow(sharedPrefs.getBoolean("show_skills", true))
    val showSkills: StateFlow<Boolean> = _showSkills.asStateFlow()

    private val _showGallery = MutableStateFlow(sharedPrefs.getBoolean("show_gallery", true))
    val showGallery: StateFlow<Boolean> = _showGallery.asStateFlow()

    private val _showContact = MutableStateFlow(sharedPrefs.getBoolean("show_contact", true))
    val showContact: StateFlow<Boolean> = _showContact.asStateFlow()

    // 3. Admin booking notification routing rules
    private val _notificationRoutingRule = MutableStateFlow(
        sharedPrefs.getString("notification_rule", "Slack/Email Server - gaber77710@gmail.com") 
        ?: "Slack/Email Server - gaber77710@gmail.com"
    )
    val notificationRoutingRule: StateFlow<String> = _notificationRoutingRule.asStateFlow()

    // UI generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _activeInsightResponse = MutableStateFlow<String?>(null)
    val activeInsightResponse: StateFlow<String?> = _activeInsightResponse.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = JournalRepository(
            database.journalDao(),
            database.professionalDao(),
            database.bookingSlotDao()
        )
        
        allEntries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allProfessionals = repository.allProfessionals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSlots = repository.allSlots.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate professionals with location coordinates if empty
        viewModelScope.launch {
            try {
                val list = repository.allProfessionals.first()
                if (list.isEmpty()) {
                    val initialList = listOf(
                        Professional(
                            name = "Zen Master Ananda",
                            specialty = "Vipassana Meditation & Tibetan Sound Healing",
                            rating = 4.9f,
                            distance = "0.8 miles away",
                            skills = "Vipassana, Crystal Sound Bowls, Breathwork, Heart Coherence, Kundalini Flow",
                            avatarEmoji = "🧘‍♂️",
                            galleryImages = "Himalayan Healing Retreat, Forest Walk Mindfulness, Zen Tea Gathering",
                            contactPhone = "+1 (555) 303-9111",
                            contactEmail = "ananda@zenmind.ai"
                        ),
                        Professional(
                            name = "Somi - Breathwork Guru",
                            specialty = "Pranayama & Active Energy Grounding",
                            rating = 4.8f,
                            distance = "1.5 miles away",
                            skills = "Pranayama Tuning, Conscious Intention, Vedic Sound Bath, Restorative Yoga Nidra",
                            avatarEmoji = "🍃",
                            galleryImages = "Sunset Breathing Circle, Mandala Sound Bath, Sunrise Yoga Nidra",
                            contactPhone = "+1 (555) 781-2290",
                            contactEmail = "somi@zenmind.ai"
                        ),
                        Professional(
                            name = "Dr. David Malik",
                            specialty = "Somatic Clinical Mindfulness & Stress Reduction",
                            rating = 4.7f,
                            distance = "2.3 miles away",
                            skills = "Somatic Experiencing, Mindfulness CBT Integration, Heartwave Biofeedback, MBSR Practitioner",
                            avatarEmoji = "✨",
                            galleryImages = "Clinical Stress Therapy, Group Mindfulness Circle, Ocean Bio-Flow Seminar",
                            contactPhone = "+1 (555) 433-8800",
                            contactEmail = "david.malik@zenmind.ai"
                        )
                    )
                    repository.insertAllProfessionals(initialList)
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }

        // Prepopulate slots if empty
        viewModelScope.launch {
            try {
                val list = repository.allSlots.first()
                if (list.isEmpty()) {
                    val initialSlots = listOf(
                        BookingSlot(date = "June 14", time = "09:00 AM", isBooked = false, isEnabled = true),
                        BookingSlot(date = "June 14", time = "11:30 AM", isBooked = false, isEnabled = true),
                        BookingSlot(date = "June 14", time = "02:00 PM", isBooked = false, isEnabled = true),
                        BookingSlot(date = "June 15", time = "10:00 AM", isBooked = false, isEnabled = true),
                        BookingSlot(date = "June 15", time = "03:30 PM", isBooked = false, isEnabled = true)
                    )
                    for (slot in initialSlots) {
                        repository.insertSlot(slot)
                    }
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    // Toggle settings methods
    fun setMapEnabled(enabled: Boolean) {
        _isMapEnabled.value = enabled
        sharedPrefs.edit().putBoolean("map_enabled", enabled).apply()
    }

    fun setShowSkills(show: Boolean) {
        _showSkills.value = show
        sharedPrefs.edit().putBoolean("show_skills", show).apply()
    }

    fun setShowGallery(show: Boolean) {
        _showGallery.value = show
        sharedPrefs.edit().putBoolean("show_gallery", show).apply()
    }

    fun setShowContact(show: Boolean) {
        _showContact.value = show
        sharedPrefs.edit().putBoolean("show_contact", show).apply()
    }

    fun setNotificationRoutingRule(rule: String) {
        _notificationRoutingRule.value = rule
        sharedPrefs.edit().putString("notification_rule", rule).apply()
    }

    fun saveApiKey(key: String) {
        _customApiKey.value = key
        sharedPrefs.edit().putString("api_key", key).apply()
    }

    // Admin Slot Management
    fun addBookingSlot(date: String, time: String) {
        viewModelScope.launch {
            repository.insertSlot(BookingSlot(date = date, time = time, isBooked = false, isEnabled = true))
        }
    }

    fun toggleSlotEnabled(slot: BookingSlot) {
        viewModelScope.launch {
            repository.updateSlot(slot.copy(isEnabled = !slot.isEnabled))
        }
    }

    fun deleteBookingSlot(id: Int) {
        viewModelScope.launch {
            repository.deleteSlotById(id)
        }
    }

    // Booking actions
    fun bookSession(slot: BookingSlot, userName: String, onBookingStatus: (String) -> Unit) {
        viewModelScope.launch {
            val updated = slot.copy(isBooked = true, bookedBy = userName)
            repository.updateSlot(updated)
            // Dispatch dynamic route notification message
            val announcement = "Mindful Session confirmed with Somi on ${slot.date} at ${slot.time} for $userName. Details dispatched via active route: ${notificationRoutingRule.value}"
            onBookingStatus(announcement)
        }
    }

    fun cancelSession(slot: BookingSlot) {
        viewModelScope.launch {
            repository.updateSlot(slot.copy(isBooked = false, bookedBy = null))
        }
    }

    // Journal functions
    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun saveEntry(text: String, mood: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isGenerating.value = true
            _activeInsightResponse.value = "Consulting ZenCoach..."

            val insight = repository.generateZenInsight(text, mood, _customApiKey.value.trim().takeIf { it.isNotEmpty() })
            
            val entry = JournalEntry(
                text = text,
                mood = mood,
                aiInsight = insight
            )
            repository.insert(entry)

            _activeInsightResponse.value = insight
            _isGenerating.value = false
            onComplete()
        }
    }

    fun reGenerateInsight(entry: JournalEntry) {
        viewModelScope.launch {
            _isGenerating.value = true
            val insight = repository.generateZenInsight(entry.text, entry.mood, _customApiKey.value.trim().takeIf { it.isNotEmpty() })
            
            val updated = entry.copy(aiInsight = insight)
            repository.update(updated)
            _isGenerating.value = false
        }
    }

    fun clearActiveInsight() {
        _activeInsightResponse.value = null
    }
}
