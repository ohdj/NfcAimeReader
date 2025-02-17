package org.ohdj.nfcaimereader.utils

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * 如果 HomeScreen 中通过
 *
 * `val nfcManager = remember { NfcManager(context) }`
 *
 * 生成了一个新的 NFC 管理器实例，这个实例与 MainActivity 中实际启用了 NFC 读卡模式的实例并不是同一个
 *
 * MainActivity 中调用了 enableNfcReaderMode() 后，只有该实例能收到 NFC 的回调并更新内部流
 *
 * 而 HomeScreen 中新创建的实例没有启用读卡功能
 *
 * 所以，为了确保 NFC 的读卡状态能正确同步到界面上，需要保证所有地方使用同一个 NFC 管理器实例
 */
class NfcManager private constructor(activity: Activity) : NfcAdapter.ReaderCallback {
    /**
     * 单例模式中，静态字段（如 INSTANCE）持有 NfcManager 实例，而该实例中保存了 Context 对象
     *
     * 如果传入的是 Activity 或 Fragment 的 Context，就会造成内存泄漏，因为静态字段的生命周期很长，会持有 Activity/Fragment 的引用，使其无法被回收
     *
     * 因此这里使用了弱引用
     */
    // 用弱引用保存 Activity 避免内存泄漏
    private val activityRef = WeakReference(activity)
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val _cardIdm = MutableStateFlow<String?>(null)
    val cardIdm = _cardIdm.asStateFlow()

    fun enableNfcReaderMode() {
        activityRef.get()?.let { activity ->
            nfcAdapter?.enableReaderMode(
                activity,
                this,
                NfcAdapter.FLAG_READER_NFC_F,
                null
            )
        }
    }

    fun disableNfcReaderMode() {
        activityRef.get()?.let { activity ->
            nfcAdapter?.disableReaderMode(activity)
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        val idm = tag?.id?.joinToString("") { String.format("%02X", it) }
        _cardIdm.value = idm ?: "No IDm found"
    }

    companion object {
        @Volatile
        private var INSTANCE: NfcManager? = null

        fun getInstance(activity: Activity): NfcManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NfcManager(activity).also { INSTANCE = it }
            }
        }
    }
}