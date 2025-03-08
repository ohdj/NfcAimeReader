package org.ohdj.nfcaimereader.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.ohdj.nfcaimereader.model.WebSocketServerInfo
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkScanner @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .build()

    suspend fun scanForWebSocketServers(port: Int): List<WebSocketServerInfo> = coroutineScope {
        val ipPrefix = getLocalIpPrefix()
        if (ipPrefix.isEmpty()) return@coroutineScope emptyList()

        val serverList = mutableListOf<WebSocketServerInfo>()

        withContext(Dispatchers.IO) {
            val scanTasks = (1..254).map { ipSuffix ->
                async {
                    val ip = "$ipPrefix.$ipSuffix"
                    val isReachable = isWebSocketServerReachable(ip, port)
                    if (isReachable) {
                        WebSocketServerInfo(
                            name = "发现的服务器 $ip:$port",
                            ip = ip,
                            port = port
                        )
                    } else {
                        null
                    }
                }
            }

            scanTasks.awaitAll().filterNotNull().toCollection(serverList)
        }

        serverList
    }

    private fun getLocalIpPrefix(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address.isSiteLocalAddress) {
                        val hostAddress = address.hostAddress
                        val lastDotIndex = hostAddress.lastIndexOf('.')
                        if (lastDotIndex > 0) {
                            return hostAddress.substring(0, lastDotIndex)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    private fun isWebSocketServerReachable(ip: String, port: Int): Boolean {
        try {
            // 首先检查IP是否可达
            if (!InetAddress.getByName(ip).isReachable(500)) {
                return false
            }

            // 然后尝试建立WebSocket连接
            val latch = CountDownLatch(1)
            var isReachable = false

            val url = "ws://$ip:$port"
            val request = Request.Builder().url(url).build()
            val webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    isReachable = true
                    webSocket.close(1000, "Scan complete")
                    latch.countDown()
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: okhttp3.Response?
                ) {
                    latch.countDown()
                }
            })

            // 等待1秒
            latch.await(1, TimeUnit.SECONDS)
            webSocket.close(1000, "Scan timeout")

            return isReachable
        } catch (e: Exception) {
            return false
        }
    }
}