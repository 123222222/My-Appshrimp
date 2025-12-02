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
import com.dung.myapplication.mainUI.logout.LogoutViewModel

@Composable
fun LogoutScreen(
    context: Context,
    onCancel: () -> Unit = {},
    logoutViewModel: LogoutViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val users = logoutViewModel.users

    Scaffold(
        topBar = { MyTopBar("Đăng xuất") }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hiển thị toàn bộ user từ LogoutViewModel
                users.forEach { user ->
                    UserCardView(user = user)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Nút Đăng xuất
                Button(onClick = {
                    logoutViewModel.logout()

                    // Mở LoginActivity và xóa toàn bộ stack
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }) {
                    Text("Đăng xuất")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Nút Hủy
                OutlinedButton(onClick = onCancel) {
                    Text("Hủy")
                }
            }
        }
    }
}
