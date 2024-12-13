package org.ohdj.nfcaimereader.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL

class NetworkScanner {
    private val _servers = MutableStateFlow<List<String>>(emptyList())
    val servers = _servers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanLogs = MutableStateFlow<List<String>>(emptyList())
    val scanLogs = _scanLogs.asStateFlow()

    suspend fun scanLocalNetwork(timeoutSeconds: Int = 30) {
        _isScanning.value = true
        _scanLogs.value = emptyList()
        val baseIp = getLocalIpAddress()
        val foundServers = mutableListOf<String>()
        val ports = listOf(8080)

        withContext(Dispatchers.Default) {
            val jobs = mutableListOf<Job>()
            for (i in 1..254) {
                val ip = "$baseIp.$i"
                jobs += launch {
                    for (port in ports) {
                        if (isServerRunning(ip, port)) {
                            val server = "http://$ip:$port"
                            foundServers.add(server)
                            addLog("Server found: $server")
                        }
                    }
                }
            }

            val timeoutJob = launch {
                delay(timeoutSeconds * 1000L)
                jobs.forEach { it.cancel() }
            }

            jobs.forEach { it.join() }
            timeoutJob.cancel()
        }

        _servers.value = foundServers
        _isScanning.value = false
        addLog("Scan completed. Found ${foundServers.size} servers.")
    }

    private suspend fun getLocalIpAddress(): String = withContext(Dispatchers.IO) {
        NetworkInterface.getNetworkInterfaces().asSequence().flatMap { it ->
            it.inetAddresses.asSequence()
                .filter { it.isSiteLocalAddress && !it.isLoopbackAddress && it.address.size == 4 }
        }.firstOrNull()?.hostAddress?.substringBeforeLast(".") ?: "192.168.1"
    }

    private suspend fun isServerRunning(ip: String, port: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("http://$ip:$port/cardnumber")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 500
                connection.readTimeout = 500
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode
                responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_BAD_REQUEST
            } catch (e: Exception) {
                false
            }
        }

    private fun addLog(message: String) {
        _scanLogs.value += message
    }
}