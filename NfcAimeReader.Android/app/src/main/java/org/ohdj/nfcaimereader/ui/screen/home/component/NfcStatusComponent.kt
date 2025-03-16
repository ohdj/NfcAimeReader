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
import androidx.compose.ui.unit.sp

@Composable
fun NfcStatusComponent(isNfcEnabled: Boolean, cardIdm: String?) {
    val context = LocalContext.current

    // 动画过渡的卡片背景颜色 (启用：primaryContainer，禁用：errorContainer)
    val animatedCardColor by animateColorAsState(
        targetValue = if (isNfcEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    )
    // 动画过渡的按钮颜色 (启用：primary，禁用：error)
    val animatedButtonColor by animateColorAsState(
        targetValue = if (isNfcEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )

    // NFC 状态
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
            Row {
                val nfcIcon =
                    if (isNfcEnabled) Icons.Outlined.Check else Icons.Outlined.Close
                Icon(
                    imageVector = nfcIcon,
                    contentDescription = "NFC Status Icon",
                    tint = if (isNfcEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = if (isNfcEnabled) "NFC 已启用" else "NFC 已禁用",
                    color = if (isNfcEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 读卡记录显示
            AnimatedVisibility(
                visible = cardIdm != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "卡号: ${cardIdm ?: ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isNfcEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // NFC 已禁用时显示 “去启用 NFC” 按钮
            AnimatedVisibility(
                visible = !isNfcEnabled,
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
        }
    }
}

@Preview
@Composable
fun NfcStatusEnabledPreview() {
    NfcStatusComponent(
        isNfcEnabled = true,
        cardIdm = "0123456789ABCDEF"
    )
}

@Preview
@Composable
fun NfcStatusDisabledPreview() {
    NfcStatusComponent(
        isNfcEnabled = false,
        cardIdm = null
    )
}