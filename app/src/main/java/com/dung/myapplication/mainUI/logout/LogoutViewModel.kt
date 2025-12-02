package com.dung.myapplication.mainUI.logout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.dung.myapplication.models.User
import com.dung.myapplication.R

@HiltViewModel
class LogoutViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun logout() {
        auth.signOut()
    }

    // Make users reactive and allow setting them from NavGraph (copy from ProfileViewModel)
    private var _users by mutableStateOf(
        listOf(
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
    )

    // Expose immutable view
    val users: List<User>
        get() = _users

    // Allow NavGraph (or other callers) to set users from ProfileViewModel
    fun setUsers(fromProfile: List<User>) {
        if (fromProfile.isNotEmpty()) {
            _users = fromProfile
        }
    }
}
