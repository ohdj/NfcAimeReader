package org.ohdj.nfcaimereader.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import org.ohdj.nfcaimereader.R
import org.ohdj.nfcaimereader.ui.LocalUiMode
import org.ohdj.nfcaimereader.ui.UiMode
import org.ohdj.nfcaimereader.ui.screen.placeholder.PlaceholderScreenMaterial
import org.ohdj.nfcaimereader.ui.screen.placeholder.PlaceholderScreenMiuix

@Composable
fun HomePager(
    bottomInnerPadding: Dp,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> PlaceholderScreenMiuix(stringResource(R.string.home), bottomInnerPadding)
        UiMode.Material -> PlaceholderScreenMaterial(stringResource(R.string.home), bottomInnerPadding)
    }
}
