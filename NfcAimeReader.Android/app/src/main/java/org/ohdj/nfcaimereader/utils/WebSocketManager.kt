package org.ohdj.nfcaimereader.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class WebSocketManager {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus = _connectionStatus.asStateFlow()

    private var webSocket: WebSocket? = null

    // 建立WebSocket连接，返回连接是否成功
    suspend fun connectToServer(ip: String, port: Int): Boolean =
        suspendCancellableCoroutine { cont ->
            val request = Request.Builder().url("ws://$ip:$port").build()
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _connectionStatus.value = true
                    if (!cont.isCompleted) cont.resume(true)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _connectionStatus.value = false
                    if (!cont.isCompleted) cont.resume(false)
                }
            })
        }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        _connectionStatus.value = false
    }
}