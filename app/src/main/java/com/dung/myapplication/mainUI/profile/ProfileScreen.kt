package com.dung.myapplication.mainUI.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.mainUI.common.MyTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onHomeClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Scaffold(
        topBar = { MyTopBar("Hồ sơ") },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(viewModel.users) { user ->
                UserCardView(user = user)
            }
        }
    }
}
