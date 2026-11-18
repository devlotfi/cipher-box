package com.github.devlotfi.cipherbox.gateway.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ApplicationInfo
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
import com.github.devlotfi.cipherbox.gateway.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.ScreenNavigationKeys
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherDb
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.global.AppQueryCache
import com.github.devlotfi.cipherbox.shared.provider.LocalLoginDb
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberQuery

class InitViewModel : ViewModel() {
  val queryClient = QueryClient(viewModelScope, AppQueryCache.queryCache)
}

@Composable
fun InitScreen(
  modifier: Modifier = Modifier,
  viewModel: InitViewModel = viewModel()
) {
  val context = LocalContext.current
  val localLoginDb = LocalLoginDb.current
  val localSqlCipherDb = LocalSqlCipherDb.current
  val localNavigation = LocalNavigation.current
  val keyguardManager =
    context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

  viewModel.queryClient.rememberQuery {
    queryKey = listOf("INIT")
    queryFn {
      if (!keyguardManager.isDeviceSecure) {
        localNavigation.backStack.add(ScreenNavigationKeys.DeviceNotSecure)
        return@queryFn
      }

      val exists = localSqlCipherDb.sqlCipherDbManager.exists(context)
      if (exists) {
        // for dev only
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
          localLoginDb.loginDbManager.init(context)
          localSqlCipherDb.sqlCipherDbManager.delete(context)
          localSqlCipherDb.sqlCipherDbManager.init(
            context,
            "test"
          )
          localNavigation.backStack.add(ScreenNavigationKeys.Dashboard)
          return@queryFn
        }

        localNavigation.backStack.add(ScreenNavigationKeys.Pin)
      } else {
        localNavigation.backStack.add(ScreenNavigationKeys.Welcome)
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