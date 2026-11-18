package com.github.devlotfi.cipherbox.gateway.composables.sections

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.composables.ActionButton
import com.github.devlotfi.cipherbox.gateway.composables.CardStatus
import com.github.devlotfi.cipherbox.gateway.composables.StatusCard
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.screens.dashboard.InfoScreenViewModel
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService

@Composable
fun ControlSection(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel(),
  settings: SettingsEntity
) {
  val context = LocalContext.current

  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      StatusCard(
        modifier = Modifier.weight(1f),
        title = "Server",
        status = when {
          GatewayForegroundService.busyTokens.contains(GatewayForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
          GatewayForegroundService.isServerRunning -> CardStatus.ACTIVE
          else -> CardStatus.INACTIVE
        },
        activeIcon = com.github.devlotfi.cipherbox.shared.icons.outlined.public,
        inactiveIcon = com.github.devlotfi.cipherbox.shared.icons.outlined.public_off
      )

      StatusCard(
        modifier = Modifier.weight(1f),
        title = "Bluetooth",
        status = when {
          GatewayForegroundService.busyTokens.contains(GatewayForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
          GatewayForegroundService.isBluetoothConnected -> CardStatus.ACTIVE
          else -> CardStatus.INACTIVE
        },
        activeIcon = com.github.devlotfi.cipherbox.shared.icons.outlined.bluetooth,
        inactiveIcon = com.github.devlotfi.cipherbox.shared.icons.outlined.bluetooth_disabled
      )
    }

    Row(
      modifier = Modifier.padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Stop",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.stop,
        enabled = !GatewayForegroundService.isBusy,
        onClick = {
          val intent = Intent(context, GatewayForegroundService::class.java)
            .setAction(GatewayForegroundService.Actions.STOP.toString())
          context.startService(intent)
        }
      )

      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Restart",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.refresh,
        enabled = !GatewayForegroundService.isBusy,
        onClick = {
          val intent = Intent(context, GatewayForegroundService::class.java)
            .setAction(GatewayForegroundService.Actions.RESTART.toString()).apply {
              putExtra(GatewayForegroundService.Extra.SERVER_PORT, settings.serverPort)
              putExtra(GatewayForegroundService.Extra.BLUETOOTH_MAC, settings.bluetoothClientMac)
            }
          ContextCompat.startForegroundService(context, intent)
        }
      )

      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Start",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.play_arrow,
        enabled = !GatewayForegroundService.isBusy,
        onClick = {
          val intent = Intent(context, GatewayForegroundService::class.java)
            .setAction(GatewayForegroundService.Actions.START.toString()).apply {
              putExtra(GatewayForegroundService.Extra.SERVER_PORT, settings.serverPort)
              putExtra(GatewayForegroundService.Extra.BLUETOOTH_MAC, settings.bluetoothClientMac)
            }
          ContextCompat.startForegroundService(context, intent)
        }
      )
    }
  }
}