package com.github.devlotfi.cipherbox.shared.screens.login

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.devlotfi.cipherbox.shared.navigation.LoginNavigationKeys
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.composevstore.DataStore

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
  backStack: NavBackStack<NavKey>,
  appName: String,
  @DrawableRes logo: Int
) {
  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = logo,
    appName = appName,
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
      backStack.add(
        LoginNavigationKeys.ConfirmDuressPin(
          pin = pin,
          duressPin = it
        )
      )
    }
  )
}