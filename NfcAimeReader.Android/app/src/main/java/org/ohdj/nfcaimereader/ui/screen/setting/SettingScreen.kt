package org.ohdj.nfcaimereader.ui.screen.setting

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ohdj.nfcaimereader.ThemeMode
import org.ohdj.nfcaimereader.data.datastore.UserPreferenceViewModel
import org.ohdj.nfcaimereader.ui.screen.setting.component.SettingSwitchItem
import org.ohdj.nfcaimereader.ui.screen.setting.component.SettingThemeItem

@Composable
fun SettingScreen(viewModel: UserPreferenceViewModel) {
    val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState(initial = false)
    val supportsDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    SettingScreenContent(
        themeMode = themeMode,
        dynamicColorEnabled = dynamicColorEnabled,
        supportsDynamicTheming = supportsDynamicTheming,
        onThemeSelected = { viewModel.updateThemeMode(it) },
        onDynamicColorChanged = { viewModel.updateDynamicColorEnabled(it) }
    )
}

@Composable
fun SettingScreenContent(
    themeMode: ThemeMode,
    dynamicColorEnabled: Boolean,
    supportsDynamicTheming: Boolean,
    onThemeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "主题",
            color = MaterialTheme.colorScheme.primary
        )
        SettingSwitchItem(
            title = "动态取色",
            description = "使用系统提供的配色方案",
            checked = dynamicColorEnabled,
            enabled = supportsDynamicTheming,
            errorMessage = if (!supportsDynamicTheming) "此功能需要 Android 12+" else null,
            onCheckedChange = onDynamicColorChanged
        )
        SettingThemeItem(
            currentTheme = themeMode,
            onThemeSelected = onThemeSelected
        )
    }
}

// Android 12+
@Preview(name = "Android 12+", showBackground = true)
@Composable
fun SettingScreenPreviewAboveAndroid12() {
    SettingScreenContent(
        themeMode = ThemeMode.SYSTEM,
        dynamicColorEnabled = true,
        supportsDynamicTheming = true,
        onThemeSelected = {},
        onDynamicColorChanged = {}
    )
}

// Android 12-
@Preview(name = "Android 12-", showBackground = true)
@Composable
fun SettingScreenPreviewBelowAndroid12() {
    SettingScreenContent(
        themeMode = ThemeMode.SYSTEM,
        dynamicColorEnabled = false,
        supportsDynamicTheming = false,
        onThemeSelected = {},
        onDynamicColorChanged = {}
    )
}