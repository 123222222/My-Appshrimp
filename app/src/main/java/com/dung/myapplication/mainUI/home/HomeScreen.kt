package com.dung.myapplication.mainUI.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.mainUI.common.MyTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeScreenViewModel = hiltViewModel(),
    onHomeClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var selectedDevice by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = { MyTopBar("Network Device Scanner") },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { state.startScan() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Scan Network")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (state.scanStatus.value.isNotEmpty()) {
                Text(
                    text = state.scanStatus.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.deviceList) { device ->
                    IpCardView(device = device) {
                        Log.d("IPDevice", "Clicked: ${device.ipAddress}")
                        selectedDevice = device.ipAddress to device.name
                    }
                }
            }
        }

        // 🪟 Hiển thị popup khi chọn 1 thiết bị
        selectedDevice?.let { (ip, name) ->
            DeviceDetailDialog(
                ipAddress = ip,
                name = name,
                onLogin = {
                    selectedDevice = null
                    onMenuClick() // 🟢 chuyển sang trang Menu
                },
                onCancel = { selectedDevice = null }
            )
        }
    }
}
