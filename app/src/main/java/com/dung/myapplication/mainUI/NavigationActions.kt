package com.dung.myapplication.mainUI

import androidx.navigation.NavController

@JvmInline
value class NavigationActions(private val navController: NavController) {
    fun push(any: Any) = navController.navigate(any) { launchSingleTop = true }

    fun replace(any: Any) {
        val current = navController.currentDestination?.route
            ?: error("Current destination is not available")
        navController.navigate(any) {
            popUpTo(current) { inclusive = true }
        }
    }

    private fun replaceRoot(any: Any) {
        navController.navigate(any) {
            popUpTo(Home) { inclusive = true }
        }
    }

    val back: () -> Unit get() = { navController.navigateUp() }

    operator fun invoke(route: Any): () -> Unit = { push(route) }

    fun <T> backWithResult(key: String): (T) -> Unit = { result ->
        requireNotNull(navController.previousBackStackEntry)
            .savedStateHandle[key] = result
        back()
    }
}
