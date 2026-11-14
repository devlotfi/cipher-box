package com.github.devlotfi.cipherbox.gateway.screens.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.gateway.ScreenNavigationKeys
import com.github.devlotfi.cipherbox.gateway.providers.LocalAppDatabase
import com.github.devlotfi.cipherbox.gateway.providers.LocalKeystore
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreAuthenticator
import com.github.devlotfi.composevstore.DataStore
import com.github.devlotfi.cipherbox.shared.R as SharedR

class ConfirmPinScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  var error by mutableStateOf<String?>(null)
  var loading by mutableStateOf(false)
}

@Composable
fun ConfirmPinScreen(
  pin: String,
  modifier: Modifier = Modifier,
  viewModel: ConfirmPinScreenViewModel = viewModel(),
) {
  val context = LocalContext.current
  val activity = context as FragmentActivity
  val localKeyStore = LocalKeystore.current
  val localAppDatabase = LocalAppDatabase.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current
  val localNavigation = LocalNavigation.current
  val authenticator = remember {
    KeyStoreAuthenticator(activity)
  }

  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = SharedR.drawable.cipher_box_gateway_logo,
    appName = stringResource(R.string.app_name),
    title = "Confirm PIN",
    titleIcon = {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          com.github.devlotfi.cipherbox.shared.icons.outlined.lock,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
          tint = MaterialTheme.colorScheme.onPrimary
        )
      }
    },
    error = viewModel.error,
    clearError = {
      viewModel.error = null
    },
    loading = viewModel.loading,
    onPin = {
      if (it != pin) {
        viewModel.error = "Pins dont match"
      } else {
        localNavigation.backStack.add(ScreenNavigationKeys.SetDuressPin(pin = pin))
      }
    }
  )
}