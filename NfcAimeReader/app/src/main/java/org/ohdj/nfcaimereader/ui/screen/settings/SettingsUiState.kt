package org.ohdj.nfcaimereader.ui.screen.settings

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import org.ohdj.nfcaimereader.ui.UiMode

@Immutable
data class SettingsUiState(
    val uiMode: String = UiMode.DEFAULT_VALUE,
    val checkUpdate: Boolean = true,
    val themeMode: Int = 0,
    val miuixMonet: Boolean = false,
    val keyColor: Int = 0,
    val colorStyle: String = PaletteStyle.TonalSpot.name,
    val colorSpec: String = ColorSpec.SpecVersion.SPEC_2025.name,
    val enablePredictiveBack: Boolean = false,
    val enableBlur: Boolean = true,
    val enableFloatingBottomBar: Boolean = false,
    val enableFloatingBottomBarBlur: Boolean = false,
    val pageScale: Float = 1.0f,
)

@Immutable
data class SettingsScreenActions(
    val onSetCheckUpdate: (Boolean) -> Unit,
    val onOpenTheme: () -> Unit,
    val onSetUiModeIndex: (Int) -> Unit,
    val onOpenAbout: () -> Unit,
)
