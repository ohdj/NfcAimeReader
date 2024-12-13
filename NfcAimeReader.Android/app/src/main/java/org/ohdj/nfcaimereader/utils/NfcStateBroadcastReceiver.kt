package org.ohdj.nfcaimereader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NfcStateBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private val _nfcState = MutableStateFlow(false)
    }

    val nfcState = _nfcState.asStateFlow()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NfcAdapter.ACTION_ADAPTER_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    NfcAdapter.EXTRA_ADAPTER_STATE,
                    NfcAdapter.STATE_OFF
                )
                _nfcState.value = state == NfcAdapter.STATE_ON
            }
        }
    }

    fun isNfcEnabled(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter?.isEnabled == true
    }
}