package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherDb
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import com.github.devlotfi.cipherbox.shared.composables.AppIconSelector
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.composables.SectionTitle
import com.github.devlotfi.cipherbox.shared.composables.ValidatedOutlinedSecureTextField
import com.github.devlotfi.cipherbox.shared.composables.ValidatedOutlinedTextField
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

  val torRc by textField(settingsEntity.torRc ?: "") {
    FieldSchema().string().build()
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
        torRc = form.torRc.value.takeIf { it.isNotBlank() },
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
      .padding(top = 20.dp, bottom = 50.dp)
  ) {
    Column(
      modifier = modifier
        .padding(horizontal = 10.dp)
        .padding(bottom = 20.dp)
    ) {
      SectionTitle(
        text = "Tor Settings",
        imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.tor
      )

      ValidatedOutlinedTextField(
        textFormFieldData = form.torRc,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
          .onBlur(form.torRc)
          .fillMaxWidth(),
        lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 5),
        label = { Text("Tor Config") },
      )

      SectionTitle(
        text = "Bluetooth Settings",
        imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.bluetooth
      )

      ValidatedOutlinedSecureTextField(
        textFormFieldData = form.bluetoothMac,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
          .onBlur(form.bluetoothMac)
          .fillMaxWidth(),
        label = { Text("ESP32 Bluetooth mac") },
      )

      SectionTitle(
        text = "MQTT Settings",
        imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.mqtt
      )

      ValidatedOutlinedTextField(
        textFormFieldData = form.mqttHost,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
          .onBlur(form.mqttHost)
          .fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        label = { Text("MQTT Host") },
      )

      ValidatedOutlinedTextField(
        textFormFieldData = form.mqttPort,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
          .onBlur(form.mqttPort)
          .fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text("MQTT port") },
      )

      ValidatedOutlinedTextField(
        textFormFieldData = form.mqttUsername,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
          .onBlur(form.mqttUsername)
          .fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        label = { Text("MQTT Username") },
      )

      ValidatedOutlinedSecureTextField(
        textFormFieldData = form.mqttPassword,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
          .onBlur(form.mqttPassword)
          .fillMaxWidth(),
        label = { Text("MQTT Password") },
      )

      ValidatedOutlinedSecureTextField(
        textFormFieldData = form.mqttTopic,
        enableCopyButton = true,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
          .onBlur(form.mqttTopic)
          .fillMaxWidth(),
        label = { Text("MQTT Topic") },
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

    HorizontalDivider()

    Column() {
      SectionTitle(
        text = "Camouflage Settings",
        imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.widgets,
        modifier = Modifier
          .padding(horizontal = 10.dp)
          .padding(top = 20.dp)
      )

      AppIconSelector(
        modifier = Modifier
          .padding(horizontal = 10.dp)
          .padding(top = 20.dp),
        defaultIcon = R.drawable.cipher_box_gateway_logo,
        defaultSplashTheme = R.style.Theme_CipherBox_Splash_Default,
        dataStore = viewModel.dataStore,
        coroutineScope = viewModel.viewModelScope
      )
    }
  }
}