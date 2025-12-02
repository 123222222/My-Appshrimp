package com.dung.myapplication.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.dung.myapplication.MainActivity
import com.dung.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100
    private lateinit var biometricExecutor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // 🔹 Nếu đã đăng nhập → vào Main luôn
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<TextView>(R.id.signUpButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)
        val googleSignInButton = findViewById<SignInButton>(R.id.googleSignInButton)
        val fingerprintButton = findViewById<TextView>(R.id.fingerprintButton)

        // Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Biometric setup
        biometricExecutor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, biometricExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val user = auth.currentUser
                    if (user != null) {
                        user.getIdToken(true).addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idToken = tokenTask.result?.token
                                if (idToken != null) saveIdTokenToPrefs(idToken)
                            }
                            Toast.makeText(applicationContext, "Đăng nhập vân tay thành công", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Vui lòng đăng nhập trước khi dùng vân tay", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Lỗi: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Vân tay không khớp", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Đăng nhập bằng vân tay")
            .setSubtitle("Sử dụng vân tay để đăng nhập nhanh")
            .setNegativeButtonText("Hủy")
            .build()

        // Fingerprint click
        fingerprintButton.setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> biometricPrompt.authenticate(promptInfo)
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Toast.makeText(this, "Thiết bị không có cảm biến vân tay", Toast.LENGTH_SHORT).show()
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Toast.makeText(this, "Cảm biến vân tay đang không khả dụng", Toast.LENGTH_SHORT).show()
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Toast.makeText(this, "Chưa cài đặt vân tay trong hệ thống", Toast.LENGTH_SHORT).show()
            }
        }

        // Email/Password login
        loginButton.setOnClickListener {
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ email và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idToken = tokenTask.result?.token
                                if (idToken != null) saveIdTokenToPrefs(idToken)
                            }
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // SignUp click
        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // ForgotPassword click
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Google Sign-In
        googleSignInButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    private fun saveIdTokenToPrefs(idToken: String) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().putString("idToken", idToken).apply()
    }

    // Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            auth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val idToken = tokenTask.result?.token
                                    if (idToken != null) saveIdTokenToPrefs(idToken)
                                }
                                Toast.makeText(this, "Đăng nhập Google thành công: ${account.email}", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
