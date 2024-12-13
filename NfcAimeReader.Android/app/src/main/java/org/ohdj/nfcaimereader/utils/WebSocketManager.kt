package org.ohdj.nfcaimereader.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketManager(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        private val SERVER_ADDRESS_KEY = stringPreferencesKey("server_address")
    }

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    // Save server address to DataStore
    suspend fun saveServerAddress(address: String) {
        context.dataStore.edit { settings ->
            settings[SERVER_ADDRESS_KEY] = address
        }
    }

    // Read saved server address
    fun getServerAddress(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[SERVER_ADDRESS_KEY]
        }

    // Establish WebSocket connection
    fun connect(serverAddress: String, callback: WebSocketCallback) {
        val request = Request.Builder()
            .url("ws://$serverAddress")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                callback.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                callback.onMessageReceived(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                callback.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                callback.onError(t.message ?: "Connection failed")
            }
        })
    }

    // Send card ID via WebSocket
    fun sendCardId(cardId: String) {
        webSocket?.send(cardId)
    }

    // Close WebSocket connection
    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
    }

    // Convert 16-digit hex NFC card ID to 20-digit decimal
    fun convertCardId(hexCardId: String): String {
        // Ensure hex ID is 16 characters long
        val paddedHexId = hexCardId.padStart(16, '0')

        // Convert hex to decimal
        val decimalId = paddedHexId.toLong(16)

        // Format to 20-digit string
        return decimalId.toString().padStart(20, '0')
    }

    // Callback interface for WebSocket events
    interface WebSocketCallback {
        fun onConnected()
        fun onDisconnected()
        fun onMessageReceived(message: String)
        fun onError(errorMessage: String)
    }
}