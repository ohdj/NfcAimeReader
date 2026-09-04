package org.ohdj.nfcaimereader.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.ohdj.nfcaimereader.KernelVersion
import org.ohdj.nfcaimereader.nfcApp
import org.ohdj.nfcaimereader.ui.screen.home.HomeUiState
import org.ohdj.nfcaimereader.ui.screen.home.SystemInfo
import org.ohdj.nfcaimereader.ui.screen.home.getManagerVersion
import org.ohdj.nfcaimereader.ui.util.module.LatestVersionInfo

/**
 * Mock HomeViewModel.
 *
 * The original KernelSU HomeViewModel queries the kernel driver via native calls.
 * This project is not a root manager, so all values below are static mock data
 * that only drives the UI. Adjust them to preview different status card styles.
 *
 * "工作中"状态卡片样式切换参数说明（见 HomeMaterial.kt / HomeMiuix.kt 的 StatusCard）：
 *
 * ┌────────────────────────────┬───────────────────────────────────────────────────┐
 * │ 参数                        │ 对应样式                                            │
 * ├────────────────────────────┼───────────────────────────────────────────────────┤
 * │ ksuVersion != null         │ "工作中"：绿色卡片 + CheckCircle 图标 + 版本号摘要      │
 * │ ksuVersion == null         │ 且 kernelVersion.isGKI() → "未安装"：               │
 * │                            │   错误色卡片 + Warning 图标 + "点击安装"              │
 * │ ksuVersion == null         │ 且 !isGKI() → "不支持"：Block 图标 + 原因说明          │
 * │ lkmMode == true            │ 尾部标签 "LKM"                                      │
 * │ lkmMode == false           │ 尾部标签 "GKI"                                      │
 * │ lkmMode == null            │ 不显示模式标签                                        │
 * │ isSafeMode == true         │ 标题旁追加"安全模式"标签（仅工作中状态）                  │
 * │ isLateLoadMode == true     │ 标题旁追加"越狱模式"标签，且卡片不可点击（不跳转安装页）     │
 * │ isSELinuxPermissive        │ (systemInfo.selinuxStatus == "Permissive")          │
 * │                            │ 未安装状态下额外显示"越狱"按钮                          │
 * └────────────────────────────┴───────────────────────────────────────────────────┘
 */
class HomeViewModel : ViewModel() {

    private val managerVersion = getManagerVersion(nfcApp)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            // ---- 状态卡片核心参数（见上方表格注释）----
            kernelVersion = KernelVersion(6, 1, 0), // 内核版本；isGKI() 由主/次版本号推导
            ksuVersion = 30000,                     // 非 null → 显示"工作中"；null → 未安装/不支持
            lkmMode = true,                         // true → "LKM" 标签；false → "GKI"；null → 无标签
            isSafeMode = false,                     // true → "安全模式"标签
            isLateLoadMode = false,                 // true → "越狱模式"标签且卡片不可点击

            // ---- 警告卡片参数（mock 全部置为不显示）----
            isManager = true,
            isManagerPrBuild = false,
            isKernelPrBuild = false,
            requiresNewKernel = false,
            uapiMismatch = false,
            isRootAvailable = true,

            // ---- 更新卡片参数（checkUpdateEnabled=false → 不显示更新卡片）----
            checkUpdateEnabled = false,
            latestVersionInfo = LatestVersionInfo(),
            currentManagerVersionCode = managerVersion.versionCode,

            // ---- 版本号摘要显示为 "ksuVersion-kernelUAPIVersion" ----
            managerUAPIVersion = 1,
            kernelUAPIVersion = 1,

            // ---- 信息卡片（设备信息读取自 Build，非 root 操作）----
            systemInfo = SystemInfo(
                kernelVersion = Build.VERSION.RELEASE,
                managerVersion = "${managerVersion.versionName} (${managerVersion.versionCode})",
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                fingerprint = Build.FINGERPRINT,
                selinuxStatus = "Enforcing", // "Permissive" 时未安装状态会显示"越狱"按钮
                seccompStatus = 2
            ),
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        // Mock: nothing to refresh.
    }
}
