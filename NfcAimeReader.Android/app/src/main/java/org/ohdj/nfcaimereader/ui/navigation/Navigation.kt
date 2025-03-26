package org.ohdj.nfcaimereader.ui.navigation

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.ohdj.nfcaimereader.R
import org.ohdj.nfcaimereader.data.datastore.UserPreferenceViewModel
import org.ohdj.nfcaimereader.ui.screen.home.HomeScreen
import org.ohdj.nfcaimereader.ui.screen.setting.SettingScreen
import org.ohdj.nfcaimereader.ui.screen.websocket.WebSocketDetailScreen
import org.ohdj.nfcaimereader.utils.NfcManager

// 导航路径
sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "主页")
    data object Config : Screen("config", "WebSocket 配置")
    data object Setting : Screen("setting", "设置")
}

// 底部导航栏
private val bottomNavItem = listOf(
    Screen.Home to Icons.Filled.Home,
    Screen.Setting to Icons.Filled.Settings,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(viewModel: UserPreferenceViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = navBackStackEntry?.destination?.route

    // 关于
    var expanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentRoute) {
                            Screen.Home.route -> stringResource(id = R.string.app_name)
                            Screen.Config.route -> Screen.Config.title
                            Screen.Setting.route -> Screen.Setting.title
                            else -> ""
                        }
                    )
                },
                navigationIcon = {
                    if (currentRoute == Screen.Config.route) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (currentRoute == Screen.Home.route) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "MoreVert")
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
        },
        bottomBar = {
            // 进入深层导航时，隐藏底部导航栏
            if (currentRoute != Screen.Config.route) {
                NavigationBar {
                    bottomNavItem.forEach { (screen, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // 避免堆栈中创建多个副本
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // 防止多次点击多次入栈
                                    launchSingleTop = true
                                    // 恢复上次的状态
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Config.route) {
                WebSocketDetailScreen()
            }
            composable(Screen.Setting.route) {
                SettingScreen(viewModel)
            }
        }
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