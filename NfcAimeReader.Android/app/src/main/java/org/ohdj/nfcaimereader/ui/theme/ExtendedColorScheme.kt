package org.ohdj.nfcaimereader.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColorScheme(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color
)

val ExtendedLightColorScheme = ExtendedColorScheme(
    success = successLight,
    onSuccess = onSuccessLight,
    successContainer = successContainerLight,
    onSuccessContainer = onSuccessContainerLight
)

val ExtendedDarkColorScheme = ExtendedColorScheme(
    success = successDark,
    onSuccess = onSuccessDark,
    successContainer = successContainerDark,
    onSuccessContainer = onSuccessContainerDark
)