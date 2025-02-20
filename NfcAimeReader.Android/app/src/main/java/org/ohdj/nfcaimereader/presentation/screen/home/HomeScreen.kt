package org.ohdj.nfcaimereader.presentation.screen.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.NfcStateBroadcastReceiver
import org.ohdj.nfcaimereader.utils.NetworkScanner
import org.ohdj.nfcaimereader.utils.WebSocketManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nfcManager: NfcManager, webSocketManager: WebSocketManager) {
    val context = LocalContext.current
    var isScaning by remember { mutableStateOf(false) }
    val networkScanner = remember { NetworkScanner() }

    // NFC状态与读卡相关
    val cardIdm by nfcManager.cardIdm.collectAsState()
    val nfcStateReceiver = remember { NfcStateBroadcastReceiver() }

//    var isNfcEnabled by remember { mutableStateOf(true) }
    val isNfcEnabled by nfcStateReceiver.nfcState.collectAsState(
        initial = nfcStateReceiver.isNfcEnabled(context)
    )

    // 动画过渡的卡片背景颜色 (启用：primaryContainer，禁用：errorContainer)
    val animatedCardColor by animateColorAsState(
        targetValue = if (isNfcEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    )
    // 动画过渡的按钮颜色 (启用：primary，禁用：error)
    val animatedButtonColor by animateColorAsState(
        targetValue = if (isNfcEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("NfcAimeReader")
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isScaning,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    text = { Text("扫描服务器") },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Scan Server") },
                    onClick = {
                        isScaning = true
                        networkScanner.startScan(context, webSocketManager)
                    }
                )
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            // 服务器状态
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                onClick = { isScaning = !isScaning }
            ) {
                AnimatedContent(
                    targetState = isScaning,
                    transitionSpec = {
                        if (targetState) {
                            // 开始扫描：旧内容向上滑出，新内容从下向上滑入
                            (slideInVertically(initialOffsetY = { height -> height }) + fadeIn()).togetherWith(
                                slideOutVertically(targetOffsetY = { height -> -height }) + fadeOut()
                            )
                        } else {
                            // 结束扫描：旧内容向下滑出，新内容从上向下滑入
                            (slideInVertically(initialOffsetY = { height -> -height }) + fadeIn()).togetherWith(
                                slideOutVertically(targetOffsetY = { height -> height }) + fadeOut()
                            )
                        }.using(SizeTransform(clip = false))
                    }
                ) { targetIsScaning ->
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!targetIsScaning) {
                            Icon(
                                modifier = Modifier.padding(8.dp),
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Server Status Icon",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .width(24.dp)
                                    .height(24.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                trackColor = MaterialTheme.colorScheme.surfaceDim,
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = if (!targetIsScaning) "未配置服务器" else "正在扫描服务器",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val nfcIcon =
                            if (isNfcEnabled) Icons.Outlined.Check else Icons.Outlined.Close
                        Icon(
                            imageVector = nfcIcon,
                            contentDescription = "NFC 状态图标",
                            tint = if (isNfcEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = if (isNfcEnabled) "NFC 已启用" else "NFC 被禁用",
                            color = if (isNfcEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 16.sp
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
                                text = "Last Card ID: ${cardIdm ?: ""}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isNfcEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // NFC 被禁用时显示 “去启用 NFC” 按钮
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
                                Text("去启用 NFC")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "向右箭头",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}