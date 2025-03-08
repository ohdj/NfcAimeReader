package org.ohdj.nfcaimereader

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.ohdj.nfcaimereader.data.datastore.UserPreferenceViewModel
import org.ohdj.nfcaimereader.ui.screen.card.CardScreen
import org.ohdj.nfcaimereader.ui.screen.home.HomeScreen
import org.ohdj.nfcaimereader.ui.navigation.AppNavigationBar
import org.ohdj.nfcaimereader.ui.screen.Screen
import org.ohdj.nfcaimereader.ui.screen.setting.SettingScreen
import org.ohdj.nfcaimereader.ui.screen.websocket.WebSocketDetailScreen
import org.ohdj.nfcaimereader.ui.theme.NfcAimeReaderTheme
import org.ohdj.nfcaimereader.utils.NfcManager

class MainActivity : ComponentActivity() {
    private lateinit var nfcManager: NfcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        nfcManager = NfcManager(this)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val viewModel: UserPreferenceViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val supportsDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            NfcAimeReaderTheme(
                darkTheme = darkTheme,
                dynamicColor = supportsDynamicTheming && dynamicColorEnabled
            ) {
                Scaffold(
                    bottomBar = {
                        AppNavigationBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    navController.graph.startDestinationRoute?.let { startRoute ->
                                        popUpTo(startRoute) {
                                            saveState = true
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(nfcManager)
                        }
                        composable(Screen.Card.route) {
                            CardScreen()
                        }
                        composable(Screen.Settings.route) {
                            SettingScreen(viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableNfcReaderMode()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableNfcReaderMode()
    }
}