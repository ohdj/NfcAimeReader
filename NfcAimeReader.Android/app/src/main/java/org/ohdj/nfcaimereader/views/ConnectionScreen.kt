package org.ohdj.nfcaimereader.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.utils.NetworkScanner
import org.ohdj.nfcaimereader.utils.WebSocketManager

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