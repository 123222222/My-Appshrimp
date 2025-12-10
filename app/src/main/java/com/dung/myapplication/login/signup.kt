package com.dung.myapplication.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dung.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        signUpButton.setOnClickListener {
            val phoneNumber = phoneInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!phoneNumber.startsWith("+")) {
                Toast.makeText(this, "Số điện thoại phải bắt đầu với +84", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(phoneNumber, email, password)
        }
    }

    private fun registerUser(phoneNumber: String, email: String, password: String) {
        // Check if phone number already exists
        firestore.collection("users")
            .document(phoneNumber)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Toast.makeText(this, "Số điện thoại này đã được đăng ký", Toast.LENGTH_SHORT).show()
                } else {
                    // Create new user with full information
                    val hashedPassword = hashPassword(password)

                    // Extract username from email (part before @)
                    val username = email.substringBefore("@")

                    val userData = hashMapOf(
                        "phoneNumber" to phoneNumber,
                        "email" to email,
                        "password" to hashedPassword,
                        "username" to username,
                        "fullName" to username, // Default fullName to username, user can update later
                        "role" to "user", // Default role is "user", admin needs to be set manually
                        "avatarResId" to com.dung.myapplication.R.drawable.avatar, // Default avatar
                        "bio" to "Người dùng mới", // Default bio
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(phoneNumber)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Lỗi đăng ký: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi kiểm tra số điện thoại: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

