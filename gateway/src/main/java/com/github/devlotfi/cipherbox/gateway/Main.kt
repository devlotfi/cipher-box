package com.github.devlotfi.cipherbox.gateway

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.github.devlotfi.cipherbox.gateway.screens.ConfirmDuressPinScreen
import com.github.devlotfi.cipherbox.gateway.screens.ConfirmPinScreen
import com.github.devlotfi.cipherbox.gateway.screens.DeviceNotSecureScreen
import com.github.devlotfi.cipherbox.gateway.screens.InitScreen
import com.github.devlotfi.cipherbox.gateway.screens.PinScreen
import com.github.devlotfi.cipherbox.gateway.screens.SetDuressPinScreen
import com.github.devlotfi.cipherbox.gateway.screens.SetPinScreen
import com.github.devlotfi.cipherbox.gateway.screens.WelcomeScreen
import com.github.devlotfi.cipherbox.gateway.screens.dashboard.DashboardScreen
import kotlinx.serialization.Serializable

sealed interface ScreenNavigationKeys {
  @Serializable
  data object Init : NavKey

  @Serializable
  data object DeviceNotSecure : NavKey

  @Serializable
  data object Welcome : NavKey

  @Serializable
  data object Pin : NavKey

  @Serializable
  data object SetPin : NavKey

  @Serializable
  data class ConfirmPin(val pin: String) : NavKey

  @Serializable
  data class SetDuressPin(val pin: String) : NavKey

  @Serializable
  data class ConfirmDuressPin(val pin: String, val duressPin: String) : NavKey

  @Serializable
  data object Dashboard : NavKey
}

data class LocalScreenNavigationData(
  val backStack: NavBackStack<NavKey>
)

val LocalNavigation = compositionLocalOf<LocalScreenNavigationData> { error("Not init") }


@Composable
fun Main(modifier: Modifier = Modifier) {
  val backStack = rememberNavBackStack(ScreenNavigationKeys.Init)

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    CompositionLocalProvider(
      LocalNavigation provides LocalScreenNavigationData(backStack)
    ) {
      NavDisplay(
        backStack = backStack,
        onBack = {
          backStack.removeLastOrNull()
        },
        entryDecorators = listOf(
          rememberSaveableStateHolderNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
          entry<ScreenNavigationKeys.Init> {
            InitScreen()
          }

          entry<ScreenNavigationKeys.DeviceNotSecure> {
            DeviceNotSecureScreen(
            )
          }

          entry<ScreenNavigationKeys.Welcome> {
            WelcomeScreen()
          }

          entry<ScreenNavigationKeys.Pin> {
            PinScreen()
          }

          entry<ScreenNavigationKeys.SetPin> {
            SetPinScreen()
          }

          entry<ScreenNavigationKeys.ConfirmPin> { key ->
            ConfirmPinScreen(
              pin = key.pin
            )
          }

          entry<ScreenNavigationKeys.SetDuressPin> { key ->
            SetDuressPinScreen(
              pin = key.pin
            )
          }

          entry<ScreenNavigationKeys.ConfirmDuressPin> { key ->
            ConfirmDuressPinScreen(
              pin = key.pin,
              duressPin = key.duressPin
            )
          }

          entry<ScreenNavigationKeys.Dashboard> {
            DashboardScreen()
          }
        },
        transitionSpec = {
          slideInHorizontally(
            initialOffsetX = { it }
          ) togetherWith slideOutHorizontally(
            targetOffsetX = { -it }
          )
        },
        popTransitionSpec = {
          slideInHorizontally(
            initialOffsetX = { -it }
          ) togetherWith slideOutHorizontally(
            targetOffsetX = { it }
          )
        },
        predictivePopTransitionSpec = {
          slideInHorizontally(
            initialOffsetX = { -it }
          ) togetherWith slideOutHorizontally(
            targetOffsetX = { it }
          )
        }
      )
    }
  }
}
