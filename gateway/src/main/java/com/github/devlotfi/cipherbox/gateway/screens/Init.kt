package com.github.devlotfi.cipherbox.gateway.screens

import android.app.KeyguardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.devlotfi.cipherbox.gateway.Navigation
import com.github.devlotfi.cipherbox.gateway.providers.LocalGlobalViewModel
import com.github.devlotfi.cipherbox.gateway.providers.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.composequery.rememberQuery

@Composable
fun InitScreen(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current
  val localGlobalViewModel = LocalGlobalViewModel.current
  val localNavigation = LocalNavigation.current
  val keyguardManager =
    context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

  localGlobalViewModel.queryClient.rememberQuery {
    queryKey = listOf("INIT")
    queryFn {
      if (!keyguardManager.isDeviceSecure) {
        localNavigation.backStack.add(Navigation.DeviceNotSecure)
        return@queryFn
      }

      val exists = localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.exists(context)
      if (exists) {
        localNavigation.backStack.add(Navigation.Pin)
      } else {
        localNavigation.backStack.add(Navigation.Welcome)
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