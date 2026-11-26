package com.github.devlotfi.cipherbox.client.composables.sections

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
import com.github.devlotfi.cipherbox.client.composables.ActionButton
import com.github.devlotfi.cipherbox.client.composables.CardStatus
import com.github.devlotfi.cipherbox.client.composables.StatusCard
import com.github.devlotfi.cipherbox.client.screens.dashboard.InfoScreenViewModel
import com.github.devlotfi.cipherbox.client.services.ClientForegroundService

@Composable
fun ControlSection(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel(),
) {
  val context = LocalContext.current

  Column(
    modifier = modifier
      .fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      StatusCard(
        modifier = Modifier.weight(1f),
        title = "Relay",
        status = when {
          ClientForegroundService.busyTokens.contains(ClientForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
          ClientForegroundService.isUsbConnected -> CardStatus.ACTIVE
          else -> CardStatus.INACTIVE
        },
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.usb,
      )

      StatusCard(
        modifier = Modifier.weight(1f),
        title = "Service",
        status = when {
          ClientForegroundService.busyTokens.contains(ClientForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
          ClientForegroundService.isForegroundServiceActive -> CardStatus.ACTIVE
          else -> CardStatus.INACTIVE
        },
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.memory,
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
        enabled = !ClientForegroundService.isBusy,
        onClick = {
          val intent = Intent(context, ClientForegroundService::class.java)
            .setAction(ClientForegroundService.Actions.STOP.toString())
          context.startService(intent)
        }
      )

      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Restart",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.refresh,
        enabled = !ClientForegroundService.isBusy,
        onClick = {
          val intent = Intent(context, ClientForegroundService::class.java)
            .setAction(ClientForegroundService.Actions.RESTART.toString())
          ContextCompat.startForegroundService(context, intent)
        }
      )

      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Start",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.play_arrow,
        enabled = !ClientForegroundService.isBusy,
        onClick = {
          val intent = Intent(context, ClientForegroundService::class.java)
            .setAction(ClientForegroundService.Actions.START.toString())
          ContextCompat.startForegroundService(context, intent)
        }
      )
    }
  }
}