package com.dung.myapplication.mainUI.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MyBottomBar(
    onHomeClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onChartClick: () -> Unit = {},
    onControlClick: () -> Unit = {}
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Đảo lại thứ tự các nút: Home - Chart - Control - Gallery - Profile - Logout
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onHomeClick() }) {
                Icon(Icons.Default.Home, contentDescription = "Home")
                Text("Home", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onChartClick() }) {
                Icon(Icons.Default.BarChart, contentDescription = "Chart")
                Text("Chart", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onControlClick() }) {
                Icon(Icons.Default.Settings, contentDescription = "Control")
                Text("Control", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onGalleryClick() }) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                Text("Gallery", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onProfileClick() }) {
                Icon(Icons.Default.Person, contentDescription = "Profile")
                Text("Profile", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onLogoutClick() }) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                Text("Logout", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
