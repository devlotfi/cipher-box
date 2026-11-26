package com.github.devlotfi.cipherbox.shared.screens.login

import android.app.KeyguardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.global.AppQueryCache
import com.github.devlotfi.cipherbox.shared.navigation.LoginNavigationKeys
import com.github.devlotfi.cipherbox.shared.provider.LocalLoginDb
import com.github.devlotfi.cipherbox.shared.utils.AppIcon
import com.github.devlotfi.cipherbox.shared.utils.CamouflageManager
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberQuery

class InitViewModel : ViewModel() {
  val queryClient = QueryClient(viewModelScope, AppQueryCache.queryCache)
}

@Composable
fun InitScreen(
  modifier: Modifier = Modifier,
  viewModel: InitViewModel = viewModel(),
  backStack: NavBackStack<NavKey>,
  sqlCipherDbExists: () -> Boolean,
  devInit: suspend () -> Unit,
  toWelcome: () -> Unit
) {
  val context = LocalContext.current
  val localLoginDb = LocalLoginDb.current
  val keyguardManager =
    context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

  viewModel.queryClient.rememberQuery {
    queryKey = listOf("INIT")
    queryFn {
      localLoginDb.loginDbManager.init(context)
      val loginDb = requireNotNull(localLoginDb.loginDbManager.db)
      val loginDataEntity = loginDb.loginDataDao().get()
      val exists = sqlCipherDbExists()
      val currentIcon = CamouflageManager.getCurrentIcon(context)

      // for dev only
      devInit()

      if (exists && loginDataEntity?.camouflagePin != null) {
        when (currentIcon) {
          AppIcon.CALCULATOR -> {
            backStack.clear()
            backStack.add(LoginNavigationKeys.CalculatorCamouflageScreen)
          }

          else -> {}
        }
      }

      if (exists) {
        if (!keyguardManager.isDeviceSecure) {
          backStack.add(LoginNavigationKeys.DeviceNotSecure)
          return@queryFn
        }
        backStack.add(LoginNavigationKeys.Pin)
        return@queryFn
      } else {
        if (!keyguardManager.isDeviceSecure) {
          backStack.add(LoginNavigationKeys.DeviceNotSecure)
          return@queryFn
        }
        toWelcome()
        return@queryFn
      }
    }
  }

  Scaffold() { innerPadding ->
    Column(
      modifier = Modifier.padding(innerPadding)
    ) {
      LoadingView(modifier = Modifier.fillMaxSize())
    }
  }
}