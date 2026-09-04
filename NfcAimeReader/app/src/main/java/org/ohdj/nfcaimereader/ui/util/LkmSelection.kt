package org.ohdj.nfcaimereader.ui.util

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * LKM (Loadable Kernel Module) selection state for the install screen.
 * Ported from KernelSU (KsuCli.kt); only the UI state is kept, no actual flashing logic.
 */
sealed class LkmSelection : Parcelable {
    @Parcelize
    data class LkmUri(val uri: Uri) : LkmSelection()

    @Parcelize
    data class KmiString(val value: String) : LkmSelection()

    @Parcelize
    data object KmiNone : LkmSelection()
}
