package com.github.devlotfi.cipherbox.gateway.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreManager

class KeyStoreViewModel : ViewModel() {
  val sqlCipherDatabaseKey = KeyStoreManager("sqlcipher_database_key")
}

val LocalKeystore = compositionLocalOf<KeyStoreViewModel>() { error("Not init") }

@Composable
fun KeyStoreProvider(
  viewModel: KeyStoreViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalKeystore provides viewModel
  ) {
    content()
  }
}