package com.github.devlotfi.cipherbox.gateway.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.composevstore.DataStore
import com.github.devlotfi.cipherbox.shared.R as SharedR

class PinScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  var error by mutableStateOf<String?>(null)
  var loading by mutableStateOf(false)
}

@Composable
fun PinScreen(
  modifier: Modifier = Modifier,
  viewModel: PinScreenViewModel = viewModel(),
) {
  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = SharedR.drawable.cipher_box_gateway_logo,
    appName = stringResource(R.string.app_name),
    title = "Enter PIN",
    error = viewModel.error,
    clearError = {
      viewModel.error = null
    },
    loading = viewModel.loading,
    onPin = {
      println(it)
    }
  )
}