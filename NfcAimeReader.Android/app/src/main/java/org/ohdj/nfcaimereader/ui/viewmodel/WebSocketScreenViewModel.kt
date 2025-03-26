package org.ohdj.nfcaimereader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.data.repository.WebSocketRepository
import org.ohdj.nfcaimereader.model.ConnectionState
import org.ohdj.nfcaimereader.model.WebSocketServerInfo
import javax.inject.Inject

@HiltViewModel
class WebSocketScreenViewModel @Inject constructor(
    private val repository: WebSocketRepository
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionState()
        )

    val savedServers: StateFlow<List<WebSocketServerInfo>> = repository.getSavedServers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResults = MutableStateFlow<List<WebSocketServerInfo>>(emptyList())
    val scanResults: StateFlow<List<WebSocketServerInfo>> = _scanResults.asStateFlow()

    init {
        // 检查是否有自动连接的服务器
        viewModelScope.launch {
            repository.getLastServerInfo().collect { serverInfo ->
                if (serverInfo != null && serverInfo.isAutoConnect) {
                    connectToServer(serverInfo)
                }
            }
        }
    }

    suspend fun getLastServerInfo(): WebSocketServerInfo? {
        return repository.getLastServerInfo().stateIn(viewModelScope).value
    }

    fun connectToServer(serverInfo: WebSocketServerInfo) {
        viewModelScope.launch {
            repository.connectToServer(serverInfo)
        }
    }

    fun disconnect() {
        repository.disconnect()
    }

    suspend fun saveServerInfo(serverInfo: WebSocketServerInfo) {
        repository.saveServerInfo(serverInfo)
    }

    suspend fun deleteServer(serverId: String) {
        repository.deleteServer(serverId)
    }

    suspend fun scanNetwork(port: Int) {
        _isScanning.value = true
        _scanResults.value = emptyList()

        try {
            val results = repository.scanNetwork(port)
            _scanResults.value = results
        } finally {
            _isScanning.value = false
        }
    }
}