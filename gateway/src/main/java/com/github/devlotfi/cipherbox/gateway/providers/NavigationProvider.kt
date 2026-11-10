package com.github.devlotfi.cipherbox.gateway.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.github.devlotfi.cipherbox.gateway.Navigation

data class LocalNavigationData(
  val backStack: NavBackStack<NavKey>
)

val LocalNavigation = compositionLocalOf<LocalNavigationData> { error("Not init") }

@Composable
fun NavigationProvider(
  content: @Composable () -> Unit
) {
  val backStack = rememberNavBackStack(Navigation.Init)

  return CompositionLocalProvider(
    LocalNavigation provides LocalNavigationData(backStack)
  ) {
    content()
  }
}