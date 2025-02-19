package org.ohdj.nfcaimereader.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DataStoreManager {
    private val IP_KEY = stringPreferencesKey("last_connected_ip")
    private val PORT_KEY = stringPreferencesKey("last_connected_port")
    private val AUTO_RECONNECT_KEY = stringPreferencesKey("auto_reconnect")

    fun saveConnection(context: Context, ip: String, port: Int) {
        runBlocking {
            context.dataStore.edit { settings ->
                settings[IP_KEY] = ip
                settings[PORT_KEY] = port.toString()
            }
        }
    }

    fun getLastConnectedIp(context: Context): String? {
        return runBlocking {
            context.dataStore.data.first()[IP_KEY]
        }
    }

    fun getLastConnectedPort(context: Context): Int? {
        return runBlocking {
            context.dataStore.data.first()[PORT_KEY]?.toInt()
        }
    }

    fun saveAutoReconnect(context: Context, flag: Boolean) {
        runBlocking {
            context.dataStore.edit { settings ->
                settings[AUTO_RECONNECT_KEY] = flag.toString()
            }
        }
    }

    fun getAutoReconnect(context: Context): Boolean {
        return runBlocking {
            context.dataStore.data.first()[AUTO_RECONNECT_KEY]?.toBoolean() ?: false
        }
    }
}