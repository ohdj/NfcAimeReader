package org.ohdj.nfcaimereader.ui.navigation3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

/**
 * Type-safe navigation keys for Navigation3.
 * Each destination is a NavKey (data object/data class) and can be saved/restored in the back stack.
 * JavaSerializable is required so the keys can be stored into a Bundle by rememberSaveable.
 */
sealed interface Route : NavKey, JavaSerializable {
    @Serializable
    data object Main : Route

    @Serializable
    data object About : Route

    @Serializable
    data object ColorPalette : Route

    @Serializable
    data object Install : Route
}
