package org.ohdj.nfcaimereader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ohdj.nfcaimereader.data.datastore.WebSocketPreferences
import org.ohdj.nfcaimereader.data.websocket.WebSocketClient
import org.ohdj.nfcaimereader.model.WebSocketServerInfo
import org.ohdj.nfcaimereader.utils.NetworkScanner
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepository @Inject constructor(
    private val preferences: WebSocketPreferences,
    private val webSocketClient: WebSocketClient,
    private val networkScanner: NetworkScanner
) {
    val connectionState = webSocketClient.connectionState

    suspend fun connectToServer(serverInfo: WebSocketServerInfo) {
        webSocketClient.connect(serverInfo)
        preferences.saveServerInfo(serverInfo)
    }

    fun disconnect() {
        webSocketClient.disconnect()
    }

    fun getLastServerInfo(): Flow<WebSocketServerInfo?> {
        return preferences.getLastServerInfo()
    }

    fun getSavedServers(): Flow<List<WebSocketServerInfo>> {
        return preferences.getSavedServers()
    }

    suspend fun saveServerInfo(serverInfo: WebSocketServerInfo) {
        preferences.saveServerInfo(serverInfo)
    }

    suspend fun deleteServer(serverId: String) {
        preferences.deleteServer(serverId)
    }

    suspend fun setAutoConnect(autoConnect: Boolean) {
        preferences.setAutoConnect(autoConnect)
    }

    suspend fun scanNetwork(port: Int): List<WebSocketServerInfo> {
        return networkScanner.scanForWebSocketServers(port)
    }

    fun sendCardId(hexCardId: String): Boolean {
        return webSocketClient.sendCardId(hexCardId)
    }
}