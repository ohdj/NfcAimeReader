package org.ohdj.nfcaimereader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.WebSocketManager

class MainActivity : ComponentActivity() {
    private lateinit var nfcManager: NfcManager
    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        nfcManager = NfcManager(this)
        webSocketManager = WebSocketManager()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    App(nfcManager, webSocketManager)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableNfcReaderMode()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableNfcReaderMode()
    }
}