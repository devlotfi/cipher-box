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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import com.github.devlotfi.cipherbox.openapi.apis.DefaultApi
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class InfoScreenViewModel : ViewModel()

@Composable
fun InfoScreen(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel()
) {
  val context = LocalContext.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current
  val localSnackbar = LocalSnackbar.current


  val permissions = remember {
    arrayOf(
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.POST_NOTIFICATIONS
    )
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { result ->
    println(result)
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
    Text("Is busy: ${GatewayForegroundService.isBusy}")
    Text("Is running server: ${GatewayForegroundService.isServerRunning}")
    Text("Is connect bluetooth: ${GatewayForegroundService.isBluetoothConnected}")
    Button(
      onClick = {
        permissionLauncher.launch(permissions)
      }
    ) {
      Text("Request permission")
    }

    Button(
      onClick = {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.START.toString()).apply {
            putExtra(GatewayForegroundService.Extra.SERVER_PORT, settings.serverPort)
            putExtra(GatewayForegroundService.Extra.BLUETOOTH_MAC, "AA:BB:CC:DD:EE:01")
          }
        ContextCompat.startForegroundService(context, intent)
      }
    ) {
      Text("Start")
    }

    Button(
      onClick = {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.RESTART.toString()).apply {
            putExtra(GatewayForegroundService.Extra.SERVER_PORT, settings.serverPort)
            putExtra(GatewayForegroundService.Extra.BLUETOOTH_MAC, "AA:BB:CC:DD:EE:01")
          }
        ContextCompat.startForegroundService(context, intent)
      }
    ) {
      Text("Restart")
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

    Button(
      onClick = {
        viewModel.viewModelScope.launch {
          try {
            val api = DefaultApi(
              baseUrl = "http://vslou3wmeykbg7qpvmn3bb3k5b6lrxltauvfgfyev5zrmlcn3mlgxgid.onion",
              httpClientEngine = OkHttp.create {
                proxy = ProxyBuilder.socks(host = "127.0.0.1", port = 9050)
              },
              httpClientConfig = { config ->
                config.install(ContentNegotiation) {
                  json(JsonInstances.jsonInstance)
                }
              },
            )
            val res = api.listTodos()
            localSnackbar.snackbarHostState.showSnackbar("${res.body()}")
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      }
    ) {
      Text("Test")
    }
  }
}