package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.composables.sections.ControlSection
import com.github.devlotfi.cipherbox.gateway.composables.sections.PermissionSection
import com.github.devlotfi.cipherbox.gateway.composables.sections.ServerTestSection
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherDb
import com.github.devlotfi.cipherbox.shared.composables.LoadingView
import com.github.devlotfi.cipherbox.shared.composables.SectionTitle
import com.github.devlotfi.cipherbox.shared.global.AppQueryCache
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.flow.map

data class AppPermission(
  val permission: String,
  val title: String,
)

class InfoScreenViewModel : ViewModel() {
  val queryClient = QueryClient(viewModelScope, AppQueryCache.queryCache)
  val dataStore = DataStore()
}

@Composable
fun InfoScreen(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel()
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val localSqlCipherDb = LocalSqlCipherDb.current

  val appPermissions = remember {
    listOf(
      AppPermission(
        permission = Manifest.permission.BLUETOOTH_CONNECT,
        title = "Bluetooth",
      ),
      AppPermission(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        title = "Notifications",
      )
    )
  }
  val grantedState = remember { mutableStateMapOf<String, Boolean>() }
  val missingPermissions = appPermissions.filter { grantedState[it.permission] != true }
  val allGranted = missingPermissions.isEmpty()


  if (localSqlCipherDb.sqlCipherDbManager.db == null) throw Exception("")
  val settingsFlow = remember {
    localSqlCipherDb.sqlCipherDbManager.db!!.settingsDao().observe()
      .map { RoomFlowData(loading = false, data = it) }
  }
  val settingsFlowData by settingsFlow.collectAsStateWithLifecycle(
    initialValue = RoomFlowData(
      loading = true
    )
  )
  val settings = settingsFlowData.data ?: SettingsEntity()

  if (settingsFlowData.loading) {
    LoadingView()
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(bottom = 50.dp)
  ) {
    ControlSection(
      settings = settings,
      allGranted = allGranted
    )

    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

    SectionTitle(
      text = "Server test",
      imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.data_object,
      modifier = Modifier.padding(10.dp)
    )
    ServerTestSection(
      settings = settings
    )

    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

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