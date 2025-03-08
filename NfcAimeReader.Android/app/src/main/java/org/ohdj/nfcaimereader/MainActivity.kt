package org.ohdj.nfcaimereader

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.ohdj.nfcaimereader.data.datastore.UserPreferenceViewModel
import org.ohdj.nfcaimereader.ui.navigation.BottomNavigation
import org.ohdj.nfcaimereader.ui.navigation.BottomNavigationItem
import org.ohdj.nfcaimereader.ui.screen.card.CardScreen
import org.ohdj.nfcaimereader.ui.screen.home.HomeScreen
import org.ohdj.nfcaimereader.ui.screen.setting.SettingScreen
import org.ohdj.nfcaimereader.ui.screen.websocket.WebSocketDetailScreen
import org.ohdj.nfcaimereader.ui.theme.NfcAimeReaderTheme
import org.ohdj.nfcaimereader.utils.NfcManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var nfcManager: NfcManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
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
                // 关于
                var expanded by remember { mutableStateOf(false) }
                var showAbout by remember { mutableStateOf(false) }
                if (showAbout) {
                    AboutDialog(onDismiss = { showAbout = false })
                }

                Scaffold(
                    bottomBar = {
                        BottomNavigation(
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
                    },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentRoute) {
                                        "home" -> "NfcAimeReader"
                                        "settings" -> "设置"
                                        "detail" -> "WebSocket"
                                        else -> ""
                                    }
                                )
                            },
                            navigationIcon = {
                                if (currentRoute == "detail") {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "返回"
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentRoute == "home") {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = Icons.Filled.MoreVert,
                                            contentDescription = "More options"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("关于") },
                                            onClick = {
                                                showAbout = true
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavigationItem.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomNavigationItem.Home.route) {
                            HomeScreen(
                                nfcManager = nfcManager,
                                navigateToWebSocketDetail = {
                                    navController.navigate("detail")
                                }
                            )
                        }
                        composable("detail") {
                            WebSocketDetailScreen()
                        }
                        composable(BottomNavigationItem.Card.route) {
                            CardScreen()
                        }
                        composable(BottomNavigationItem.Setting.route) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                )

                Spacer(modifier = Modifier.width(24.dp))

                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "v0.0.3",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(buildAnnotatedString {
                        append("在 ")
                        withLink(
                            LinkAnnotation.Url(
                                url = "https://github.com/ohdj/NfcAimeReader",
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            )
                        ) {
                            append("GitHub")
                        }
                        append(" 查看源码")
                    })
                }
            }
        }
    }
}