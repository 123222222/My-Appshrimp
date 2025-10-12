package com.dung.myapplication.mainUI.logout

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun logout() {
        auth.signOut()
    }
}
