package org.ohdj.nfcaimereader.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "server_settings")

object DataStoreManager {
    private val LAST_CONNECTED_IP = stringPreferencesKey("last_connected_ip")
    private val LAST_CONNECTED_PORT = intPreferencesKey("last_connected_port")
    private val AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")

    suspend fun saveLastConnected(context: Context, ip: String, port: Int) {
        context.dataStore.edit { prefs ->
            prefs[LAST_CONNECTED_IP] = ip
            prefs[LAST_CONNECTED_PORT] = port
        }
    }

    suspend fun getLastConnected(context: Context): Pair<String?, Int?> {
        val prefs = context.dataStore.data.first()
        return Pair(prefs[LAST_CONNECTED_IP], prefs[LAST_CONNECTED_PORT])
    }

    suspend fun setAutoReconnect(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_RECONNECT] = enabled
        }
    }

    suspend fun getAutoReconnect(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[AUTO_RECONNECT] ?: true
    }
}
