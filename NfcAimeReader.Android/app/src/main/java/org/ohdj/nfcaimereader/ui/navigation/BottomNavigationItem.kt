package org.ohdj.nfcaimereader.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItem(val route: String, val title: String, val icon: ImageVector) {
    data object Home : BottomNavigationItem("home", "主页", Icons.Filled.Home)
    data object Card : BottomNavigationItem("card", "卡片", Icons.Filled.AddCircle)
    data object Setting : BottomNavigationItem("setting", "设置", Icons.Filled.Settings)
}