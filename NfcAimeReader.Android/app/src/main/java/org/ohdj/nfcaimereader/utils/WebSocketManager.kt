package org.ohdj.nfcaimereader.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketManager private constructor() {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 连接状态供UI观察
    private val _connectionStatus = MutableStateFlow("未连接服务器")
    val connectionStatus = _connectionStatus.asStateFlow()

    // 缓存上次成功的服务器地址，例如 "192.168.0.1:14514"
    var lastServerAddress: String? = null
        private set

    fun connect(serverAddress: String) {
        _connectionStatus.value = "连接中..."
        val wsUrl = "ws://$serverAddress"
        // 捕获 serverAddress 供后续使用
        val targetServer = serverAddress
        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("X-Custom-Scanner", "AndroidClientV1")
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 无法从响应头获取验证信息，等待服务器发送验证消息
                _connectionStatus.value = "等待服务器验证..."
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                // 处理验证消息
                if (text.startsWith("X-Server-Verification:")) {
                    val verification = text.substringAfter(":").trim()
                    if (verification == "MyServerV1") {
                        _connectionStatus.value = "已连接服务器"
                        lastServerAddress = targetServer
                        startHeartbeat()
                    } else {
                        _connectionStatus.value = "握手验证失败"
                        webSocket.close(1000, "验证失败")
                    }
                } else if (text == "HEARTBEAT_ACK") {
                    // 处理心跳回复
                }
            }
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // ...existing code...
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.value = "连接失败"
                stopHeartbeat()
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStatus.value = "连接关闭"
                stopHeartbeat()
            }
        })
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(30_000)
                webSocket?.send("HEARTBEAT")
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
    }

    companion object {
        @Volatile
        private var instance: WebSocketManager? = null
        fun getInstance(): WebSocketManager {
            return instance ?: synchronized(this) {
                instance ?: WebSocketManager().also { instance = it }
            }
        }
    }
}