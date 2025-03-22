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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ohdj.nfcaimereader.ThemeMode
import org.ohdj.nfcaimereader.data.datastore.UserPreferenceViewModel
import org.ohdj.nfcaimereader.ui.screen.setting.component.SettingSwitchItem
import org.ohdj.nfcaimereader.ui.screen.setting.component.SettingThemeItem

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = false,
    val supportsDynamicTheming: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
)

@Composable
fun SettingScreen(viewModel: UserPreferenceViewModel) {
    val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState(initial = false)
    val supportsDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val uiState = SettingsUiState(
        themeMode = themeMode,
        dynamicColorEnabled = dynamicColorEnabled,
        supportsDynamicTheming = supportsDynamicTheming
    )

    SettingScreenContent(
        uiState = uiState,
        onThemeSelected = { viewModel.updateThemeMode(it) },
        onDynamicColorChanged = { viewModel.updateDynamicColorEnabled(it) }
    )
}

@Composable
fun SettingScreenContent(
    uiState: SettingsUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "主题",
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SettingSwitchItem(
            title = "动态取色",
            description = "使用系统提供的配色方案",
            checked = uiState.dynamicColorEnabled,
            enabled = uiState.supportsDynamicTheming,
            errorMessage = if (!uiState.supportsDynamicTheming) "此功能需要 Android 12+" else null,
            onCheckedChange = onDynamicColorChanged
        )
        SettingThemeItem(
            currentTheme = uiState.themeMode,
            onThemeSelected = onThemeSelected
        )
    }
}

// Android 12+
@Preview(name = "Android 12+", showBackground = true)
@Composable
fun SettingScreenPreviewAboveAndroid12() {
    SettingScreenContent(
        uiState = SettingsUiState(
            themeMode = ThemeMode.SYSTEM,
            dynamicColorEnabled = false,
            supportsDynamicTheming = true
        ),
        onThemeSelected = {},
        onDynamicColorChanged = {}
    )
}

// Android 12-
@Preview(name = "Android 12-", showBackground = true)
@Composable
fun SettingScreenPreviewBelowAndroid12() {
    SettingScreenContent(
        uiState = SettingsUiState(
            themeMode = ThemeMode.SYSTEM,
            dynamicColorEnabled = false,
            supportsDynamicTheming = false
        ),
        onThemeSelected = {},
        onDynamicColorChanged = {}
    )
}