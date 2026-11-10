package com.github.devlotfi.cipherbox.gateway.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.Navigation
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.gateway.providers.LocalNavigation
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.composevstore.DataStore
import com.github.devlotfi.cipherbox.shared.R as SharedR

class SetPinScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  var error by mutableStateOf<String?>(null)
  var loading by mutableStateOf(false)
}

@Composable
fun SetPinScreen(
  modifier: Modifier = Modifier,
  viewModel: SetPinScreenViewModel = viewModel(),
) {
  val localNavigation = LocalNavigation.current

  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = SharedR.drawable.cipher_box_gateway_logo,
    appName = stringResource(R.string.app_name),
    title = "Set a PIN",
    error = viewModel.error,
    clearError = {
      viewModel.error = null
    },
    loading = viewModel.loading,
    onPin = {
      localNavigation.backStack.add(Navigation.ConfirmPin(it))
    }
  )
}