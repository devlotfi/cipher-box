package com.github.devlotfi.cipherbox.gateway

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.github.devlotfi.cipherbox.gateway.providers.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.screens.ConfirmPinScreen
import com.github.devlotfi.cipherbox.gateway.screens.DashboardScreen
import com.github.devlotfi.cipherbox.gateway.screens.InitScreen
import com.github.devlotfi.cipherbox.gateway.screens.PinScreen
import com.github.devlotfi.cipherbox.gateway.screens.SetPinScreen

@Composable
fun Main(modifier: Modifier = Modifier) {
  val localNavigation = LocalNavigation.current

  Column(
    modifier = modifier
      .fillMaxSize(),
  ) {
    NavDisplay(
      backStack = localNavigation.backStack,
      onBack = { localNavigation.backStack.removeLastOrNull() },
      entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator()
      ),
      entryProvider = { key ->
        when (key) {
          is Navigation.Init -> NavEntry(key) {
            InitScreen(modifier = Modifier.weight(1f))
          }

          is Navigation.Pin -> NavEntry(key) {
            PinScreen(modifier = Modifier.weight(1f))
          }

          is Navigation.SetPin -> NavEntry(key) {
            SetPinScreen(modifier = Modifier.weight(1f))
          }

          is Navigation.ConfirmPin -> NavEntry(key) {
            ConfirmPinScreen(modifier = Modifier.weight(1f), pin = key.pin)
          }

          is Navigation.Dashboard -> NavEntry(key) {
            DashboardScreen(modifier = Modifier.weight(1f))
          }

          else -> NavEntry(key) {
            Text("Unknown")
          }
        }
      },
      transitionSpec = {
        // Slide in from right when navigating forward
        slideInHorizontally(initialOffsetX = { it }) togetherWith
          slideOutHorizontally(targetOffsetX = { -it })
      },
      popTransitionSpec = {
        // Slide in from left when navigating back
        slideInHorizontally(initialOffsetX = { -it }) togetherWith
          slideOutHorizontally(targetOffsetX = { it })
      },
      predictivePopTransitionSpec = {
        // Slide in from left when navigating back
        slideInHorizontally(initialOffsetX = { -it }) togetherWith
          slideOutHorizontally(targetOffsetX = { it })
      },
    )
  }
}