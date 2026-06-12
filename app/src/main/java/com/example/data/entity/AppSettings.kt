package com.example.data.entity

data class AppSettings(
    val id: String = "globals",
    val appName: String = "الدليل الشامل لكل الخدمات",
    val colorTheme: String = "🌌 كوزميك سيلفر", // "🌌 كوزميك سيلفر", "✨ الذهبي الفاخر", "🟢 الزمردي الراقي"
    val textColorStyle: String = "الأبيض الناصع", // "الأبيض الناصع", "الذهبي الفاتح", "الفضي المتوهج"
    val footerText: String = "MAW 777644670",
    val footerTransparency: Float = 0.6f, // Opacity float (0f - 1f)
    val footerFontSize: Int = 12,
    val welcomeMessage: String = "مرحباً بكم في الدليل الشامل للأعمال والمهن باليمن!",
    val supportPhone: String = "777644670",
    val supportEmail: String = "support@alyemen.services",
    val supportWhatsapp: String = "777644670",
    val adminPassword: String = "maher736462", // Default general supervisor password
    val isMaintenanceMode: Boolean = false,
    val assistantIconSize: Float = 48f, // custom size configuration for assistant button
    val showAssistant: Boolean = true,
    val isMapEnabled: Boolean = true,
    val isChatEnabled: Boolean = true,
    val fcmNotifyRegistrations: Boolean = true,
    val fcmNotifyBugs: Boolean = true,
    val fcmNotifyRatings: Boolean = true,
    val activeCities: String = "صنعاء, عدن, تعز, الحديدة, إب, حضرموت, ذمار", // Comma-separated manageable cities
    val dynamicCustomColorsJson: String = "" // Custom primary, secondary hues if set by owner
)
