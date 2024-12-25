package org.ohdj.nfcaimereader

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.ohdj.nfcaimereader.views.MainScreen
import org.ohdj.nfcaimereader.views.SettingsView

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable(
            route = "main",
            enterTransition = { sharedAxisEnterTransition(forward = true) },
            exitTransition = { sharedAxisExitTransition(forward = true) },
            popEnterTransition = { sharedAxisEnterTransition(forward = false) },
            popExitTransition = { sharedAxisExitTransition(forward = false) }
        ) {
            MainScreen(navController)
        }

        composable(
            route = "settings",
            enterTransition = { sharedAxisEnterTransition(forward = true) },
            exitTransition = { sharedAxisExitTransition(forward = true) },
            popEnterTransition = { sharedAxisEnterTransition(forward = false) },
            popExitTransition = { sharedAxisExitTransition(forward = false) }
        ) {
            SettingsView(navController)
        }
    }
}

fun sharedAxisEnterTransition(forward: Boolean): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { if (forward) it else -it }, // forward = 从右侧滑入, backward = 从左侧滑入
        animationSpec = tween(500)
    ) + fadeIn(animationSpec = tween(500)) // 淡入效果
}

fun sharedAxisExitTransition(forward: Boolean): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { if (forward) -it else it }, // forward = 向左侧滑出, backward = 向右侧滑出
        animationSpec = tween(500)
    ) + fadeOut(animationSpec = tween(500)) // 淡出效果
}