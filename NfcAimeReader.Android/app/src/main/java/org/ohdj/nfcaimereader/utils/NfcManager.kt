package org.ohdj.nfcaimereader.utils

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcManager @Inject constructor(
    @ApplicationContext private val context: Context
) : NfcAdapter.ReaderCallback {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    private val _cardIdm = MutableStateFlow<String?>(null)
    val cardIdm = _cardIdm.asStateFlow()

    fun enableNfcReaderMode(activity: Activity) {
        nfcAdapter?.enableReaderMode(activity, this, NfcAdapter.FLAG_READER_NFC_F, null)
    }

    fun disableNfcReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val idm = tag?.id?.joinToString("") { String.format("%02X", it) }
        _cardIdm.value = idm
    }

    fun isNfcSupported(): Boolean = nfcAdapter != null
}