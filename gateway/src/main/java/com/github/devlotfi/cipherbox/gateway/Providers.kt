package com.github.devlotfi.cipherbox.gateway

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.github.devlotfi.cipherbox.gateway.providers.AppDatabaseProvider
import com.github.devlotfi.cipherbox.gateway.providers.GlobalViewModelProvider
import com.github.devlotfi.cipherbox.gateway.providers.KeyStoreProvider
import com.github.devlotfi.cipherbox.gateway.providers.NavigationProvider
import com.github.devlotfi.cipherbox.gateway.providers.SqlCipherAppDatabaseProvider

@Composable
fun Providers(
  content: @Composable () -> Unit
) {
  AppTheme {
    GlobalViewModelProvider() {
      KeyStoreProvider {
        AppDatabaseProvider {
          SqlCipherAppDatabaseProvider {
            NavigationProvider {
              content()
            }
          }
        }
      }
    }
  }
}