package org.ohdj.nfcaimereader.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "server_settings")

object DataStoreManager {
    private val LAST_CONNECTED_IP = stringPreferencesKey("last_connected_ip")
    private val LAST_CONNECTED_PORT = intPreferencesKey("last_connected_port")
    private val AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")
    // 是否使用系统配色
    private val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")

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

    // 设置系统配色选项
    suspend fun setUseSystemTheme(context: Context, useSystem: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USE_SYSTEM_THEME] = useSystem
        }
    }

    // 读取系统配色选项
    suspend fun getUseSystemTheme(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[USE_SYSTEM_THEME] ?: true
    }

    // 通过 Flow 持续读取系统配色选项
    fun getUseSystemThemeFlow(context: Context) =
        context.dataStore.data.map { it[USE_SYSTEM_THEME] ?: true }
}
