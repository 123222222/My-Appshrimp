package com.dung.myapplication.mainUI.logout

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.dung.myapplication.models.User
import com.dung.myapplication.utils.UserSession

@HiltViewModel
class LogoutViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun logout() {
        // Đăng xuất Firebase Auth
        auth.signOut()

        // Xóa toàn bộ session qua UserSession
        UserSession.clearSession(context)
    }

    // Get current user from session
    val currentUser: User? get() = UserSession.getCurrentUser(context)

    // Check if admin
    val isAdmin: Boolean get() = UserSession.isAdmin(context)

    // Make users reactive - show current logged in user
    val users: List<User>
        get() {
            val user = currentUser
            return if (user != null) {
                listOf(user)
            } else {
                emptyList()
            }
        }
}
