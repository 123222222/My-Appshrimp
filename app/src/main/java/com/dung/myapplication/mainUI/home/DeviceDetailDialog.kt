package com.dung.myapplication.mainUI.home

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DeviceDetailDialog(
    ipAddress: String,
    name: String,
    onLogin: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Thông tin thiết bị", style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                text = "Tên thiết bị: $name\nĐịa chỉ IP: $ipAddress",
                fontWeight = FontWeight.Medium
            )
        },
        confirmButton = {
            Button(onClick = onLogin) {
                Text("Đăng nhập")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Hủy")
            }
        }
    )
}
