package com.github.devlotfi.cipherbox.gateway

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.github.devlotfi.cipherbox.gateway.providers.AppDatabaseProvider
import com.github.devlotfi.cipherbox.gateway.providers.GlobalViewModelProvider
import com.github.devlotfi.cipherbox.gateway.providers.KeyStoreProvider
import com.github.devlotfi.cipherbox.gateway.providers.SqlCipherAppDatabaseProvider
import com.github.devlotfi.cipherbox.shared.provider.SnackbarProvider

@Composable
fun Providers(
  content: @Composable () -> Unit
) {
  AppTheme {
    GlobalViewModelProvider() {
      KeyStoreProvider {
        AppDatabaseProvider {
          SqlCipherAppDatabaseProvider {
            SnackbarProvider {
              content()
            }
          }
        }
      }
    }
  }
}