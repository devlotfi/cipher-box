package com.github.devlotfi.cipherbox.client

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.github.devlotfi.cipherbox.client.providers.LocalSqlCipherDb
import com.github.devlotfi.cipherbox.client.screens.WelcomeScreen
import com.github.devlotfi.cipherbox.client.screens.dashboard.DashboardScreen
import com.github.devlotfi.cipherbox.client.screens.messages.MessagesAppScreen
import com.github.devlotfi.cipherbox.shared.navigation.LoginNavigationKeys
import com.github.devlotfi.cipherbox.shared.provider.LocalLoginDb
import com.github.devlotfi.cipherbox.shared.screens.DeviceNotSecureScreen
import com.github.devlotfi.cipherbox.shared.screens.login.CalculatorCamouflageScreen
import com.github.devlotfi.cipherbox.shared.screens.login.ConfirmDuressPinScreen
import com.github.devlotfi.cipherbox.shared.screens.login.ConfirmPinScreen
import com.github.devlotfi.cipherbox.shared.screens.login.InitScreen
import com.github.devlotfi.cipherbox.shared.screens.login.PinScreen
import com.github.devlotfi.cipherbox.shared.screens.login.SetDuressPinScreen
import com.github.devlotfi.cipherbox.shared.screens.login.SetPinScreen
import kotlinx.serialization.Serializable

sealed interface ScreenNavigationKeys {
  @Serializable
  data object Welcome : NavKey

  @Serializable
  data object Dashboard : NavKey

  @Serializable
  data object MessagesApp : NavKey
}

data class LocalScreenNavigationData(
  val backStack: NavBackStack<NavKey>
)

val LocalNavigation = compositionLocalOf<LocalScreenNavigationData> { error("Not init") }


@Composable
fun Main(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val localSqlCipherDb = LocalSqlCipherDb.current
  val localLoginDb = LocalLoginDb.current
  val backStack = rememberNavBackStack(LoginNavigationKeys.Init)

  Column(
    modifier = modifier
      .fillMaxSize()
      .imePadding()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        focusManager.clearFocus()
      },
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
          entry<LoginNavigationKeys.Init> {
            InitScreen(
              backStack = backStack,
              toWelcome = {
                backStack.add(ScreenNavigationKeys.Welcome)
              },
              sqlCipherDbExists = {
                localSqlCipherDb.sqlCipherDbManager.exists(context)
              },
              devInit = {
                val exists = localSqlCipherDb.sqlCipherDbManager.exists(context)
                localLoginDb.loginDbManager.init(context)
                localSqlCipherDb.sqlCipherDbManager.init(
                  context,
                  "test"
                )
                backStack.clear()
                backStack.add(ScreenNavigationKeys.Dashboard)
              }
            )
          }

          entry<LoginNavigationKeys.DeviceNotSecure> {
            DeviceNotSecureScreen()
          }

          entry<LoginNavigationKeys.CalculatorCamouflageScreen> {
            CalculatorCamouflageScreen(
              backStack = backStack
            )
          }

          entry<ScreenNavigationKeys.Welcome> {
            WelcomeScreen()
          }

          entry<LoginNavigationKeys.Pin> {
            PinScreen(
              appName = stringResource(R.string.app_name),
              logo = R.drawable.cipher_box_logo,
              backStack = backStack,
              initSqlcipherDb = {
                localSqlCipherDb.sqlCipherDbManager.init(context, it)
              },
              deleteSqlcipherDb = {
                localSqlCipherDb.sqlCipherDbManager.delete(context)
              },
              onSuccess = {
                backStack.add(ScreenNavigationKeys.Dashboard)
              }
            )
          }

          entry<LoginNavigationKeys.SetPin> {
            SetPinScreen(
              appName = stringResource(R.string.app_name),
              logo = R.drawable.cipher_box_logo,
              backStack = backStack
            )
          }

          entry<LoginNavigationKeys.ConfirmPin> { key ->
            ConfirmPinScreen(
              appName = stringResource(R.string.app_name),
              logo = R.drawable.cipher_box_logo,
              pin = key.pin,
              backStack = backStack
            )
          }

          entry<LoginNavigationKeys.SetDuressPin> { key ->
            SetDuressPinScreen(
              appName = stringResource(R.string.app_name),
              logo = R.drawable.cipher_box_logo,
              pin = key.pin,
              backStack = backStack
            )
          }

          entry<LoginNavigationKeys.ConfirmDuressPin> { key ->
            ConfirmDuressPinScreen(
              appName = stringResource(R.string.app_name),
              logo = R.drawable.cipher_box_logo,
              pin = key.pin,
              duressPin = key.duressPin,
              backStack = backStack,
              setupSqlCipherDb = {
                localSqlCipherDb.sqlCipherDbManager.delete(context)
                localSqlCipherDb.sqlCipherDbManager.init(context, it)
                val sqlCipherDB = requireNotNull(localSqlCipherDb.sqlCipherDbManager.db)
              },
              closeSqlCipherDb = {
                localSqlCipherDb.sqlCipherDbManager.close()
              }
            )
          }

          entry<ScreenNavigationKeys.Dashboard> {
            DashboardScreen()
          }

          entry<ScreenNavigationKeys.MessagesApp> {
            MessagesAppScreen()
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