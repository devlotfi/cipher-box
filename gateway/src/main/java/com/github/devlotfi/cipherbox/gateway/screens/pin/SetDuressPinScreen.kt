package com.github.devlotfi.cipherbox.gateway.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.gateway.ScreenNavigationKeys
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.composevstore.DataStore
import com.github.devlotfi.cipherbox.shared.R as SharedR

class SetDuressPinScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  var error by mutableStateOf<String?>(null)
  var loading by mutableStateOf(false)
}

@Composable
fun SetDuressPinScreen(
  pin: String,
  modifier: Modifier = Modifier,
  viewModel: SetDuressPinScreenViewModel = viewModel(),
) {
  val localNavigation = LocalNavigation.current

  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = SharedR.drawable.cipher_box_gateway_logo,
    appName = stringResource(R.string.app_name),
    title = "Set a duress pin",
    subTitle = "The pin used wipe app data in case of emergency",
    error = viewModel.error,
    titleIcon = {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          com.github.devlotfi.cipherbox.shared.icons.outlined.delete_forever,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
          tint = MaterialTheme.colorScheme.onError
        )
      }
    },
    clearError = {
      viewModel.error = null
    },
    loading = viewModel.loading,
    onPin = {
      localNavigation.backStack.add(
        ScreenNavigationKeys.ConfirmDuressPin(
          pin = pin,
          duressPin = it
        )
      )
    }
  )
}