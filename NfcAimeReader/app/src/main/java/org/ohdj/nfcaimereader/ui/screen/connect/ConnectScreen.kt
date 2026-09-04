package org.ohdj.nfcaimereader.ui.screen.connect

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import org.ohdj.nfcaimereader.R
import org.ohdj.nfcaimereader.ui.LocalUiMode
import org.ohdj.nfcaimereader.ui.UiMode
import org.ohdj.nfcaimereader.ui.screen.placeholder.PlaceholderScreenMaterial
import org.ohdj.nfcaimereader.ui.screen.placeholder.PlaceholderScreenMiuix

@Composable
fun ConnectPager(
    bottomInnerPadding: Dp,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> PlaceholderScreenMiuix(stringResource(R.string.connect), bottomInnerPadding)
        UiMode.Material -> PlaceholderScreenMaterial(stringResource(R.string.connect), bottomInnerPadding)
    }
}
