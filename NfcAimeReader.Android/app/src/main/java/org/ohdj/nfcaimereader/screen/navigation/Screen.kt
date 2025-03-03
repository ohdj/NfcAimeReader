package org.ohdj.nfcaimereader.screen.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "主页", Icons.Filled.Home)
    data object Card : Screen("card", "卡片", Icons.Filled.AddCircle)
    data object Settings : Screen("settings", "设置", Icons.Filled.Settings)
}