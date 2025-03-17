package org.ohdj.nfcaimereader.ui.screen.home.component

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ohdj.nfcaimereader.utils.NfcState

@Composable
fun NfcStatusComponent(nfcState: NfcState, cardIdm: String?) {
    val context = LocalContext.current

    // 动画过渡的卡片背景颜色
    val animatedCardColor by animateColorAsState(
        targetValue = when (nfcState) {
            NfcState.ENABLED -> MaterialTheme.colorScheme.primaryContainer
            NfcState.DISABLED -> MaterialTheme.colorScheme.errorContainer
            NfcState.UNSUPPORTED -> MaterialTheme.colorScheme.surfaceVariant
        }
    )

    // 动画过渡的按钮颜色
    val animatedButtonColor by animateColorAsState(
        targetValue = when (nfcState) {
            NfcState.ENABLED -> MaterialTheme.colorScheme.primary
            NfcState.DISABLED -> MaterialTheme.colorScheme.error
            NfcState.UNSUPPORTED -> MaterialTheme.colorScheme.outline
        }
    )

    // NFC 状态卡片
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = animatedCardColor),
        onClick = { }
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 根据NFC状态显示不同图标
                val (icon, iconTint) = when (nfcState) {
                    NfcState.ENABLED -> Icons.Outlined.Check to MaterialTheme.colorScheme.onPrimaryContainer
                    NfcState.DISABLED -> Icons.Outlined.Close to MaterialTheme.colorScheme.onErrorContainer
                    NfcState.UNSUPPORTED -> Icons.Outlined.Warning to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "NFC Status Icon",
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 根据NFC状态显示不同文本
                val (statusText, textColor) = when (nfcState) {
                    NfcState.ENABLED -> "NFC 已启用" to MaterialTheme.colorScheme.onPrimaryContainer
                    NfcState.DISABLED -> "NFC 已禁用" to MaterialTheme.colorScheme.onErrorContainer
                    NfcState.UNSUPPORTED -> "设备不支持 NFC" to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Text(
                    text = statusText,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 读卡记录显示 - 仅在NFC启用时显示
            AnimatedVisibility(
                visible = nfcState == NfcState.ENABLED && cardIdm != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "卡号: ${cardIdm ?: ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // NFC 已禁用时显示 "去启用 NFC" 按钮
            AnimatedVisibility(
                visible = nfcState == NfcState.DISABLED,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = animatedButtonColor)
                    ) {
                        Text(
                            text = "去启用 NFC",
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "ArrowForward",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }

            // 设备不支持NFC时的提示信息
            AnimatedVisibility(
                visible = nfcState == NfcState.UNSUPPORTED,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "当前无法使用读卡功能",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NfcStatusEnabledPreview() {
    NfcStatusComponent(
        nfcState = NfcState.ENABLED,
        cardIdm = "0123456789ABCDEF"
    )
}

@Preview
@Composable
fun NfcStatusDisabledPreview() {
    NfcStatusComponent(
        nfcState = NfcState.DISABLED,
        cardIdm = null
    )
}

@Preview
@Composable
fun NfcStatusUnsupportedPreview() {
    NfcStatusComponent(
        nfcState = NfcState.UNSUPPORTED,
        cardIdm = null
    )
}