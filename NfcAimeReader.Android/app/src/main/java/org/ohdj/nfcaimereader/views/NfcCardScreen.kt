package org.ohdj.nfcaimereader.views

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.NfcStateBroadcastReceiver
import org.ohdj.nfcaimereader.utils.WebSocketManager

@Composable
fun NfcCardScreen(
    nfcManager: NfcManager,
    webSocketManager: WebSocketManager,
    isNfcEnabled: Boolean,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    val nfcStateReceiver = remember { NfcStateBroadcastReceiver() }
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

        // Conditional NFC Settings Button
        if (!isNfcEnabled) {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                    context.startActivity(intent)
                }
            ) {
                Text("Open NFC Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

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