package org.ohdj.nfcaimereader.data.websocket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.ohdj.nfcaimereader.model.ConnectionState
import org.ohdj.nfcaimereader.model.WebSocketServerInfo
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    fun connect(serverInfo: WebSocketServerInfo) {
        disconnect()

        val url = "ws://${serverInfo.ip}:${serverInfo.port}"
        val request = Request.Builder()
            .url(url)
            .build()

        _connectionState.value = ConnectionState(
            isConnected = false,
            message = "正在连接到 $url...",
            serverInfo = serverInfo
        )

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState(
                    isConnected = true,
                    message = "已连接到 ${serverInfo.ip}:${serverInfo.port}",
                    serverInfo = serverInfo.copy(isConnected = true)
                )
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState(
                    isConnected = false,
                    message = "连接失败: ${t.message}",
                    serverInfo = serverInfo.copy(isConnected = false)
                )
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState(
                    isConnected = false,
                    message = "连接关闭中: $reason",
                    serverInfo = serverInfo.copy(isConnected = false)
                )
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState(
                    isConnected = false,
                    message = "连接已关闭: $reason",
                    serverInfo = serverInfo.copy(isConnected = false)
                )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val jsonObject = JSONObject(text)
                    // 根据通讯协议，若 status 为 true，则表示刷卡成功
                    val status = jsonObject.optBoolean("status", false)
                    if (status) {
                        _connectionState.value = _connectionState.value.copy(
                            message = "刷卡成功",
                        )
                    } else {
                        _connectionState.value = _connectionState.value.copy(
                            message = "刷卡失败"
                        )
                    }
                } catch (e: Exception) {
                    _connectionState.value = _connectionState.value.copy(
                        message = "响应解析错误: ${e.message}"
                    )
                }
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "正常关闭")
        webSocket = null

        if (_connectionState.value.isConnected) {
            _connectionState.value = ConnectionState(
                isConnected = false,
                message = "已断开连接",
                serverInfo = _connectionState.value.serverInfo?.copy(isConnected = false)
            )
        }
    }

    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }
}