package com.dung.myapplication.login

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dung.myapplication.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private val client = OkHttpClient()
    private val API_URL = "http://192.168.137.125:5001"  // IP của Raspberry Pi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        firestore = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val sendButton = findViewById<Button>(R.id.sendResetLinkButton)

        // Xử lý deep link khi user nhấn vào link trong email
        handleDeepLink(intent)

        sendButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tìm số điện thoại từ email trong Firestore
            findPhoneByEmail(email)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent == null) return
        val data: Uri? = intent.data
        if (data != null && data.scheme == "myappshrimp" && data.host == "reset-password") {
            val token = data.getQueryParameter("token")
            if (token != null) {
                verifyTokenAndResetPassword(token)
            }
        }
    }

    private fun findPhoneByEmail(email: String) {
        Toast.makeText(this, "Đang tìm tài khoản...", Toast.LENGTH_SHORT).show()

        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Email này chưa được đăng ký", Toast.LENGTH_SHORT).show()
                } else {
                    val phoneNumber = documents.documents[0].id
                    sendResetLink(email, phoneNumber)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun sendResetLink(email: String, phone: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("email", email)
                    put("phone", phone)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$API_URL/send-reset-link")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showSuccessDialog(email)
                    } else {
                        val error = JSONObject(responseBody ?: "{}").optString("error", "Lỗi không xác định")
                        Toast.makeText(this@ForgotPasswordActivity, "Lỗi: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ForgotPasswordActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSuccessDialog(email: String) {
        AlertDialog.Builder(this)
            .setTitle("Thành công!")
            .setMessage("Link đặt lại mật khẩu đã được gửi đến email: $email\n\nVui lòng kiểm tra hộp thư và nhấn vào link để tiếp tục.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .show()
    }

    private fun verifyTokenAndResetPassword(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("token", token)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$API_URL/verify-reset-token")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody ?: "{}")
                        val phone = jsonResponse.getString("phone")
                        showPasswordResetDialog(token, phone)
                    } else {
                        val error = JSONObject(responseBody ?: "{}").optString("error", "Token không hợp lệ")
                        Toast.makeText(this@ForgotPasswordActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ForgotPasswordActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showPasswordResetDialog(token: String, phone: String) {
        val passwordInput = EditText(this).apply {
            hint = "Nhập mật khẩu mới (tối thiểu 6 ký tự)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle("Đặt lại mật khẩu")
            .setMessage("Nhập mật khẩu mới cho số điện thoại: $phone")
            .setView(passwordInput)
            .setPositiveButton("Đặt lại") { _, _ ->
                val newPassword = passwordInput.text.toString().trim()
                if (newPassword.length >= 6) {
                    resetPassword(token, phone, newPassword)
                } else {
                    Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun resetPassword(token: String, phone: String, newPassword: String) {
        val hashedPassword = hashPassword(newPassword)

        firestore.collection("users")
            .document(phone)
            .update("password", hashedPassword)
            .addOnSuccessListener {
                // Đánh dấu token đã sử dụng
                completeReset(token)

                Toast.makeText(this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi cập nhật mật khẩu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun completeReset(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("token", token)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$API_URL/complete-reset")
                    .post(body)
                    .build()

                client.newCall(request).execute()
            } catch (_: Exception) {
                // Bỏ qua lỗi
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
