package com.github.devlotfi.cipherbox.client.composables.sections

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.client.screens.dashboard.AppPermission
import com.github.devlotfi.cipherbox.client.screens.dashboard.InfoScreenViewModel
import com.github.devlotfi.cipherbox.client.services.ClientForegroundService

fun isPermissionGranted(
  context: Context,
  permission: AppPermission
): Boolean {
  return ContextCompat.checkSelfPermission(context, permission.permission) ==
    PackageManager.PERMISSION_GRANTED
}

@Composable
fun PermissionSection(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel(),
  appPermissions: List<AppPermission>,
  missingPermissions: List<AppPermission>,
  grantedState: SnapshotStateMap<String, Boolean>,
  allGranted: Boolean
) {
  val context = LocalContext.current

  fun refreshPermissions() {
    appPermissions.forEach { grantedState[it.permission] = isPermissionGranted(context, it) }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refreshPermissions() }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { refreshPermissions() }

  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    appPermissions.forEach { permission ->
      val granted = grantedState[permission.permission] == true
      ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        content = { Text(permission.title) },
        trailingContent = {
          Text(
            text = if (granted) "Granted" else "Not granted",
            style = MaterialTheme.typography.labelLarge,
            color = if (granted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
          )
        }
      )
    }

    Button(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      shape = RoundedCornerShape(15.dp),
      enabled = !allGranted && !ClientForegroundService.isBusy,
      onClick = {
        permissionLauncher.launch(missingPermissions.map { it.permission }.toTypedArray())
      }
    ) {
      Text(
        if (allGranted) "All permissions granted" else "Request permissions",
        modifier = Modifier.padding(7.dp)
      )
    }
  }
}