package org.ohdj.nfcaimereader.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * NFC状态管理器
 *
 * NfcManager（负责设备支持检测和读卡操作）
 *
 * NfcStateBroadcastReceiver（负责监听NFC启用/禁用状态）
 *
 * 最终确定NFC状态（启用/禁用/不支持）
 */
class NfcStateManager(
    private val nfcManager: NfcManager,
    private val nfcStateReceiver: NfcStateBroadcastReceiver
) {

    /**
     * 获取当前NFC状态的Flow
     */
    fun getNfcStateFlow(context: Context): Flow<NfcState> {
        return nfcStateReceiver.nfcEnabled.combine(
            kotlinx.coroutines.flow.flowOf(nfcManager.isNfcSupported())
        ) { isEnabled, isSupported ->
            when {
                !isSupported -> NfcState.UNSUPPORTED
                isEnabled -> NfcState.ENABLED
                else -> NfcState.DISABLED
            }
        }
    }

    /**
     * 获取当前NFC状态
     */
    fun getCurrentNfcState(context: Context): NfcState {
        val isSupported = nfcManager.isNfcSupported()
        val isEnabled = if (isSupported) nfcStateReceiver.isNfcEnabled(context) else false

        return when {
            !isSupported -> NfcState.UNSUPPORTED
            isEnabled -> NfcState.ENABLED
            else -> NfcState.DISABLED
        }
    }
}

/**
 * NfcStateManager的扩展函数
 *
 * 创建并记忆NfcStateManager实例
 */
@Composable
fun rememberNfcStateManager(
    nfcManager: NfcManager,
    nfcStateReceiver: NfcStateBroadcastReceiver = remember { NfcStateBroadcastReceiver() }
): NfcStateManager {
    return remember { NfcStateManager(nfcManager, nfcStateReceiver) }
}

/**
 * 获取当前NFC状态
 *
 * 将NFC状态作为Compose状态收集
 */
@Composable
fun NfcStateManager.collectNfcStateAsState(context: Context): NfcState {
    val nfcStateFlow = getNfcStateFlow(context)
    val nfcState by nfcStateFlow.collectAsState(initial = getCurrentNfcState(context))
    return nfcState
}