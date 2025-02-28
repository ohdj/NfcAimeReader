package org.ohdj.nfcaimereader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.ohdj.nfcaimereader.presentation.screen.card.CardScreen
import org.ohdj.nfcaimereader.presentation.screen.card.CardViewModel
import org.ohdj.nfcaimereader.presentation.screen.home.HomeScreen
import org.ohdj.nfcaimereader.presentation.screen.home.HomeViewModel
import org.ohdj.nfcaimereader.presentation.screen.setting.SettingScreen
import org.ohdj.nfcaimereader.presentation.screen.setting.SettingViewModel
import org.ohdj.nfcaimereader.presentation.screen.settings.SettingsScreen
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.WebSocketManager

@Composable
fun App(nfcManager: NfcManager, webSocketManager: WebSocketManager, appViewModel: AppViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                composable("home") {
                    val viewModel: HomeViewModel = viewModel()
                    HomeScreen(nfcManager, webSocketManager)
                }
                composable("card") {
                    val viewModel: CardViewModel = viewModel()
                    CardScreen(viewModel)
                }
                composable("settings") {
                    SettingsScreen(appViewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentDestination == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            label = { Text("主页") },
            icon = { Icon(Icons.Default.Home, contentDescription = "home") }
        )
        NavigationBarItem(
            selected = currentDestination == "card",
            onClick = {
                navController.navigate("card") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            label = { Text("卡片") },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "card") }
        )
        NavigationBarItem(
            selected = currentDestination == "settings",
            onClick = {
                navController.navigate("settings") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            label = { Text("设置") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "settings") }
        )
    }
}