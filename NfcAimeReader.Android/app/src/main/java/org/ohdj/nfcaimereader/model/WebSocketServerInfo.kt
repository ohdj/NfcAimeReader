package org.ohdj.nfcaimereader.model

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketServerInfo(
    val id: String = System.currentTimeMillis().toString(),
    val name: String = "未命名服务器",
    val ip: String,
    val port: Int,
    val isConnected: Boolean = false,
    val isAutoConnect: Boolean = false
)

@Serializable
data class ConnectionState(
    val isConnected: Boolean = false,
    val message: String = "",
    val serverInfo: WebSocketServerInfo? = null
)