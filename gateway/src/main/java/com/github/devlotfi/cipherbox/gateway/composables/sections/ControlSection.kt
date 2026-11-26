package com.github.devlotfi.cipherbox.gateway.composables.sections

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.composables.ActionButton
import com.github.devlotfi.cipherbox.gateway.composables.CardStatus
import com.github.devlotfi.cipherbox.gateway.composables.StatusCard
import com.github.devlotfi.cipherbox.gateway.data.settings.SettingsEntity
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherDb
import com.github.devlotfi.cipherbox.gateway.screens.dashboard.InfoScreenViewModel
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ControlSection(
  modifier: Modifier = Modifier,
  viewModel: InfoScreenViewModel = viewModel(),
  settings: SettingsEntity,
  allGranted: Boolean,
) {
  val context = LocalContext.current
  val localSqlCipherDb = LocalSqlCipherDb.current

  SideEffect(GatewayForegroundService.currentOnionKey) {
    if (GatewayForegroundService.currentOnionKey == null) return@SideEffect
    val settingsCopy = settings.copy(
      onionKey = GatewayForegroundService.currentOnionKey
    )
    viewModel.viewModelScope.launch(Dispatchers.IO) {
      localSqlCipherDb.sqlCipherDbManager.db?.settingsDao()?.save(settingsCopy)
    }
  }

  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (settings.isMissingSettings()) Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.error
        )
      ) {
        Row(
          modifier = Modifier.padding(15.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = com.github.devlotfi.cipherbox.shared.icons.outlined.info,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
          )
          Text("Some settings are missing")
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StatusCard(
          modifier = Modifier.weight(1f),
          title = "Service",
          status = when {
            GatewayForegroundService.busyTokens.contains(GatewayForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
            GatewayForegroundService.isForegroundServiceActive -> CardStatus.ACTIVE
            else -> CardStatus.INACTIVE
          },
          icon = com.github.devlotfi.cipherbox.shared.icons.outlined.memory,
        )
      }
      Row(
        modifier = Modifier
          .fillMaxWidth(),
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
          icon = com.github.devlotfi.cipherbox.shared.icons.outlined.public,
        )

        StatusCard(
          modifier = Modifier.weight(1f),
          title = "Bluetooth",
          status = when {
            GatewayForegroundService.busyTokens.contains(GatewayForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
            GatewayForegroundService.isBluetoothConnected -> CardStatus.ACTIVE
            else -> CardStatus.INACTIVE
          },
          icon = com.github.devlotfi.cipherbox.shared.icons.outlined.bluetooth,
        )
      }
      Row(
        modifier = Modifier
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StatusCard(
          modifier = Modifier.weight(1f),
          title = "Tor",
          status = when {
            GatewayForegroundService.busyTokens.contains(GatewayForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
            GatewayForegroundService.isTorReady -> CardStatus.ACTIVE
            else -> CardStatus.INACTIVE
          },
          icon = com.github.devlotfi.cipherbox.shared.icons.outlined.tor,
        )

        StatusCard(
          modifier = Modifier.weight(1f),
          title = "MQTT",
          status = when {
            GatewayForegroundService.busyTokens.contains(GatewayForegroundService.Busy.RESTARTING) -> CardStatus.RESTARTING
            GatewayForegroundService.isMqttConnected -> CardStatus.ACTIVE
            else -> CardStatus.INACTIVE
          },
          icon = com.github.devlotfi.cipherbox.shared.icons.outlined.mqtt,
        )
      }
    }

    Row(
      modifier = Modifier.padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Stop",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.stop,
        enabled = !GatewayForegroundService.isBusy && !settings.isMissingSettings() && allGranted,
        onClick = {
          if (settings.isMissingSettings()) return@ActionButton
          val intent = Intent(context, GatewayForegroundService::class.java)
            .setAction(GatewayForegroundService.Actions.STOP.toString())
          context.startService(intent)
        }
      )

      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Restart",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.refresh,
        enabled = !GatewayForegroundService.isBusy && !settings.isMissingSettings() && allGranted,
        onClick = {
          if (settings.isMissingSettings()) return@ActionButton
          val intent = Intent(context, GatewayForegroundService::class.java)
            .setAction(GatewayForegroundService.Actions.RESTART.toString())
          GatewayForegroundService.putIntentExtras(
            intent = intent,
            intentExtras = settings.toIntentExtras()
          )
          ContextCompat.startForegroundService(context, intent)
        }
      )

      ActionButton(
        modifier = Modifier.weight(1f),
        title = "Start",
        icon = com.github.devlotfi.cipherbox.shared.icons.outlined.play_arrow,
        enabled = !GatewayForegroundService.isBusy && !settings.isMissingSettings() && allGranted,
        onClick = {
          if (settings.isMissingSettings()) return@ActionButton
          val intent = Intent(context, GatewayForegroundService::class.java)
            .setAction(GatewayForegroundService.Actions.START.toString())
          GatewayForegroundService.putIntentExtras(
            intent = intent,
            intentExtras = settings.toIntentExtras()
          )
          ContextCompat.startForegroundService(context, intent)
        }
      )
    }
  }
}