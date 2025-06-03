package org.ohdj.nfcaimereader.ui.screen.home

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ohdj.nfcaimereader.ui.navigation.Screen
import org.ohdj.nfcaimereader.ui.screen.home.component.NfcStatusComponent
import org.ohdj.nfcaimereader.ui.screen.home.component.WebSocketStatusComponent
import org.ohdj.nfcaimereader.ui.viewmodel.HomeViewModel
import org.ohdj.nfcaimereader.ui.viewmodel.WebSocketScreenViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    webSocketViewModel: WebSocketScreenViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 使用DisposableEffect注册和注销当前Activity到NfcManager
    // 这使得NfcManager能够基于应用的前台状态启用/禁用读卡器模式
    DisposableEffect(activity, homeViewModel.nfcManager) {
        if (activity != null) {
            homeViewModel.nfcManager.registerActivity(activity)
        }
        onDispose {
            if (activity != null) {
                homeViewModel.nfcManager.unregisterActivity(activity)
            }
        }
    }

    Column {
        val nfcState by homeViewModel.nfcState.collectAsState()
        val cardIdm by homeViewModel.cardIdm.collectAsState()

        NfcStatusComponent(
            nfcState = nfcState,
            cardIdm = cardIdm
        )

        Spacer(modifier = Modifier.height(16.dp))

        val connectionState by webSocketViewModel.connectionState.collectAsState()
        WebSocketStatusComponent(
            connectionState = connectionState,
            onClick = {
                navController.navigate(Screen.Config.route)
            }
        )
    }
}