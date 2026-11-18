package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devlotfi.kanyon.schema.FieldSchema
import com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.global.KanyonFormDefinition
import com.github.devlotfi.cipherbox.shared.global.RegexPresets
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import com.github.devlotfi.composequery.QueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberMutation
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  val queryCache = QueryCache()
  val queryClient = QueryClient(viewModelScope, queryCache)
}

class SettingsForm(settingsEntity: SettingsEntity) : KanyonFormDefinition() {
  init {
    validateOnChange = true
    validateOnBlur = true
  }

  val serverPort by formField(settingsEntity.serverPort.toString()) {
    FieldSchema().string()
      .regex(RegexPresets.portRegex, error = "Invalid port")
      .notBlank().build()
  }
  val torSocksProxyPort by formField(settingsEntity.torSocksProxyPort.toString()) {
    FieldSchema().string()
      .regex(RegexPresets.portRegex, error = "Invalid port")
      .notBlank().build()
  }
  val bluetoothClientMac by formField(settingsEntity.bluetoothClientMac) {
    FieldSchema().string()
      .mac().build()
  }
}

@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: SettingsScreenViewModel = viewModel()
) {
  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current
  val localSnackbar = LocalSnackbar.current

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

  var form by viewModel.dataStore.rememberValueInDataStore("SETTINGS_FORM") {
    SettingsForm(settingsFlowData.data ?: SettingsEntity())
  }

  val saveSettingsMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("SAVE_SETTINGS")
    mutateFn { params: Unit ->
      localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.db!!.settingsDao().save(
        SettingsEntity(
          serverPort = form.serverPort.value.toInt(),
          torSocksProxyPort = form.torSocksProxyPort.value.toInt(),
          bluetoothClientMac = form.bluetoothClientMac.value
        )
      )

      if (GatewayForegroundService.isServerRunning || GatewayForegroundService.isBluetoothConnected) {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.RESTART.toString()).apply {
            putExtra(GatewayForegroundService.Extra.SERVER_PORT, form.serverPort.value.toInt())
            putExtra(GatewayForegroundService.Extra.BLUETOOTH_MAC, form.bluetoothClientMac.value)
          }
        context.startService(intent)
      }
    }
    onSuccess {
      println("Success")
      scope.launch { localSnackbar.snackbarHostState.showSnackbar("Action success") }
    }
    onError {
      it.printStackTrace()
      scope.launch { localSnackbar.snackbarHostState.showSnackbar("An error occured") }
    }
  }

  LaunchedEffect(settingsFlowData.data) {
    settingsFlowData.data?.let {
      form.serverPort.value = it.serverPort.toString()
      form.torSocksProxyPort.value = it.torSocksProxyPort.toString()
      form.bluetoothClientMac.value = it.bluetoothClientMac
    }
  }

  if (settingsFlowData.loading) {
    LoadingView()
    return
  }


  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 10.dp)
      .padding(top = 20.dp, bottom = 50.dp)
  ) {
    Row(
      modifier = Modifier.padding(bottom = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .clip(
            RoundedCornerShape(10.dp)
          )
          .background(MaterialTheme.colorScheme.primary)
          .padding(3.dp)
      ) {
        Icon(
          imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.settings,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
          tint = MaterialTheme.colorScheme.onPrimary
        )
      }
      Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    OutlinedTextField(
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number
      ),
      label = { Text("Server port") },
      value = form.serverPort.value,
      onValueChange = { form.serverPort.value = it },
      isError = form.serverPort.error != null,
      supportingText = { if (form.serverPort.error != null) Text("${form.serverPort.error}") else Unit }
    )

    OutlinedTextField(
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number
      ),
      label = { Text("TOR Socks proxy port") },
      value = form.torSocksProxyPort.value,
      onValueChange = { form.torSocksProxyPort.value = it },
      isError = form.torSocksProxyPort.error != null,
      supportingText = { if (form.torSocksProxyPort.error != null) Text("${form.torSocksProxyPort.error}") else Unit }
    )

    OutlinedTextField(
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      label = { Text("Bluetooth client mac") },
      value = form.bluetoothClientMac.value ?: "",
      onValueChange = { form.bluetoothClientMac.value = it },
      isError = form.bluetoothClientMac.error != null,
      supportingText = { if (form.bluetoothClientMac.error != null) Text("${form.bluetoothClientMac.error}") else Unit }
    )

    Button(
      shape = RoundedCornerShape(15.dp),
      enabled = !saveSettingsMutation.loading,
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        if (form.validate()) {
          saveSettingsMutation.mutate(Unit)
        }
      },
    ) {
      Text("Save", modifier = Modifier.padding(7.dp))
    }
  }
}