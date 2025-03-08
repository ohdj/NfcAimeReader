package org.ohdj.nfcaimereader.ui.screen.websocket

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.model.WebSocketServerInfo
import org.ohdj.nfcaimereader.ui.viewmodel.WebSocketViewModel

@Composable
fun WebSocketDetailScreen(
    viewModel: WebSocketViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val savedServers by viewModel.savedServers.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanResults by viewModel.scanResults.collectAsState()

    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("14514") }
    var autoConnect by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // 加载上次连接的服务器信息
    LaunchedEffect(Unit) {
        viewModel.getLastServerInfo()?.let { serverInfo ->
            ipAddress = serverInfo.ip
            port = serverInfo.port.toString()
            autoConnect = serverInfo.isAutoConnect
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 连接状态卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (connectionState.isConnected) {
                        Color(0xFFE8F5E9)
                    } else {
                        Color(0xFFFBE9E7)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (connectionState.isConnected) "已连接" else "未连接",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = connectionState.message.ifEmpty {
                            connectionState.serverInfo?.let {
                                "${it.ip}:${it.port}"
                            } ?: "未配置服务器"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (connectionState.isConnected) {
                            Button(
                                onClick = { viewModel.disconnect() }
                            ) {
                                Text("断开连接")
                            }
                        }
                    }
                }
            }
        }

        item {
            // 手动配置表单
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "手动配置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("服务器 IP 地址") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("端口") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "启动时自动连接",
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = autoConnect,
                            onCheckedChange = { autoConnect = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                val portNumber = port.toIntOrNull() ?: 8080
                                val serverInfo = WebSocketServerInfo(
                                    ip = ipAddress,
                                    port = portNumber,
                                    isAutoConnect = autoConnect
                                )

                                coroutineScope.launch {
                                    viewModel.saveServerInfo(serverInfo)
                                    viewModel.connectToServer(serverInfo)
                                }
                            },
                            enabled = ipAddress.isNotEmpty() && port.isNotEmpty()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text("保存并连接")
                            }
                        }
                    }
                }
            }
        }

        item {
            // 局域网扫描
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "局域网扫描",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("扫描端口") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = isScanning,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Text(
                                text = "正在扫描局域网中的 WebSocket 服务器...",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Button(
                        onClick = {
                            val portNumber = port.toIntOrNull() ?: 8080
                            coroutineScope.launch {
                                viewModel.scanNetwork(portNumber)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isScanning && port.isNotEmpty()
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text("开始扫描")
                            }
                        }
                    }
                }
            }
        }

        // 扫描结果
        if (scanResults.isNotEmpty()) {
            item {
                Text(
                    text = "扫描结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(scanResults) { serverInfo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = serverInfo.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "${serverInfo.ip}:${serverInfo.port}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.saveServerInfo(serverInfo)
                                    viewModel.connectToServer(serverInfo)
                                }
                            }
                        ) {
                            Text("连接")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 已保存的服务器
        if (savedServers.isNotEmpty()) {
            item {
                Text(
                    text = "已保存的服务器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(savedServers) { serverInfo ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = serverInfo.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "${serverInfo.ip}:${serverInfo.port}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (serverInfo.isAutoConnect) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "自动连接",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.deleteServer(serverInfo.id)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.connectToServer(serverInfo)
                                    }
                                }
                            ) {
                                Text("连接")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 底部空间
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}