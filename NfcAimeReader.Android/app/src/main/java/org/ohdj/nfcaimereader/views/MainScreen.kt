package org.ohdj.nfcaimereader.views

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import org.ohdj.nfcaimereader.utils.*

@Composable
fun MainScreen(
    nfcManager: NfcManager,
    networkScanner: NetworkScanner
) {
    var isConnected by remember { mutableStateOf(false) }
    var serverAddress by remember { mutableStateOf("") }
    val context = LocalContext.current
    val webSocketManager = remember { WebSocketManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val nfcStateReceiver = remember { NfcStateBroadcastReceiver() }
    val isNfcEnabled by nfcStateReceiver.nfcState.collectAsState(initial = false)

    // 注册和注销广播接收器
    DisposableEffect(context) {
        nfcStateReceiver.register(context)
        onDispose {
            nfcStateReceiver.unregister(context)
        }
    }

    // Observe saved server address
    LaunchedEffect(Unit) {
        webSocketManager.getServerAddress().collect { savedAddress ->
            savedAddress?.let { serverAddress = it }
        }
    }

    // WebSocket connection handling
    val webSocketCallback = object : WebSocketManager.WebSocketCallback {
        override fun onConnected() {
            isConnected = true
        }

        override fun onDisconnected() {
            isConnected = false
        }

        override fun onMessageReceived(message: String) {
            // Handle incoming messages if needed
        }

        override fun onError(errorMessage: String) {
            // Show error dialog
            // TODO: Implement error handling UI
        }
    }

    if (!isConnected) {
        ConnectionScreen(
            networkScanner = networkScanner,
            webSocketManager = webSocketManager,
            onConnectSuccess = {
                webSocketManager.connect(serverAddress, webSocketCallback)
            }
        )
    } else {
        NfcCardScreen(
            nfcManager = nfcManager,
            webSocketManager = webSocketManager,
            isNfcEnabled = isNfcEnabled,
            onDisconnect = {
                webSocketManager.disconnect()
                isConnected = false
            }
        )
    }
}