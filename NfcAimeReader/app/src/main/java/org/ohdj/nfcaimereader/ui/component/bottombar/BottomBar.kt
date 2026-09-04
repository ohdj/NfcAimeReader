package org.ohdj.nfcaimereader.ui.component.bottombar

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.ui.LocalUiMode
import org.ohdj.nfcaimereader.ui.UiMode
import org.ohdj.nfcaimereader.ui.component.PagerNavigationSpringSpec
import org.ohdj.nfcaimereader.ui.util.shouldShowSplitPane
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import kotlin.math.abs

class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
    private val animatePageChanges: Boolean,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()

        selectedPage = targetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                if (animatePageChanges) {
                    pagerState.springAnimateToPage(targetIndex)
                } else {
                    pagerState.scrollToPage(targetIndex)
                }
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

private suspend fun PagerState.springAnimateToPage(target: Int) {
    if (target !in 0 until pageCount) return
    var shouldSnapToTarget = false
    scroll(MutatePriority.UserInput) {
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val distance = target - currentPage - currentPageOffsetFraction
        val scrollPixels = distance * pageSize
        if (abs(scrollPixels) <= 0.5f) return@scroll

        var consumedScroll = 0f
        var skipScroll = false
        Animatable(0f).animateTo(
            targetValue = scrollPixels,
            animationSpec = PagerNavigationSpringSpec,
        ) {
            if (skipScroll) return@animateTo

            val delta = value - consumedScroll
            if (abs(delta) > 0.5f) {
                val consumed = scrollBy(delta)
                consumedScroll += consumed
                if (abs(delta - consumed) > 0.1f) {
                    shouldSnapToTarget = true
                    skipScroll = true
                }
            } else {
                consumedScroll = value
            }

            if (abs(velocity) < 0.1f && abs(scrollPixels - consumedScroll) < 1.0f) {
                skipScroll = true
            }
        }

        val remaining = scrollPixels - consumedScroll
        if (abs(remaining) > 0.5f) {
            scrollBy(remaining)
        }
    }

    if (shouldSnapToTarget || currentPage != target) {
        scrollToPage(target)
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    animatePageChanges: Boolean = true,
): MainPagerState {
    return remember(pagerState, coroutineScope, animatePageChanges) {
        MainPagerState(pagerState, coroutineScope, animatePageChanges)
    }
}

@Composable
fun useNavigationRail(enableFloatingBottomBar: Boolean): Boolean {
    return shouldShowSplitPane() && !(LocalUiMode.current == UiMode.Miuix && enableFloatingBottomBar)
}

@Composable
fun BottomBar(
    blurBackdrop: LayerBackdrop?,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> BottomBarMiuix(blurBackdrop, backdrop, modifier)
        UiMode.Material -> BottomBarMaterial()
    }
}
