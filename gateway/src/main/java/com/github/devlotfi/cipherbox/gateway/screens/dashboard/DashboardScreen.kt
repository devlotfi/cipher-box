package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.shared.composables.SimpleTopBar
import com.github.devlotfi.cipherbox.shared.icons.outlined.info
import com.github.devlotfi.cipherbox.shared.icons.outlined.settings
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import kotlinx.serialization.Serializable

sealed interface DashboardNavigationKeys {
  @Serializable
  data object Info : NavKey

  @Serializable
  data object Settings : NavKey
}

data class LocalDashboardNavigationData(
  val backStack: NavBackStack<NavKey>
)

val LocalNavigation = compositionLocalOf<LocalDashboardNavigationData> { error("Not init") }


enum class DashboardTabData(
  val navKey: NavKey,
  @StringRes val label: Int,
  val icon: ImageVector,
  val iconSelected: ImageVector,
) {
  Info(
    navKey = DashboardNavigationKeys.Info,
    label = R.string.info,
    icon = info,
    iconSelected = com.github.devlotfi.cipherbox.shared.icons.filled.info
  ),
  Settings(
    navKey = DashboardNavigationKeys.Settings,
    label = R.string.settings,
    icon = settings,
    iconSelected = com.github.devlotfi.cipherbox.shared.icons.filled.settings
  ),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
  modifier: Modifier = Modifier
) {
  val localSnackbar = LocalSnackbar.current
  val backStack = rememberNavBackStack(DashboardNavigationKeys.Info)
  val selectedTab = backStack.lastOrNull() ?: throw Exception("Error")


  Scaffold(
    modifier = modifier,
    snackbarHost = { SnackbarHost(localSnackbar.snackbarHostState) },
    topBar = {
      SimpleTopBar(
        appName = stringResource(R.string.app_name),
        logo = R.drawable.cipher_box_gateway_logo
      )
    },
    bottomBar = {
      if (!WindowInsets.isImeVisible) NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      ) {
        DashboardTabData.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab.navKey,
            onClick = {
              backStack.clear()
              backStack.add(tab.navKey)
            },
            icon = {
              Icon(
                imageVector = if (selectedTab == tab.navKey) tab.iconSelected else tab.icon,
                contentDescription = stringResource(tab.label)
              )
            },
            label = {
              Text(stringResource(tab.label))
            }
          )
        }
      }
    }
  ) { innerPadding ->
    NavDisplay(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      backStack = backStack,
      onBack = {},
      entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator()
      ),
      entryProvider = entryProvider {
        entry<DashboardNavigationKeys.Info> {
          InfoScreen()
        }

        entry<DashboardNavigationKeys.Settings> {
          SettingsScreen()
        }
      }
    )
  }
}