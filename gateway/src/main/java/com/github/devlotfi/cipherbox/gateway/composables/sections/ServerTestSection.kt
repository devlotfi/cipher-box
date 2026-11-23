package com.github.devlotfi.cipherbox.gateway.composables.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.screens.dashboard.InfoScreenViewModel
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import com.github.devlotfi.cipherbox.openapi.apis.DefaultApi
import com.github.devlotfi.cipherbox.shared.composables.ValidatedOutlinedTextField
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.global.KanyonFormDefinition
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import com.github.devlotfi.composeform.onBlur
import com.github.devlotfi.composequery.rememberMutation
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

class TestServerForm(url: String?) : KanyonFormDefinition() {
  init {
    validateOnChange = true
    validateOnBlur = true
  }

  val url by textField(url ?: "")
}

@Composable
fun ServerTestSection(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel(),
  settings: SettingsEntity
) {
  val localSnackbar = LocalSnackbar.current

  val form by viewModel.dataStore.rememberValueInDataStore("TEST_SERVER_FORM") {
    TestServerForm(GatewayForegroundService.onionAddress)
  }

  val testServerMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("TEST_SERVER")
    mutateFn { params: Unit ->
      if (GatewayForegroundService.onionAddress == null) return@mutateFn
      val api = DefaultApi(
        baseUrl = "http://${GatewayForegroundService.onionAddress!!}",
        httpClientEngine = OkHttp.create {
          proxy = ProxyBuilder.socks(host = "127.0.0.1", port = 9050)
        },
        httpClientConfig = { config ->
          config.install(ContentNegotiation) {
            json(JsonInstances.jsonInstance)
          }
        },
      )
      val res = api.test()
      println(res.body())
    }
    onSuccess {
      localSnackbar.snackbarHostState.showSnackbar("Action successful")
    }
    onError {
      it.printStackTrace()
    }
  }

  LaunchedEffect(GatewayForegroundService.onionAddress) {
    GatewayForegroundService.onionAddress.let {
      form.url.value = it ?: ""
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(10.dp)
  ) {
    Text("Calls the hosted server via TOR proxy")

    ValidatedOutlinedTextField(
      textFormFieldData = form.url,
      enableCopyButton = true,
      readOnly = true,
      shape = RoundedCornerShape(15.dp),
      modifier = Modifier
        .onBlur(form.url)
        .fillMaxWidth(),
      label = { Text("URL") },
    )

    Button(
      modifier = Modifier.fillMaxWidth(),
      enabled = !testServerMutation.loading && GatewayForegroundService.isServerRunning && GatewayForegroundService.isTorReady,
      shape = RoundedCornerShape(15.dp),
      onClick = {
        if (form.validate()) {
          testServerMutation.mutate(Unit)
        }
      }
    ) {
      Text("Test", modifier = Modifier.padding(7.dp))
    }
  }
}