package org.ohdj.nfcaimereader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.data.repository.WebSocketRepository
import org.ohdj.nfcaimereader.utils.NfcManager
import org.ohdj.nfcaimereader.utils.NfcState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val nfcManager: NfcManager,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    // 向UI公开NFC状态和卡片ID
    val nfcState: StateFlow<NfcState> = nfcManager.nfcState
    val cardIdm: StateFlow<String?> = nfcManager.cardIdm

    init {
        viewModelScope.launch {
            nfcManager.cardIdm
                .filterNotNull() // 仅在有实际卡片ID可用时处理
                .collect { hexCardId ->
                    // 假设 webSocketRepository.connectionState 是一个 Flow<Boolean> 或类似结构
                    // 并且 webSocketRepository.sendCardId 期望特定格式（例如十进制）
                    // val isConnected = webSocketRepository.connectionState.value.isConnected // 示例
                    // 目前，我们假设sendCardId处理连接检查，或者如果应用逻辑允许，我们无条件发送

                    // 如果需要先转换为十进制再发送：
                    // val decimalCardId = BigInteger(hexCardId, 16).toString().padStart(20, '0')
                    // webSocketRepository.sendCardId(decimalCardId)

                    // 如果直接发送十六进制ID：
                    if (webSocketRepository.connectionState.value.isConnected) {
                        webSocketRepository.sendCardId(hexCardId)
                    }
                }
        }
    }
}