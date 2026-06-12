package com.example.data.entity

data class AdminUser(
    val id: String = "",
    val username: String = "",
    val password: String = "",
    val editCategories: Boolean = true,
    val deleteProviders: Boolean = true,
    val manageSettings: Boolean = true
)
