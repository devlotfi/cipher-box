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

class ConfirmPinScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  var error by mutableStateOf<String?>(null)
  var loading by mutableStateOf(false)
}

@Composable
fun ConfirmPinScreen(
  modifier: Modifier = Modifier,
  viewModel: ConfirmPinScreenViewModel = viewModel(),
  pin: String
) {
  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = SharedR.drawable.cipher_box_gateway_logo,
    appName = stringResource(R.string.app_name),
    title = "Confirm PIN",
    error = viewModel.error,
    clearError = {
      viewModel.error = null
    },
    loading = viewModel.loading,
    onPin = {
      if (it != pin) {
        viewModel.error = "Pins dont match"
      }
    }
  )
}