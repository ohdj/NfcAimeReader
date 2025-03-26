package org.ohdj.nfcaimereader.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ohdj.nfcaimereader.ui.navigation.Screen
import org.ohdj.nfcaimereader.ui.screen.home.component.NfcStatusComponent
import org.ohdj.nfcaimereader.ui.screen.home.component.WebSocketStatusComponent
import org.ohdj.nfcaimereader.ui.viewmodel.HomeViewModel
import org.ohdj.nfcaimereader.ui.viewmodel.WebSocketScreenViewModel
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.NfcStateBroadcastReceiver
import org.ohdj.nfcaimereader.utils.collectNfcStateAsState
import org.ohdj.nfcaimereader.utils.rememberNfcStateManager

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: WebSocketScreenViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val nfcStateReceiver = remember { NfcStateBroadcastReceiver() }

    // 创建NFC状态管理器
    val nfcStateManager = rememberNfcStateManager(homeViewModel.nfcManager, nfcStateReceiver)

    // 只有当设备支持NFC时才注册广播接收器
    LaunchedEffect(Unit) {
        if (homeViewModel.nfcManager.isNfcSupported()) {
            nfcStateReceiver.register(context)
        }
    }

    // 在组件销毁时解注册广播接收器
    DisposableEffect(Unit) {
        onDispose {
            if (homeViewModel.nfcManager.isNfcSupported()) {
                nfcStateReceiver.unregister(context)
            }
        }
    }

    // Send card ID when detected
//    LaunchedEffect(cardIdm) {
//        cardIdm?.let { hexCardId ->
//            val decimalCardId = webSocketManager.convertCardId(hexCardId)
//            webSocketManager.sendCardId(decimalCardId)
//        }
//    }

    Column {
        // 获取NFC状态和卡片ID
        val nfcState = nfcStateManager.collectNfcStateAsState(context)
        val cardIdm by homeViewModel.nfcManager.cardIdm.collectAsState()

        // NFC状态组件
        NfcStatusComponent(
            nfcState = nfcState,
            cardIdm = cardIdm
        )

        Spacer(modifier = Modifier.height(16.dp))

        // WebSocket 状态组件
        val connectionState by viewModel.connectionState.collectAsState()
        WebSocketStatusComponent(
            connectionState = connectionState,
            onClick = {
                navController.navigate(Screen.Config.route)
            }
        )
    }
}