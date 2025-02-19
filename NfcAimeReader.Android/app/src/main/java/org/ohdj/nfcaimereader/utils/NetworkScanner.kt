package org.ohdj.nfcaimereader.utils

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

class NetworkScanner {
    suspend fun scanWebSocketServers(
        context: Context,
        port: Int = 8080,
        timeout: Int = 1000
    ): List<String> = coroutineScope {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val subnet = getSubnet(wifiManager)

        val scanJobs = (1..254).map { host ->
            async(Dispatchers.IO) {
                val ip = "$subnet.$host"
                try {
                    val address = InetAddress.getByName(ip)
                    if (address.isReachable(timeout)) {
                        val url = URL("http://$ip:$port")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = timeout
                        connection.readTimeout = timeout

                        val responseCode = connection.responseCode
                        connection.disconnect()

                        if (responseCode == 101) ip else null
                    } else null
                } catch (e: IOException) {
                    null
                }
            }
        }

        scanJobs.awaitAll().filterNotNull()
    }

    private fun getSubnet(wifiManager: WifiManager): String {
        val ip = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff
        )
    }
}