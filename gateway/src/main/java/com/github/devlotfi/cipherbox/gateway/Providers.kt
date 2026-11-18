package com.github.devlotfi.cipherbox.gateway

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.github.devlotfi.cipherbox.gateway.providers.SqlCipherDbProvider
import com.github.devlotfi.cipherbox.shared.provider.LoginDbProvider
import com.github.devlotfi.cipherbox.shared.provider.SnackbarProvider

@Composable
fun Providers(
  content: @Composable () -> Unit
) {
  AppTheme {
    LoginDbProvider {
      SqlCipherDbProvider {
        SnackbarProvider {
          content()
        }
      }
    }
  }
}