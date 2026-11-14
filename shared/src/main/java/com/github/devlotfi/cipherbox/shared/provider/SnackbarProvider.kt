package com.github.devlotfi.cipherbox.shared.provider

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class SnackbarViewModel : ViewModel() {
  val snackbarHostState = SnackbarHostState()
}

val LocalSnackbar = compositionLocalOf<SnackbarViewModel>() {
  error("Not init")
}

@Composable
fun SnackbarProvider(
  viewModel: SnackbarViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalSnackbar provides viewModel
  ) {
    content()
  }
}