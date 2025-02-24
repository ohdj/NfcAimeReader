package org.ohdj.nfcaimereader.presentation.screen.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(viewModel: SettingViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadAutoReconnect(context)
        viewModel.loadUseSystemTheme(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("设置")
                }
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = "主题",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
            ListItem(
                headlineContent = { Text("自动重连") },
                supportingContent = { Text("游戏连接断开后尝试重连") },
                trailingContent = {
                    Switch(
                        checked = viewModel.autoReconnect.collectAsState().value,
                        onCheckedChange = { viewModel.setAutoReconnect(context, it) }
                    )
                }
            )
            ListItem(
                headlineContent = { Text("动态取色") },
                supportingContent = { Text("使用系统提供的配色方案") },
                trailingContent = {
                    Switch(
                        checked = viewModel.useSystemTheme.collectAsState().value,
                        onCheckedChange = { viewModel.setUseSystemTheme(context, it) }
                    )
                }
            )
        }
    }
}