package org.ohdj.nfcaimereader.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
            onDisconnect = {
                webSocketManager.disconnect()
                isConnected = false
            }
        )
    }
}

@Composable
fun ConnectionScreen(
    networkScanner: NetworkScanner,
    webSocketManager: WebSocketManager,
    onConnectSuccess: () -> Unit
) {
    val context = LocalContext.current
    var serverAddress by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8080") }
    var discoveredServers by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("WebSocket Server Connection", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Manual IP Input
        OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("IP Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Port") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                serverAddress = "$ipAddress:$port"
                coroutineScope.launch {
                    webSocketManager.saveServerAddress(serverAddress)
                    onConnectSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect Manually")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    discoveredServers = networkScanner.scanWebSocketServers(context)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan Network")
        }

        // Display discovered servers
        discoveredServers.forEach { server ->
            TextButton(
                onClick = {
                    serverAddress = "$server:$port"
                    coroutineScope.launch {
                        webSocketManager.saveServerAddress(serverAddress)
                        onConnectSuccess()
                    }
                }
            ) {
                Text("Connect to: $server")
            }
        }
    }
}

@Composable
fun NfcCardScreen(
    nfcManager: NfcManager,
    webSocketManager: WebSocketManager,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    val nfcStateReceiver = remember { NfcStateBroadcastReceiver() }
    val isNfcEnabled by nfcStateReceiver.nfcState.collectAsState(initial = false)
    val cardIdm by nfcManager.cardIdm.collectAsState()

    // Check and request NFC permissions
    LaunchedEffect(Unit) {
        if (!nfcStateReceiver.isNfcEnabled(context)) {
            // TODO: Implement NFC enable request dialog
        }
    }

    // Send card ID when detected
    LaunchedEffect(cardIdm) {
        cardIdm?.let { hexCardId ->
            val decimalCardId = webSocketManager.convertCardId(hexCardId)
            webSocketManager.sendCardId(decimalCardId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("NFC Card Reader", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // NFC Status Display
        Text(
            text = if (isNfcEnabled) "NFC Enabled" else "NFC Disabled",
            color = if (isNfcEnabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Card ID
        Text(
            text = "Last Card ID: ${cardIdm ?: "No card scanned"}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disconnect")
        }
    }
}