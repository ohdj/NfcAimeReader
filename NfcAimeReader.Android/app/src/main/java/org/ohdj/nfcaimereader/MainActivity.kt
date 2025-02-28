package org.ohdj.nfcaimereader

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.runBlocking
import org.ohdj.nfcaimereader.ui.theme.NfcAimeReaderTheme
import org.ohdj.nfcaimereader.utils.DataStoreManager
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
            val viewModel: AppViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val supportsDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            NfcAimeReaderTheme(
                darkTheme = darkTheme,
                dynamicColor = supportsDynamicTheming && dynamicColorEnabled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    App(nfcManager, webSocketManager, viewModel)
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