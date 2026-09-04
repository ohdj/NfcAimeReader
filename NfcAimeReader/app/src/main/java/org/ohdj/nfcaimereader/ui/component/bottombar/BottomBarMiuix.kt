package org.ohdj.nfcaimereader.ui.component.bottombar

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ohdj.nfcaimereader.R
import org.ohdj.nfcaimereader.ui.LocalMainPagerState
import org.ohdj.nfcaimereader.ui.component.FloatingBottomBar
import org.ohdj.nfcaimereader.ui.component.FloatingBottomBarItem
import org.ohdj.nfcaimereader.ui.theme.LocalEnableFloatingBottomBar
import org.ohdj.nfcaimereader.ui.theme.LocalEnableFloatingBottomBarBlur
import org.ohdj.nfcaimereader.ui.util.BlurredBar
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BottomBarMiuix(
    blurBackdrop: LayerBackdrop?,
    backdrop: Backdrop,
    modifier: Modifier,
) {
    val mainState = LocalMainPagerState.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current

    val items = BottomBarDestination.entries.map { destination ->
        NavigationItem(
            label = stringResource(destination.label),
            icon = destination.icon,
        )
    }
    if (!enableFloatingBottomBar) {
        BlurredBar(blurBackdrop) {
            NavigationBar(
                modifier = modifier,
                color = if (blurBackdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface,
                content = {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            modifier = Modifier.weight(1f),
                            icon = item.icon,
                            label = item.label,
                            selected = mainState.selectedPage == index,
                            onClick = {
                                mainState.animateToPage(index)
                            },
                        )
                    }
                }
            )
        }
    } else {
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            .let { inset -> if (inset != 0.dp) 8.dp + inset else 28.dp }
        FloatingBottomBar(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
                .padding(start = 28.dp, end = 28.dp, bottom = bottomPadding),
            selectedIndex = mainState.selectedPage,
            onSelected = { mainState.animateToPage(it) },
            backdrop = backdrop,
            tabsCount = items.size,
            isBlurEnabled = enableFloatingBottomBarBlur,
        ) { activateTab ->
            items.forEachIndexed { index, item ->
                FloatingBottomBarItem(
                    selected = mainState.selectedPage == index,
                    onClick = {
                        activateTab(index)
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 76.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible
                    )
                }
            }
        }
    }
}

enum class BottomBarDestination(
    @get:StringRes val label: Int,
    val icon: ImageVector,
) {
    Home(R.string.home, Icons.Rounded.Cottage),
    Connect(R.string.connect, Icons.Rounded.Nfc),
    Setting(R.string.settings, Icons.Rounded.Settings)
}
