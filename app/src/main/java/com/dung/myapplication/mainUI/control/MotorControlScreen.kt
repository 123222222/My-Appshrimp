package com.dung.myapplication.mainUI.control

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dung.myapplication.mainUI.common.MyTopBar
import com.dung.myapplication.mainUI.common.MyBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorControlScreen(
    onHomeClick: () -> Unit = {},
    onChartClick: () -> Unit = {},
    onControlClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val raspDeviceId = prefs.getString("rasp_device_id", null)

    Scaffold(
        topBar = {
            MyTopBar(
                title = "Điều khiển động cơ",
                onBack = onBackClick
            )
        },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
                onGalleryClick = onGalleryClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick,
                onChartClick = onChartClick,
                onControlClick = onControlClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (raspDeviceId == null) {
                // Not bound to any device
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Chưa kết nối thiết bị",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vui lòng vào trang Hồ sơ để quét và kết nối thiết bị Raspberry Pi trước khi sử dụng chức năng này.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onProfileClick) {
                        Text("Đến trang Hồ sơ")
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚙️",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tính năng đang phát triển",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
