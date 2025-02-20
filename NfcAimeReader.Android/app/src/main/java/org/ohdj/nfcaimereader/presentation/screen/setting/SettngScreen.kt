package org.ohdj.nfcaimereader.presentation.screen.setting

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingScreen(viewModel: SettingViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadAutoReconnect(context)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "启用自动重连")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = viewModel.autoReconnect.collectAsState().value,
                onCheckedChange = { viewModel.setAutoReconnect(context, it) }
            )
        }
    }
}
