package org.ohdj.nfcaimereader.ui.screen.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ohdj.nfcaimereader.model.ConnectionState
import org.ohdj.nfcaimereader.model.WebSocketServerInfo
import org.ohdj.nfcaimereader.ui.theme.extendedColorScheme

@Composable
fun WebSocketStatusComponent(
    connectionState: ConnectionState,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        if (connectionState.isConnected) MaterialTheme.extendedColorScheme.successContainer else MaterialTheme.colorScheme.errorContainer
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示
            val icon =
                if (connectionState.isConnected) Icons.Outlined.Check else Icons.Outlined.Close
            Icon(
                imageVector = icon,
                contentDescription = "Websocket Status Icon",
                tint = if (connectionState.isConnected) MaterialTheme.extendedColorScheme.onSuccessContainer else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (connectionState.isConnected) "已连接" else "未连接",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (connectionState.isConnected) MaterialTheme.extendedColorScheme.onSuccessContainer else MaterialTheme.colorScheme.onErrorContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = connectionState.message.ifEmpty {
                        connectionState.serverInfo?.let {
                            "${it.ip}:${it.port}"
                        } ?: "未配置服务器"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                tint = if (connectionState.isConnected) MaterialTheme.extendedColorScheme.onSuccessContainer else MaterialTheme.colorScheme.onErrorContainer,
                contentDescription = "详情"
            )
        }
    }
}

@Preview
@Composable
fun WebSocketStatusConnectedPreview() {
    WebSocketStatusComponent(
        connectionState = ConnectionState(
            isConnected = true,
            serverInfo = WebSocketServerInfo(
                ip = "已连接到 192.168.1.100",
                port = 14514
            )
        ),
        onClick = {}
    )
}

@Preview
@Composable
fun WebSocketStatusDisconnectedPreview() {
    WebSocketStatusComponent(
        connectionState = ConnectionState(
            isConnected = false,
            message = "连接失败: Connection reset"
        ),
        onClick = {}
    )
}

@Preview
@Composable
fun WebSocketStatusNotConfiguredPreview() {
    WebSocketStatusComponent(
        connectionState = ConnectionState(
            isConnected = false,
            message = ""
        ),
        onClick = {}
    )
}