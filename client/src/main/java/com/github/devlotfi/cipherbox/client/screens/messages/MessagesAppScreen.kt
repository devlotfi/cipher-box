package com.github.devlotfi.cipherbox.client.screens.messages

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.github.devlotfi.cipherbox.client.R
import com.github.devlotfi.cipherbox.shared.composables.SimpleTopBar
import com.github.devlotfi.cipherbox.shared.provider.LocalSnackbar
import kotlinx.serialization.Serializable

sealed interface MessagesAppNavigationKeys {
  @Serializable
  data object Contacts : NavKey

  @Serializable
  data object UnverifiedMessages : NavKey
}


enum class DashboardTabData(
  val navKey: NavKey,
  @StringRes val label: Int,
  val icon: ImageVector,
  val iconSelected: ImageVector,
) {
  Contacts(
    navKey = MessagesAppNavigationKeys.Contacts,
    label = R.string.contacts,
    icon = com.github.devlotfi.cipherbox.shared.icons.outlined.contacts,
    iconSelected = com.github.devlotfi.cipherbox.shared.icons.filled.contacts
  ),
  UnverifiedMessages(
    navKey = MessagesAppNavigationKeys.UnverifiedMessages,
    label = R.string.unverified,
    icon = com.github.devlotfi.cipherbox.shared.icons.outlined.chat_error,
    iconSelected = com.github.devlotfi.cipherbox.shared.icons.filled.chat_error
  ),
}

@Composable
fun MessagesAppScreen(
  modifier: Modifier = Modifier
) {
  val localSnackbar = LocalSnackbar.current
  val backStack = rememberNavBackStack(MessagesAppNavigationKeys.Contacts)
  val selectedTab = backStack.lastOrNull() ?: throw Exception("Error")


  Scaffold(
    modifier = modifier,
    snackbarHost = { SnackbarHost(localSnackbar.snackbarHostState) },
    topBar = {
      SimpleTopBar(
        appName = "Messages",
        logo = R.drawable.cipher_box_logo
      )
    },
    floatingActionButton = {
      if (selectedTab is MessagesAppNavigationKeys.Contacts) {
        FloatingActionButton(
          onClick = { },
        ) {
          Icon(
            imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.add,
            contentDescription = null
          )
        }
      }
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
        entry<MessagesAppNavigationKeys.Contacts> {
          ContactsScreen()
        }

        entry<MessagesAppNavigationKeys.UnverifiedMessages> {
          UnverifiedMessagesScreen()
        }
      }
    )
  }
}