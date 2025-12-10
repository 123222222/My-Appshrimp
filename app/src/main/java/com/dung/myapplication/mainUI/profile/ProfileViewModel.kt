package com.dung.myapplication.mainUI.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dung.myapplication.R
import com.dung.myapplication.models.User
import com.dung.myapplication.utils.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    // Get current user from session
    val currentUser: User? get() = UserSession.getCurrentUser(context)

    // Check if admin
    val isAdmin: Boolean get() = UserSession.isAdmin(context)

    // Return list with current user, or empty list if not logged in
    val users: List<User>
        get() {
            val user = currentUser
            return if (user != null) {
                listOf(user)
            } else {
                // Fallback to default users if no session (shouldn't happen)
                listOf(
                    User(
                        id = "1",
                        username = "dungho",
                        fullName = "Ho Ngoc Dung",
                        email = "dungho@example.com",
                        phone = "0123456789",
                        avatarResId = R.drawable.avatar,
                        bio = "Embedded systems & Kotlin dev, yêu thích STM32 + Compose Multiplatform!"
                    )
                )
            }
        }
}
