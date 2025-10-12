package com.dung.myapplication.mainUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dung.myapplication.mainUI.home.HomeScreen
import com.dung.myapplication.mainUI.logout.LogoutScreen
import com.dung.myapplication.mainUI.menu.MenuScreen
import com.dung.myapplication.mainUI.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier
    ) {
        // 🏠 Home
        composable<Home> {
            HomeScreen(
                onHomeClick = { /* đang ở Home */ },
                onMenuClick = { navController.navigate(Menu) },
                onProfileClick = { navController.navigate(Profile) },
                onLogoutClick = { navController.navigate(Logout) }
            )
        }

        // 📋 Menu
        composable<Menu> {
            MenuScreen(
                onHomeClick = { navController.navigate(Home) },
                onMenuClick = { /* đang ở Menu */ },
                onProfileClick = { navController.navigate(Profile) },
                onLogoutClick = { navController.navigate(Logout) }
            )
        }

        // 👤 Profile
        composable<Profile> {
            ProfileScreen(
                onHomeClick = { navController.navigate(Home) },
                onMenuClick = { navController.navigate(Menu) },
                onProfileClick = { /* đang ở Profile */ },
                onLogoutClick = { navController.navigate(Logout) }
            )
        }

        // 🚪 Logout
        composable<Logout> {
            val context = LocalContext.current
            LogoutScreen(
                context = context,
                onCancel = { navController.navigate(Home) }
            )
        }
    }
}
