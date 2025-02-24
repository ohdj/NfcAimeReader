package org.ohdj.nfcaimereader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

        // 同步读取 DataStore 中存储的设置，确保在 setContent 中使用正确的初始值，避免应用启动时瞬间切换主题
        val useSystemThemeInitial = runBlocking {
            DataStoreManager.getUseSystemTheme(this@MainActivity)
        }

        setContent {
            // 使用从 DataStore 读取到的初始值作为 collectAsState 的 initial 值
            val useSystemTheme by DataStoreManager.getUseSystemThemeFlow(this@MainActivity)
                .collectAsState(initial = useSystemThemeInitial)
            NfcAimeReaderTheme(
                darkTheme = androidx.compose.foundation.isSystemInDarkTheme(),
                dynamicColor = useSystemTheme
            ) {
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