package org.ohdj.nfcaimereader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NfcStateBroadcastReceiver {
    private val _nfcEnabled = MutableStateFlow(false)
    val nfcEnabled = _nfcEnabled.asStateFlow()

    private val nfcStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                NfcAdapter.ACTION_ADAPTER_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF
                    )
                    _nfcEnabled.value = state == NfcAdapter.STATE_ON
                }
            }
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        context.registerReceiver(nfcStateReceiver, filter)

        // Initialize the current NFC state
        _nfcEnabled.value = isNfcEnabled(context)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(nfcStateReceiver)
    }

    fun isNfcEnabled(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter?.isEnabled == true
    }
}