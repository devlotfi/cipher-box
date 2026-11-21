package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherDb
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.composables.SectionTitle
import com.github.devlotfi.cipherbox.shared.crypto.SHAUtils
import com.github.devlotfi.cipherbox.shared.global.KanyonFormDefinition
import com.github.devlotfi.cipherbox.shared.global.RegexPresets
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import com.github.devlotfi.composeform.onBlur
import com.github.devlotfi.composequery.QueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberMutation
import com.github.devlotfi.composevstore.DataStore
import com.github.devlotfi.kanyon.schema.FieldSchema
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  val queryCache = QueryCache()
  val queryClient = QueryClient(viewModelScope, queryCache)
}

class SettingsForm(settingsEntity: SettingsEntity) : KanyonFormDefinition() {
  init {
    validateOnChange = false
    validateOnBlur = true
  }

  val torSocksProxyPort by textField(settingsEntity.torSocksProxyPort.toString()) {
    FieldSchema().string()
      .regex(RegexPresets.portRegex, error = "Invalid port").notBlank().build()
  }
  val torRc by textField(settingsEntity.torRc ?: "") {
    FieldSchema().string().build()
  }

  val serverPort by textField(settingsEntity.serverPort.toString()) {
    FieldSchema().string()
      .regex(RegexPresets.portRegex, error = "Invalid port").notBlank().build()
  }
  val gatewayKey by textField(settingsEntity.gatewayKey ?: "") {
    FieldSchema().string().notBlank().build()
  }

  val bluetoothMac by textField(settingsEntity.bluetoothMac) {
    FieldSchema().string().mac().build()
  }

  val mqttHost by textField(settingsEntity.mqttHost ?: "") {
    FieldSchema().string().notBlank().build()
  }
  val mqttPort by textField(settingsEntity.mqttPort.toString()) {
    FieldSchema().string()
      .regex(RegexPresets.portRegex, error = "Invalid port").notBlank().build()
  }
  val mqttUsername by textField(settingsEntity.mqttUsername ?: "") {
    FieldSchema().string().build()
  }
  val mqttPassword by textField(settingsEntity.mqttPassword ?: "") {
    FieldSchema().string().build()
  }
  val mqttTopic by textField(settingsEntity.mqttTopic ?: "") {
    FieldSchema().string().notBlank().build()
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
  val localSqlCipherDb = LocalSqlCipherDb.current
  val localSnackbar = LocalSnackbar.current

  if (localSqlCipherDb.sqlCipherDbManager.db == null) throw Exception("")
  val settingsFlow = remember {
    localSqlCipherDb.sqlCipherDbManager.db!!.settingsDao().observe()
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
      val settingsEntity = SettingsEntity(
        torSocksProxyPort = form.torSocksProxyPort.value.toInt(),
        torRc = form.torRc.value.takeIf { it.isNotBlank() },
        serverPort = form.serverPort.value.toInt(),
        gatewayKey = form.gatewayKey.value,
        gatewayKeyHash = SHAUtils.hash(form.gatewayKey.value),
        bluetoothMac = form.bluetoothMac.value,
        mqttHost = form.mqttHost.value,
        mqttPort = form.mqttPort.value.toInt(),
        mqttUsername = form.mqttUsername.value.takeIf { it.isNotBlank() },
        mqttPassword = form.mqttPassword.value.takeIf { it.isNotBlank() },
        mqttTopic = form.mqttTopic.value,
      )
      localSqlCipherDb.sqlCipherDbManager.db!!.settingsDao().save(settingsEntity)

      if (GatewayForegroundService.isServerRunning || GatewayForegroundService.isBluetoothConnected) {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.RESTART.toString())
        GatewayForegroundService.putIntentExtras(
          intent = intent,
          intentExtras = settingsEntity.toIntentExtras()
        )
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
      form.torRc.value = it.torRc ?: ""
      form.torSocksProxyPort.value = it.torSocksProxyPort.toString()
      form.serverPort.value = it.serverPort.toString()
      form.gatewayKey.value = it.gatewayKey ?: ""
      form.bluetoothMac.value = it.bluetoothMac
      form.mqttHost.value = it.mqttHost ?: ""
      form.mqttPort.value = it.mqttPort.toString()
      form.mqttUsername.value = it.mqttUsername ?: ""
      form.mqttPassword.value = it.mqttPassword ?: ""
      form.mqttTopic.value = it.mqttTopic ?: ""
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
    SectionTitle(
      text = "Tor Settings",
      imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.tor
    )

    OutlinedTextField(
      state = form.torSocksProxyPort.state,
      inputTransformation = form.torSocksProxyPort.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.torSocksProxyPort)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      label = { Text("Tor Socks proxy port") },
      isError = form.torSocksProxyPort.error != null,
      supportingText = { form.torSocksProxyPort.error?.let { Text(it) } }
    )

    OutlinedTextField(
      state = form.torRc.state,
      inputTransformation = form.torRc.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.torRc)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 5),
      label = { Text("Tor Config") },
      isError = form.torRc.error != null,
      supportingText = { form.torRc.error?.let { Text(it) } }
    )

    SectionTitle(
      text = "Server Settings",
      imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.public
    )

    OutlinedTextField(
      state = form.serverPort.state,
      inputTransformation = form.serverPort.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.serverPort)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      label = { Text("Server port") },
      isError = form.serverPort.error != null,
      supportingText = { form.serverPort.error?.let { Text(it) } }
    )

    OutlinedTextField(
      state = form.gatewayKey.state,
      readOnly = true,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.gatewayKey)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      label = { Text("Gateway Key") },
      trailingIcon = {
        Row(
          modifier = Modifier.padding(horizontal = 5.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          IconButton(onClick = { }) {
            Icon(
              imageVector = com.github.devlotfi.cipherbox.shared.icons.error,
              contentDescription = null,
              modifier = Modifier.size(30.dp)
            )
          }
        }
      },
      isError = form.gatewayKey.error != null,
      supportingText = { form.gatewayKey.error?.let { Text(it) } }
    )

    SectionTitle(
      text = "Bluetooth Settings",
      imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.bluetooth
    )

    OutlinedTextField(
      state = form.bluetoothMac.state,
      inputTransformation = form.bluetoothMac.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.bluetoothMac)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      label = { Text("ESP32 Bluetooth mac") },
      isError = form.bluetoothMac.error != null,
      supportingText = { form.bluetoothMac.error?.let { Text(it) } }
    )

    SectionTitle(
      text = "MQTT Settings",
      imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.mqtt
    )

    OutlinedTextField(
      state = form.mqttHost.state,
      inputTransformation = form.mqttHost.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.mqttHost)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      label = { Text("MQTT Host") },
      isError = form.mqttHost.error != null,
      supportingText = { form.mqttHost.error?.let { Text(it) } }
    )

    OutlinedTextField(
      state = form.mqttPort.state,
      inputTransformation = form.mqttPort.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.mqttPort)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      label = { Text("MQTT port") },
      isError = form.mqttPort.error != null,
      supportingText = { form.mqttPort.error?.let { Text(it) } }
    )

    OutlinedTextField(
      state = form.mqttUsername.state,
      inputTransformation = form.mqttUsername.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.mqttUsername)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      label = { Text("MQTT Username") },
      isError = form.mqttUsername.error != null,
      supportingText = { form.mqttUsername.error?.let { Text(it) } }
    )

    OutlinedTextField(
      state = form.mqttPassword.state,
      inputTransformation = form.mqttPassword.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.mqttPassword)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      label = { Text("MQTT Password") },
      isError = form.mqttPassword.error != null,
      supportingText = { form.mqttPassword.error?.let { Text(it) } }
    )

    OutlinedTextField(
      state = form.mqttTopic.state,
      inputTransformation = form.mqttTopic.inputTransformation,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.mqttTopic)
        .fillMaxWidth(),
      lineLimits = TextFieldLineLimits.SingleLine,
      label = { Text("MQTT Topic") },
      isError = form.mqttTopic.error != null,
      supportingText = { form.mqttTopic.error?.let { Text(it) } }
    )

    Button(
      shape = RoundedCornerShape(15.dp),
      enabled = !saveSettingsMutation.loading,
      modifier = Modifier.fillMaxWidth(),
      onClick = { if (form.validate()) saveSettingsMutation.mutate(Unit) },
    ) {
      Text("Save", modifier = Modifier.padding(7.dp))
    }
  }
}