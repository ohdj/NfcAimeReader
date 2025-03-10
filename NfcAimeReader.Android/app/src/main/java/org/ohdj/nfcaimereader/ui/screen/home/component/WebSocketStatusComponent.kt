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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ohdj.nfcaimereader.model.ConnectionState
import org.ohdj.nfcaimereader.ui.navigation.Screen

@Composable
fun WebSocketStatusComponent(
    connectionState: ConnectionState,
    navController: NavController,
) {
    val backgroundColor by animateColorAsState(
        if (connectionState.isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { navController.navigate(Screen.Config.route) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示
            val icon =
                if (connectionState.isConnected) Icons.Outlined.Check else Icons.Outlined.Close
            Icon(
                imageVector = icon,
                contentDescription = "Websocket Status Icon",
                tint = if (connectionState.isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (connectionState.isConnected) "已连接" else "未连接",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = connectionState.message.ifEmpty {
                        connectionState.serverInfo?.let {
                            "${it.ip}:${it.port}"
                        } ?: "未配置服务器"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "详情"
            )
        }
    }
}