package org.ohdj.nfcaimereader.utils

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NfcManager(private val activity: Activity) : NfcAdapter.ReaderCallback {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val _cardIdm = MutableStateFlow<String?>(null)
    val cardIdm = _cardIdm.asStateFlow()

    private val _nfcState = MutableStateFlow(getNfcState())
    val nfcState = _nfcState.asStateFlow()

    fun refreshNfcState() {
        _nfcState.value = getNfcState()
    }

    private fun getNfcState(): NfcState {
        return when {
            nfcAdapter == null -> NfcState.UNSUPPORTED
            nfcAdapter.isEnabled -> NfcState.ENABLED
            else -> NfcState.DISABLED
        }
    }

    fun enableNfcReaderMode() {
        nfcAdapter?.enableReaderMode(activity, this, NfcAdapter.FLAG_READER_NFC_F, null)
    }

    fun disableNfcReaderMode() {
        nfcAdapter?.disableReaderMode(activity)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val idm = tag?.id?.joinToString("") { String.format("%02X", it) }
        _cardIdm.value = idm ?: "No IDm found"
    }

    fun isNfcSupported(): Boolean = nfcAdapter != null
}