package com.github.devlotfi.cipherbox.gateway.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.composequery.QueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composevstore.DataStore

class GlobalViewModel : ViewModel() {
  val dataStore = DataStore()
  val queryCache = QueryCache()
  val queryClient = QueryClient(viewModelScope, queryCache)
}

val LocalGlobalViewModel = compositionLocalOf<GlobalViewModel>() { error("Not init") }

@Composable
fun GlobalViewModelProvider(
  viewModel: GlobalViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalGlobalViewModel provides viewModel
  ) {
    content()
  }
}

