package com.dung.myapplication.mainUI.profile

import androidx.lifecycle.ViewModel
import com.dung.myapplication.R
import com.dung.myapplication.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    val users = listOf(
        User(
            id = "1",
            username = "dungho",
            fullName = "Ho Ngoc Dung",
            email = "dungho@example.com",
            phone = "0123456789",
            avatarResId = R.drawable.avatar, // ✅ dùng avatarResId
            bio = "Embedded systems & Kotlin dev, yêu thích STM32 + Compose Multiplatform!"
        ),
        User(
            id = "2",
            username = "johndoe",
            fullName = "John Doe",
            email = "john@example.com",
            phone = "0987654321",
            avatarResId = R.drawable.avatar2, // ✅ ảnh thứ hai
            bio = "Mobile developer, thích Compose + Clean Architecture."
        )
    )
}
