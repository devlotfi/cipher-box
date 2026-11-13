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
import com.github.devlotfi.cipherbox.gateway.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.ScreenNavigationKeys
import com.github.devlotfi.cipherbox.gateway.providers.LocalAppDatabase
import com.github.devlotfi.cipherbox.gateway.providers.LocalGlobalViewModel
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.composequery.rememberQuery

@Composable
fun InitScreen(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val localAppDatabase = LocalAppDatabase.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current
  val localGlobalViewModel = LocalGlobalViewModel.current
  val localNavigation = LocalNavigation.current
  val keyguardManager =
    context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

  localGlobalViewModel.queryClient.rememberQuery {
    queryKey = listOf("INIT")
    queryFn {
      if (!keyguardManager.isDeviceSecure) {
        localNavigation.backStack.add(ScreenNavigationKeys.DeviceNotSecure)
        return@queryFn
      }

      val exists = localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.exists(context)
      if (exists) {
        // for dev only
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
          localAppDatabase.appDatabaseManager.init(context)
          localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.init(
            context,
            "u37M65vvxCNc6E74CBC34akV7hq8Kzg7mBGH7OOONgw="
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