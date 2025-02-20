package org.ohdj.nfcaimereader.presentation.screen.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.utils.DataStoreManager
import android.content.Context

class SettingViewModel : ViewModel() {
    val autoReconnect = MutableStateFlow(true)

    // 加载自动重连设置
    fun loadAutoReconnect(context: Context) {
        viewModelScope.launch {
            autoReconnect.value = DataStoreManager.getAutoReconnect(context)
        }
    }

    // 更新自动重连设置
    fun setAutoReconnect(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            DataStoreManager.setAutoReconnect(context, enabled)
            autoReconnect.value = enabled
        }
    }
}