package org.ohdj.nfcaimereader.presentation.screen.home

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ohdj.nfcaimereader.utils.NetworkScanner
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.NfcStateBroadcastReceiver
import org.ohdj.nfcaimereader.utils.WebSocketManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    // 使用单例模式获取NfcManager实例
    val nfcManager = remember { NfcManager.getInstance(context as Activity) }

    val websocketManager = WebSocketManager()
    val networkScanner = NetworkScanner(context)

    // NFC状态与读卡相关
    val cardIdm by nfcManager.cardIdm.collectAsState()
    val nfcStateReceiver = remember { NfcStateBroadcastReceiver() }

    LaunchedEffect(Unit) {
        nfcStateReceiver.register(context)

        // Check and request NFC permissions
        if (!nfcStateReceiver.isNfcEnabled(context)) {
            // TODO: Implement NFC enable request dialog
        }
    }

    // Send card ID when detected
//    LaunchedEffect(cardIdm) {
//        cardIdm?.let { hexCardId ->
//            val decimalCardId = webSocketManager.convertCardId(hexCardId)
//            webSocketManager.sendCardId(decimalCardId)
//        }
//    }

    // 通过 collectAsState 订阅状态更新
    val isNfcEnabled by nfcStateReceiver.nfcState.collectAsState(
        initial = nfcStateReceiver.isNfcEnabled(context)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("NfcAimeReader")
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("连接服务器") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Server") },
                onClick = {
                    showBottomSheet = true
                }
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            WebsocketStatusCard(websocketManager)

            // 读卡功能
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isNfcEnabled) "NFC Enabled" else "NFC Disabled",
                    color = if (isNfcEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Go to NFC Settings
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
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState,
//                windowInsets = WindowInsets.navigationBars
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {

                }
            }

            // 订阅扫描状态
            val scanStatus by networkScanner.scanStatus.collectAsState()

            // 显示网络扫描的当前状态
            Text(
                text = "Scan Status: $scanStatus",
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = { networkScanner.startScan() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Start Scan")
            }
        }
    }
}

@Composable
fun WebsocketStatusCard(websocketManager: WebSocketManager) {
    val connectionStatus by websocketManager.connectionStatus.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEDF4F6)
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFFC4EAFD)
            ) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    // 图标根据当前 websocket 连接进行同步更改
                    imageVector = if (connectionStatus.startsWith("Connected")) Icons.Outlined.Check else Icons.Outlined.Info,
                    contentDescription = "Server Status Icon"
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 显示 websocket 当前连接状态
            Text(
                text = connectionStatus,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}