package com.dung.myapplication.mainUI.logout

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dung.myapplication.login.LoginActivity
import com.dung.myapplication.mainUI.common.MyTopBar

@Composable
fun LogoutScreen(
    context: Context,
    onCancel: () -> Unit = {}
) {
    Scaffold(
        topBar = { MyTopBar("Đăng xuất") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bạn có chắc muốn đăng xuất không?")
            Spacer(modifier = Modifier.height(12.dp))

            // Nút Đăng xuất
            Button(onClick = {
                // Đăng xuất khỏi Firebase (nếu dùng)
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

                // Mở LoginActivity và xóa toàn bộ stack
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }) {
                Text("Đăng xuất")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nút Hủy
            OutlinedButton(onClick = onCancel) {
                Text("Hủy")
            }
        }
    }
}
