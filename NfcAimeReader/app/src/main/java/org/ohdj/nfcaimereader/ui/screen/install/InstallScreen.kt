package org.ohdj.nfcaimereader.ui.screen.install

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.R
import org.ohdj.nfcaimereader.ui.LocalUiMode
import org.ohdj.nfcaimereader.ui.UiMode
import org.ohdj.nfcaimereader.ui.component.dialog.DownloadDialog
import org.ohdj.nfcaimereader.ui.navigation3.LocalNavigator
import org.ohdj.nfcaimereader.ui.util.LkmSelection
import top.yukonga.miuix.kmp.basic.SnackbarHostState as MiuixSnackbarHostState

/**
 * Install screen, ported from KernelSU with all real operations stripped.
 *
 * Mock data used to drive the UI (adjust to preview different states):
 * - mockPartitions / mockDefaultPartition: partition dropdown items
 * - mockRootAvailable / mockIsAbDevice / mockIsGkiDevice: control which install
 *   methods are listed (direct install / inactive slot / select-file tip)
 * - mockCurrentKmi: non-blank so the flow never asks for a manual KMI selection
 * - mockSlotSuffix: suffix shown next to "Select partition"
 *
 * Everything is UI-only: no download, no flashing, no root calls. The file
 * manager intents for picking a boot image / LKM (.ko) file are kept, but the
 * results only update local UI state.
 */
@Composable
fun InstallScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val miuixSnackbarHost = remember { MiuixSnackbarHostState() }
    val uiMode = LocalUiMode.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    // ---- Mock device data (replacing KernelSU's root/partition probing) ----
    val mockPartitions = listOf("boot", "init_boot")
    val mockDefaultPartition = "init_boot"
    val mockRootAvailable = true
    val mockIsAbDevice = true
    val mockIsGkiDevice = true
    val mockCurrentKmi = "6.1.75-android14-11-g1234567890ab"
    val mockSlotSuffix = "_a"

    var installMethod by rememberSaveable { mutableStateOf<InstallMethod?>(null) }
    var downloadDialogShown by rememberSaveable { mutableStateOf(false) }
    var remotePartitions by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var remotePartitionSelectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var lkmSelection by rememberSaveable { mutableStateOf<LkmSelection>(LkmSelection.KmiNone) }
    var partitionSelectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var hasCustomSelected by rememberSaveable { mutableStateOf(false) }
    var advancedOptionsShown by rememberSaveable { mutableStateOf(false) }
    var allowShell by rememberSaveable { mutableStateOf(false) }
    var enableAdb by rememberSaveable { mutableStateOf(false) }
    var forceBackup by rememberSaveable { mutableStateOf(false) }

    val partitions = mockPartitions
    val defaultPartition = mockDefaultPartition
    val slotSuffix = mockSlotSuffix

    val selectFileTip = stringResource(id = R.string.select_file_tip, defaultPartition)
    val selectFileTipNoGki = stringResource(id = R.string.select_file_tip_nogki)
    val downloadFileMsg = stringResource(id = R.string.download_dialog_msg)
    val installMethodOptions = remember(selectFileTip, selectFileTipNoGki, downloadFileMsg) {
        buildList {
            add(InstallMethod.SelectFile(summary = if (mockIsGkiDevice) selectFileTip else selectFileTipNoGki))
            add(InstallMethod.DownloadFile(summary = downloadFileMsg))
            if (mockRootAvailable && mockIsGkiDevice) {
                add(InstallMethod.DirectInstall)
                if (mockIsAbDevice) add(InstallMethod.DirectInstallToInactiveSlot)
            }
        }
    }

    val defaultIndex = remember(partitions, defaultPartition) {
        partitions.indexOf(defaultPartition).coerceAtLeast(0)
    }

    LaunchedEffect(partitions, defaultIndex, hasCustomSelected) {
        if (partitions.isEmpty()) return@LaunchedEffect
        if (!hasCustomSelected) {
            partitionSelectionIndex = defaultIndex.coerceIn(0, partitions.lastIndex)
        } else if (partitionSelectionIndex > partitions.lastIndex) {
            partitionSelectionIndex = partitions.lastIndex
        }
    }

    val displayPartitions = remember(partitions, defaultPartition) {
        partitions.map { name -> if (defaultPartition == name) "$name (default)" else name }
    }
    val remoteDisplayPartitions = remember(remotePartitions, defaultPartition) {
        remotePartitions.map { name -> if (defaultPartition == name) "$name (default)" else name }
    }

    fun showMessage(message: String) {
        scope.launch {
            if (uiMode == UiMode.Material) {
                snackbarHost.showSnackbar(message)
            } else {
                miuixSnackbarHost.showSnackbar(message)
            }
        }
    }

    DownloadDialog(
        show = downloadDialogShown,
        onConfirm = { url ->
            downloadDialogShown = false
            // Mock: no network download/probe. Pretend the remote image contains
            // the same partitions as the local device.
            remotePartitions = mockPartitions
            val defaultIdx = mockPartitions.indexOf(mockDefaultPartition).coerceAtLeast(0)
            remotePartitionSelectionIndex = defaultIdx
            installMethod = InstallMethod.DownloadFile(
                url = url,
                partition = mockPartitions[defaultIdx],
                summary = downloadFileMsg,
            )
        },
        onDismiss = { downloadDialogShown = false }
    )

    // File manager intent: pick an LKM (.ko) file. Result only updates UI state.
    val selectLkmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                if (isKoFile(context, uri)) {
                    lkmSelection = LkmSelection.LkmUri(uri)
                } else {
                    lkmSelection = LkmSelection.KmiNone
                    showMessage(resources.getString(R.string.install_only_support_ko_file))
                }
            }
        }
    }
    // File manager intent: pick a boot image. Result only updates UI state.
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                installMethod = InstallMethod.SelectFile(uri, summary = if (mockIsGkiDevice) selectFileTip else selectFileTipNoGki)
            }
        }
    }

    val state = InstallUiState(
        installMethod = installMethod,
        lkmSelection = lkmSelection,
        partitionSelectionIndex = partitionSelectionIndex,
        displayPartitions = displayPartitions,
        remoteDisplayPartitions = remoteDisplayPartitions,
        remotePartitionSelectionIndex = remotePartitionSelectionIndex,
        currentKmi = mockCurrentKmi,
        slotSuffix = slotSuffix,
        installMethodOptions = installMethodOptions,
        canSelectPartition = installMethod is InstallMethod.DirectInstall ||
            installMethod is InstallMethod.DirectInstallToInactiveSlot ||
            installMethod is InstallMethod.DownloadFile,
        advancedOptionsShown = advancedOptionsShown,
        allowShell = allowShell,
        enableAdb = enableAdb,
        forceBackup = forceBackup,
        canForceBackup = installMethod is InstallMethod.SelectFile,
    )
    val demoNotice = stringResource(id = R.string.install_demo_notice)
    val actions = InstallScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSelectMethod = { method -> installMethod = method },
        onDownloadFile = { downloadDialogShown = true },
        onSelectBootImage = {
            selectImageLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
        },
        onUploadLkm = {
            selectLkmLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
        },
        onClearLkm = { lkmSelection = LkmSelection.KmiNone },
        onSelectPartition = { index ->
            hasCustomSelected = true
            val method = installMethod
            if (method is InstallMethod.DownloadFile) {
                remotePartitionSelectionIndex = index
                installMethod = method.copy(partition = remotePartitions.getOrNull(index))
            } else {
                partitionSelectionIndex = index
            }
        },
        onNext = {
            // Mock: KernelSU pushes the flash screen here (real patching/flashing).
            // UI demo only — show a notice instead.
            showMessage(demoNotice)
        },
        onAdvancedOptionsClicked = {
            advancedOptionsShown = !advancedOptionsShown
        },
        onSelectAllowShell = {
            allowShell = it
        },
        onSelectEnableAdb = {
            enableAdb = it
        },
        onSelectForceBackup = {
            forceBackup = it
        }
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> InstallScreenMiuix(state, actions, miuixSnackbarHost)
        UiMode.Material -> InstallScreenMaterial(state, actions, snackbarHost)
    }
}
