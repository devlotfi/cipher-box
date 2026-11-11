package com.github.devlotfi.cipherbox.gateway.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.devlotfi.cipherbox.gateway.R
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
      TopAppBar(
        modifier = Modifier.clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
          titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Image(
              painter = painterResource(SharedR.drawable.cipher_box_gateway_logo),
              contentDescription = "logo",
              modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
              stringResource(SharedR.string.app_name),
              fontSize = 22.sp,
              fontWeight = FontWeight.Black
            )
          }
        }
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
        AppTab.Info -> Text("Info")
        AppTab.Settings -> Text("Settings")
      }
    }
  }
}