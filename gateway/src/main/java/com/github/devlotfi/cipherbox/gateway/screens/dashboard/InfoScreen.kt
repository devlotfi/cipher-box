package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.github.devlotfi.cipherbox.shared.global.AppQueryCache
import com.github.devlotfi.cipherbox.shared.utils.RoomFlowData
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.flow.map

class InfoScreenViewModel : ViewModel() {
  val queryClient = QueryClient(viewModelScope, AppQueryCache.queryCache)
  val dataStore = DataStore()
}

@Composable
fun InfoScreen(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel()
) {
  val scrollState = rememberScrollState()
  val localSqlCipherDb = LocalSqlCipherDb.current


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
      settings = settings
    )

    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    Row(
      modifier = Modifier.padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .clip(
            RoundedCornerShape(10.dp)
          )
          .background(MaterialTheme.colorScheme.primary)
          .padding(3.dp)
      ) {
        Icon(
          imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.shield,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
          tint = MaterialTheme.colorScheme.onPrimary
        )
      }
      Text("Permissions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    PermissionSection()

    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    Row(
      modifier = Modifier.padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .clip(
            RoundedCornerShape(10.dp)
          )
          .background(MaterialTheme.colorScheme.primary)
          .padding(3.dp)
      ) {
        Icon(
          imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.data_object,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
          tint = MaterialTheme.colorScheme.onPrimary
        )
      }
      Text("Server test", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
    ServerTestSection(
      settings = settings
    )
  }
}