package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import kotlinx.coroutines.flow.map

@Composable
fun InfoScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {
    println(it)
  }

  if (localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.db == null) throw Exception("")
  val settingsFlow = remember {
    localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.db!!.settingsDao().observe()
      .map { RoomFlowData(loading = false, data = it) }
  }
  val settingsFlowData by settingsFlow.collectAsStateWithLifecycle(
    initialValue = RoomFlowData(
      loading = true
    )
  )
  val settings = settingsFlowData.data ?: SettingsEntity()

  if (settingsFlowData.loading) {
    LoadingView()
    return
  }

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    Text("Info screen")
    Text("Is running: ${GatewayForegroundService.isRunning}")
    Button(
      onClick = {
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    ) {
      Text("Request permission")
    }

    Button(
      onClick = {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.START.toString()).apply {
            putExtra(GatewayForegroundService.Extra.SERVER_PORT, settings.serverPort)
          }
        ContextCompat.startForegroundService(context, intent)
      }
    ) {
      Text("Start")
    }

    Button(
      onClick = {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.STOP.toString())
        context.startService(intent)
      }
    ) {
      Text("Stop")
    }
  }
}