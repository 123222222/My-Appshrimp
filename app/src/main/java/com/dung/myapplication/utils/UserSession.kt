package com.dung.myapplication.utils

import android.content.Context
import androidx.core.content.edit
import com.dung.myapplication.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

/**
 * Quản lý session và thông tin user hiện tại
 */
object UserSession {
    private const val PREFS_NAME = "user_session"
    private const val KEY_USER_DATA = "user_data"
    private const val KEY_PHONE_NUMBER = "phone_number"
    private const val KEY_IS_ADMIN = "is_admin"

    private val gson = Gson()

    /**
     * Lưu thông tin user sau khi đăng nhập
     */
    fun saveUser(context: Context, user: User, isAdmin: Boolean = false) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_USER_DATA, gson.toJson(user))
            putString(KEY_PHONE_NUMBER, user.phone)
            putBoolean(KEY_IS_ADMIN, isAdmin)
        }
    }

    /**
     * Lấy thông tin user hiện tại
     */
    fun getCurrentUser(context: Context): User? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userJson = prefs.getString(KEY_USER_DATA, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Kiểm tra user có phải admin không
     */
    fun isAdmin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_ADMIN, false)
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    fun isLoggedIn(context: Context): Boolean {
        // Check Firebase Auth
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) return true

        // Check phone login session
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PHONE_NUMBER, null) != null
    }

    /**
     * Xóa session khi đăng xuất
     */
    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }

        // Xóa cả auth prefs (cho Google login)
        val authPrefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        authPrefs.edit { clear() }
    }

    /**
     * Lấy loại đăng nhập hiện tại
     */
    fun getLoginType(context: Context): LoginType {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            return if (firebaseUser.email != null) LoginType.GOOGLE else LoginType.FIREBASE
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.getString(KEY_PHONE_NUMBER, null) != null) {
            LoginType.PHONE
        } else {
            LoginType.NONE
        }
    }

    enum class LoginType {
        GOOGLE, PHONE, FIREBASE, NONE
    }
}

