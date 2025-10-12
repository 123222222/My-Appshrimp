package com.dung.myapplication.models

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val avatarResId: Int? = null, // ✅ Dùng resource ID thay vì URL
)