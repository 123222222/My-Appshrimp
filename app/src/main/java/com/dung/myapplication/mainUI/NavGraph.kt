package com.dung.myapplication.mainUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dung.myapplication.mainUI.home.HomeScreen
import com.dung.myapplication.mainUI.logout.LogoutScreen
import com.dung.myapplication.mainUI.profile.ProfileScreen
import com.dung.myapplication.mainUI.home.CameraStreamScreen
import com.dung.myapplication.mainUI.gallery.GalleryScreen
import com.dung.myapplication.mainUI.gallery.ImageDetailScreen
import com.dung.myapplication.mainUI.control.MotorControlScreen
import com.dung.myapplication.mainUI.chart.ChartScreen
import com.dung.myapplication.mainUI.profile.ProfileViewModel
import com.dung.myapplication.mainUI.logout.LogoutViewModel
import java.net.URLEncoder

// Define string routes for parameterized screens
private const val ROUTE_CAMERA = "cameraStream"
private const val ROUTE_CAMERA_WITH_PARAM = "cameraStream?streamUrl={streamUrl}"
private const val ROUTE_IMAGE_DETAIL = "imageDetail/{imageId}"

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
                onControlClick = { navController.navigate(MotorControl) },
                onChartClick = { navController.navigate(Chart) },
                onGalleryClick = { navController.navigate(Gallery) },
                onProfileClick = { navController.navigate(Profile) },
                onLogoutClick = { navController.navigate(Logout) }
            )
        }

        // 🕹️ Control
        composable<MotorControl> {
            MotorControlScreen(
                baseUrl = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev",
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // 📊 Chart
        composable<Chart> {
            ChartScreen(
                onBackClick = { navController.navigateUp() },
                onChartClick = { /* đang ở Chart */ },
                onHomeClick = { navController.navigate(Home) },
                onGalleryClick = { navController.navigate(Gallery) },
                onProfileClick = { navController.navigate(Profile) },
                onLogoutClick = { navController.navigate(Logout) },
                onControlClick = { navController.navigate(MotorControl) }
            )
        }

        // 👤 Profile
        composable<Profile> {
            val context = LocalContext.current
            ProfileScreen(
                onHomeClick = { navController.navigate(Home) },
                onChartClick = { navController.navigate(Chart) },
                onControlClick = { navController.navigate(MotorControl) },
                onGalleryClick = { navController.navigate(Gallery) },
                onLogoutClick = { navController.navigate(Logout) },
                onProfileClick = { /* đang ở Profile */ },
                onBackClick = { navController.navigateUp() },
                users = emptyList(), // Truyền danh sách rỗng để trang Profile trống
                context = context // truyền context vào đây
            )
        }

        // 🚪 Logout
        composable<Logout> {
            val context = LocalContext.current
            val profileViewModel = androidx.hilt.navigation.compose.hiltViewModel<ProfileViewModel>()
            val logoutViewModel = androidx.hilt.navigation.compose.hiltViewModel<LogoutViewModel>()

            // Transfer profile users into logout viewmodel (so logout shows the same user data)
            logoutViewModel.setUsers(profileViewModel.users)

            LogoutScreen(
                context = context,
                onCancel = { navController.navigate(Home) }
            )
        }

        // 📹 Camera Stream (string route with query param)
        composable(ROUTE_CAMERA_WITH_PARAM) { backStackEntry ->
            // Bọc lấy argument trong try/catch để tránh crash
            val streamUrl = try {
                backStackEntry.arguments?.getString("streamUrl")
            } catch (e: Exception) {
                null
            } ?: "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev/blynk_feed"
            CameraStreamScreen(
                streamUrl = streamUrl,
                onBackClick = { navController.navigateUp() },
                onHomeClick = { navController.navigate(Home) },
                onGalleryClick = { navController.navigate(Gallery) },
                onProfileClick = { navController.navigate(Profile) },
                onLogoutClick = { navController.navigate(Logout) }
            )
        }

        // 🖼️ Gallery
        composable<Gallery> {
            GalleryScreen(
                onHomeClick = { navController.navigate(Home) },
                onGalleryClick = { /* đang ở Gallery */ },
                onProfileClick = { navController.navigate(Profile) },
                onLogoutClick = { navController.navigate(Logout) },
                onImageClick = { imageId ->
                    val encoded = URLEncoder.encode(imageId, "UTF-8")
                    navController.navigate("imageDetail/$encoded")
                },
                onChartClick = { navController.navigate(Chart) },
                onControlClick = { navController.navigate(MotorControl) }
            )
        }

        // 🔍 Image Detail
        composable(ROUTE_IMAGE_DETAIL) { backStackEntry ->
            // Bọc lấy argument trong try/catch để tránh crash
            val imageId = try {
                backStackEntry.arguments?.getString("imageId")
            } catch (e: Exception) {
                null
            } ?: ""
            ImageDetailScreen(
                imageId = imageId,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}