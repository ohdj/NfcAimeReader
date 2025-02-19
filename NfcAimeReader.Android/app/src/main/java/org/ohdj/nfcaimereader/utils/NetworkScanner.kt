package org.ohdj.nfcaimereader.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class NetworkScanner(context: Context) {

    private val appContext = context.applicationContext

    private val _scanStatus = MutableStateFlow("空闲")
    val scanStatus: StateFlow<String> = _scanStatus

    private val websocketManager = WebSocketManager()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startScan() {
        scope.launch {
            _scanStatus.value = "开始扫描..."
            val lastConnectedIp = DataStoreManager.getLastConnectedIp(appContext)
            val lastConnectedPort = DataStoreManager.getLastConnectedPort(appContext)
            // 尝试使用上次保存的连接信息
            if (lastConnectedIp != null && lastConnectedPort != null) {
                val connected = tryConnect(lastConnectedIp, lastConnectedPort)
                if (!connected) {
                    performFullScan()
                }
            } else {
                performFullScan()
            }
        }
    }

    private suspend fun tryConnect(ip: String, port: Int): Boolean {
        for (i in port..14517) {
            try {
                websocketManager.connect(ip, i)
                delay(1000) // 等待连接建立
                val currentStatus = websocketManager.connectionStatus.value
                if (currentStatus.startsWith("Connected")) {
                    DataStoreManager.saveConnection(appContext, ip, i)
                    _scanStatus.value = "连接成功：$ip:$i"
                    return true
                } else {
                    _scanStatus.value = "未连接：$ip:$i，状态: $currentStatus"
                }
            } catch (e: Exception) {
                _scanStatus.value = "尝试连接 $ip:$i 时异常：${e.message}"
                delay(500)
            }
        }
        return false
    }

    private suspend fun performFullScan() {
        val localIp = getLocalIpAddress()
        if (localIp == null) {
            _scanStatus.value = "无法获取本机IP地址"
            return
        }
        // 更新状态提示全网扫描正在进行
        _scanStatus.value = "全网扫描中..."
        val ipBase = localIp.substringBeforeLast('.')
        val jobs = mutableListOf<Job>()

        // 遍历 1~254 段IP，每个 IP 尝试 4 个端口
        for (i in 1..254) {
            val ip = "$ipBase.$i"
            for (port in 14514..14517) {
                jobs.add(scope.launch {
                    tryConnect(ip, port)
                })
            }
        }
        jobs.joinAll()
        _scanStatus.value = "扫描完成"
    }

    private fun getLocalIpAddress(): String? {
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val intf = interfaces.nextElement()
            val addrs = intf.inetAddresses
            while (addrs.hasMoreElements()) {
                val addr = addrs.nextElement()
                if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                    return addr.hostAddress
                }
            }
        }
        return null
    }

    fun stopScan() {
        scope.cancel()
    }
}