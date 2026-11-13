package com.github.devlotfi.cipherbox.gateway.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.gateway.screens.dashboard.InfoScreen
import com.github.devlotfi.cipherbox.gateway.screens.dashboard.SettingsScreen
import com.github.devlotfi.cipherbox.shared.composables.SimpleTopBar
import com.github.devlotfi.cipherbox.shared.R as SharedR

enum class AppTab(
  @StringRes val label: Int,
  val icon: ImageVector,
  val iconSelected: ImageVector,
) {
  Info(
    label = R.string.info,
    icon = com.github.devlotfi.cipherbox.shared.icons.outlined.info,
    iconSelected = com.github.devlotfi.cipherbox.shared.icons.filled.info
  ),
  Settings(
    label = R.string.settings,
    icon = com.github.devlotfi.cipherbox.shared.icons.outlined.settings,
    iconSelected = com.github.devlotfi.cipherbox.shared.icons.filled.settings
  ),
}

@Composable
fun DashboardScreen(
  modifier: Modifier = Modifier
) {
  var selectedTab by rememberSaveable { mutableStateOf(AppTab.Info) }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopBar(
        appName = stringResource(R.string.app_name),
        logo = SharedR.drawable.cipher_box_gateway_logo
      )
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      ) {
        AppTab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = {
              selectedTab = tab
            },
            icon = {
              Icon(
                imageVector = if (selectedTab == tab) tab.iconSelected else tab.icon,
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        AppTab.Info -> InfoScreen()
        AppTab.Settings -> SettingsScreen()
      }
    }
  }
}