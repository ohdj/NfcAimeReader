package org.ohdj.nfcaimereader.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

class NetworkScanner {
    private val scanScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var found = false

    // 扫描局域网内的websocket服务器，优先尝试DataStore中保存的服务器
    fun startScan(context: Context, webSocketManager: WebSocketManager) {
        scanScope.launch {
            // 1. 尝试上次成功的IP和端口
            val (lastIp, lastPort) = DataStoreManager.getLastConnected(context)
            if (lastIp != null && lastPort != null) {
                for (port in lastPort..(lastPort)) {
                    if (webSocketManager.connectToServer(lastIp, port)) {
                        DataStoreManager.saveLastConnected(context, lastIp, port)
                        found = true
                        return@launch
                    } else {
                        // 在该IP尝试 14514-14517
                        for (p in 14514..14517) {
                            if (webSocketManager.connectToServer(lastIp, p)) {
                                DataStoreManager.saveLastConnected(context, lastIp, p)
                                found = true
                                return@launch
                            }
                        }
                    }
                }
            }

            // 2. 未找到，扫描同一网段（简单获取当前设备IP的前三段，剩余段遍历1-254）
            val localIp = getLocalIPAddress() ?: return@launch
            val ipParts = localIp.split(".")
            if (ipParts.size != 4) return@launch
            val subnet = "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}."

            val jobs = mutableListOf<Job>()
            for (i in 1..254) {
                if (found) break
                val ip = "$subnet$i"
                for (port in 14514..14517) {
                    if (found) break
                    jobs += launch {
                        if (!found && webSocketManager.connectToServer(ip, port)) {
                            withContext(NonCancellable) {
                                DataStoreManager.saveLastConnected(context, ip, port)
                            }
                            found = true
                            // 取消其他扫描任务
                            scanScope.coroutineContext.cancelChildren()
                        }
                    }
                }
            }
            jobs.joinAll()
        }
    }

    // 获取局域网IP
    private fun getLocalIPAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addresses = intf.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}