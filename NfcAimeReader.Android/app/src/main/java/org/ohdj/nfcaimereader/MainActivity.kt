package org.ohdj.nfcaimereader

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import org.ohdj.nfcaimereader.data.datastore.UserPreferenceViewModel
import org.ohdj.nfcaimereader.ui.navigation.Navigation
import org.ohdj.nfcaimereader.ui.theme.NfcAimeReaderTheme
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.NfcState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var nfcManager: NfcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcManager = NfcManager(this)

        setContent {
            val viewModel: UserPreferenceViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            enableEdgeToEdge(
                statusBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                },
                navigationBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
            )

            val supportsDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            NfcAimeReaderTheme(
                darkTheme = darkTheme,
                dynamicColor = supportsDynamicTheming && dynamicColorEnabled
            ) {
                Navigation(nfcManager, viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 刷新NFC状态
        nfcManager.refreshNfcState()

        // 只有在NFC可用且已启用的情况下才启用读取模式
        val nfcState = nfcManager.nfcState.value
        if (nfcState == NfcState.ENABLED) {
            nfcManager.enableNfcReaderMode()
        }
    }

    override fun onPause() {
        super.onPause()
        // 总是禁用NFC读取模式，无论设备是否支持
        nfcManager.disableNfcReaderMode()
    }
}