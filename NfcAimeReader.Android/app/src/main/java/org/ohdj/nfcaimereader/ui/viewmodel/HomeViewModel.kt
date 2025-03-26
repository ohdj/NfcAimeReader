package org.ohdj.nfcaimereader.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ohdj.nfcaimereader.utils.NfcManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val nfcManager: NfcManager
) : ViewModel() {
    val cardIdm = nfcManager.cardIdm

    fun isNfcSupported() = nfcManager.isNfcSupported()
}