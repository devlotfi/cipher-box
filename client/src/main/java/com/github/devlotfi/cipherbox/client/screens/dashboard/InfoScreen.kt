package com.github.devlotfi.cipherbox.client.screens.dashboard

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.client.composables.sections.ControlSection
import com.github.devlotfi.cipherbox.client.composables.sections.PermissionSection
import com.github.devlotfi.cipherbox.shared.composables.SectionTitle
import com.github.devlotfi.cipherbox.shared.global.AppQueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composevstore.DataStore

data class AppPermission(
  val permission: String,
  val title: String,
)

class InfoScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  val queryClient = QueryClient(viewModelScope, AppQueryCache.queryCache)
}

@Composable
fun InfoScreen(
  modifier: Modifier = Modifier,
  viewModel: SettingsScreenViewModel = viewModel()
) {
  val scrollState = rememberScrollState()
  val appPermissions = remember {
    listOf(
      AppPermission(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        title = "Notifications",
      )
    )
  }
  val grantedState = remember { mutableStateMapOf<String, Boolean>() }
  val missingPermissions = appPermissions.filter { grantedState[it.permission] != true }
  val allGranted = missingPermissions.isEmpty()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(bottom = 50.dp)
  ) {
    ControlSection()

    HorizontalDivider()

    SectionTitle(
      text = "Permissions",
      imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.shield,
      modifier = Modifier.padding(10.dp)
    )
    PermissionSection(
      appPermissions = appPermissions,
      missingPermissions = missingPermissions,
      grantedState = grantedState,
      allGranted = allGranted
    )
  }
}