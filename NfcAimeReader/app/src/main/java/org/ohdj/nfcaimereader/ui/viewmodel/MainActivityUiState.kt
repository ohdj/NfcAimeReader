package org.ohdj.nfcaimereader.ui.viewmodel

import androidx.compose.runtime.Immutable
import org.ohdj.nfcaimereader.ui.UiMode
import org.ohdj.nfcaimereader.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val enableBlur: Boolean,
    val enableFloatingBottomBar: Boolean,
    val enableFloatingBottomBarBlur: Boolean,
    val uiMode: UiMode,
)
