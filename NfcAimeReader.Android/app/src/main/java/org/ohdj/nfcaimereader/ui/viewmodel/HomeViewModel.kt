package org.ohdj.nfcaimereader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.data.repository.WebSocketRepository
import org.ohdj.nfcaimereader.utils.NfcManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val nfcManager: NfcManager,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {
    init {
        // 监听NFC卡号变化并发送
        viewModelScope.launch {
            nfcManager.cardIdm.collect { cardId ->
                if (!cardId.isNullOrEmpty() && webSocketRepository.connectionState.value.isConnected) {
                    webSocketRepository.sendCardId(cardId)
                }
            }
        }
    }
}