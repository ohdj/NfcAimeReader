package org.ohdj.nfcaimereader.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
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
import org.ohdj.nfcaimereader.ui.viewmodel.WebSocketViewModel
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.NfcStateBroadcastReceiver

@Composable
fun HomeScreen(
    nfcManager: NfcManager,
    navController: NavController,
    viewModel: WebSocketViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val nfcStateReceiver = remember { NfcStateBroadcastReceiver() }

    LaunchedEffect(Unit) {
        // 如果设备支持NFC，则注册广播接收器
        if (nfcManager.isNfcSupported()) {
            nfcStateReceiver.register(context)
        }

        // 刷新NFC状态
        nfcManager.refreshNfcState()
    }

    // Send card ID when detected
//    LaunchedEffect(cardIdm) {
//        cardIdm?.let { hexCardId ->
//            val decimalCardId = webSocketManager.convertCardId(hexCardId)
//            webSocketManager.sendCardId(decimalCardId)
//        }
//    }

    Column {
        // NFC 状态组件
        val cardIdm by nfcManager.cardIdm.collectAsState()

        // 获取NFC状态，优先使用nfcManager的状态
        val nfcState by if (nfcManager.isNfcSupported()) {
            nfcStateReceiver.nfcState.collectAsState()
        } else {
            nfcManager.nfcState.collectAsState()
        }
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