package org.ohdj.nfcaimereader.presentation.screen.settings

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ohdj.nfcaimereader.AppViewModel
import org.ohdj.nfcaimereader.ThemeMode
import org.ohdj.nfcaimereader.presentation.screen.settings.components.SettingsSwitchItem
import org.ohdj.nfcaimereader.presentation.screen.settings.components.SettingsThemeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState(initial = false)
    val supportsDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "外观设置",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    SettingsSwitchItem(
                        title = "动态取色",
                        description = "使用您的壁纸颜色来美化应用",
                        checked = dynamicColorEnabled,
                        enabled = supportsDynamicTheming,
                        onCheckedChange = { viewModel.updateDynamicColorEnabled(it) }
                    )

                    if (!supportsDynamicTheming) {
                        Text(
                            text = "此功能需要 Android 12 +",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    SettingsThemeItem(
                        currentTheme = themeMode,
                        onThemeSelected = { viewModel.updateThemeMode(it) }
                    )
                }
            }
        }
    }
}