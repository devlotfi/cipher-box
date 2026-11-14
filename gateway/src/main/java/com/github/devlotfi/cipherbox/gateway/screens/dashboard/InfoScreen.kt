package com.github.devlotfi.cipherbox.gateway.screens.dashboard

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.github.devlotfi.cipherbox.gateway.services.GatewayForegroundService

@Composable
fun InfoScreen(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Android 13+ needs runtime permission for the notification to show
  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {
    println(it)
  }

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    Text("Info screen")
    Text("Is running: ${GatewayForegroundService.isRunning}")
    Button(
      onClick = {
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    ) {
      Text("Request permission")
    }

    Button(
      onClick = {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.START.toString())
        ContextCompat.startForegroundService(context, intent)
      }
    ) {
      Text("Start")
    }

    Button(
      onClick = {
        val intent = Intent(context, GatewayForegroundService::class.java)
          .setAction(GatewayForegroundService.Actions.STOP.toString())
        context.startService(intent)
      }
    ) {
      Text("Stop")
    }
  }
}