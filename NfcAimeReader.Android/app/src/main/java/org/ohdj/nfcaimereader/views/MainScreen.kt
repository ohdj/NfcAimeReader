package org.ohdj.nfcaimereader.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.utils.NetworkScanner
import org.ohdj.nfcaimereader.utils.NfcManager

@Composable
fun MainScreen(nfcManager: NfcManager, networkScanner: NetworkScanner) {
    val scope = rememberCoroutineScope()
    val cardIdm by nfcManager.cardIdm.collectAsState()
    val servers by networkScanner.servers.collectAsState()
    val isScanning by networkScanner.isScanning.collectAsState()
    val scanLogs by networkScanner.scanLogs.collectAsState()
    var selectedServer by remember { mutableStateOf<String?>(null) }
    var showLogs by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Scanned Card IDm: ${cardIdm ?: "No card scanned"}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { scope.launch { networkScanner.scanLocalNetwork() } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan for Servers")
        }

        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        Text(
            text = "Available Servers:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(servers) { server ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = server == selectedServer,
                        onClick = { selectedServer = server }
                    )
                    Text(text = server, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        if (servers.isEmpty() && !isScanning) {
            Text(
                text = "No servers found",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Checkbox(
                checked = showLogs,
                onCheckedChange = { showLogs = it }
            )
            Text("Show Logs", modifier = Modifier.padding(start = 8.dp))
        }

        if (showLogs) {
            LazyColumn(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            ) {
                items(scanLogs) { log ->
                    Text(text = log)
                }
            }
        }
    }
}