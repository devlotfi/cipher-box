package com.github.devlotfi.cipherbox.gateway.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.Navigation
import com.github.devlotfi.cipherbox.gateway.providers.LocalGlobalViewModel
import com.github.devlotfi.cipherbox.gateway.providers.LocalKeystore
import com.github.devlotfi.cipherbox.gateway.providers.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherDatabase
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.composequery.rememberQuery
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class InitScreenViewModel : ViewModel() {
  val dataStore = DataStore()
}

@Composable
fun InitScreen(
  modifier: Modifier = Modifier,
  viewModel: InitScreenViewModel = viewModel()
) {
  val localContext = LocalContext.current
  val localDatabase = LocalSqlCipherDatabase.current
  val localGlobalViewModel = LocalGlobalViewModel.current
  val localNavigation = LocalNavigation.current
  val localKeyStore = LocalKeystore.current

  val initQuery = localGlobalViewModel.queryClient.rememberQuery {
    queryKey = listOf("INIT")
    queryFn {
      delay(2000.milliseconds)
      val exists = localDatabase.sqlCipherDatabaseManager.exists(localContext)
      if (exists) {
        localNavigation.backStack.add(Navigation.Pin)
      } else {
        localNavigation.backStack.add(Navigation.SetPin)
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