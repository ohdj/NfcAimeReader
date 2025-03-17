package org.ohdj.nfcaimereader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NfcStateBroadcastReceiver {
    private val _nfcState = MutableStateFlow<NfcState>(NfcState.DISABLED)
    val nfcState = _nfcState.asStateFlow()

    private val nfcStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                NfcAdapter.ACTION_ADAPTER_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF
                    )
                    _nfcState.value = when (state) {
                        NfcAdapter.STATE_ON -> NfcState.ENABLED
                        else -> NfcState.DISABLED
                    }
                }
            }
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)
        context.registerReceiver(nfcStateReceiver, filter)

        // Initialize the current NFC state
        _nfcState.value = getNfcState(context)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(nfcStateReceiver)
    }

    fun getNfcState(context: Context): NfcState {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return when {
            nfcAdapter == null -> NfcState.UNSUPPORTED
            nfcAdapter.isEnabled -> NfcState.ENABLED
            else -> NfcState.DISABLED
        }
    }
}