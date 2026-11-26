package com.github.devlotfi.cipherbox.client.screens.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.shared.global.AppQueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composevstore.DataStore

class SettingsScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  val queryClient = QueryClient(viewModelScope, AppQueryCache.queryCache)
}

@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: SettingsScreenViewModel = viewModel()
) {
  Text("Settings")
}