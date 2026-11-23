package com.github.devlotfi.cipherbox.client

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
import com.github.devlotfi.cipherbox.client.services.ClientForegroundService
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.packet.Level2CipherPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import java.util.UUID

@Composable
fun Main(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { println(it) }

  Column(
    modifier = modifier.fillMaxSize()
  ) {
    Text("busy: ${ClientForegroundService.isBusy}")
    Text("status: ${ClientForegroundService.usbStatus}")
    Text("connected: ${ClientForegroundService.isUsbConnected}")

    Button(onClick = {
      permissionLauncher.launch(
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
      )
    }) {
      Text("permissions")
    }

    Button(onClick = {
      val intent = Intent(context, ClientForegroundService::class.java)
        .setAction(ClientForegroundService.Actions.START.toString())
      context.startService(intent)
    }) {
      Text("start")
    }

    Button(onClick = {
      val intent = Intent(context, ClientForegroundService::class.java)
        .setAction(ClientForegroundService.Actions.RESTART.toString())
      context.startService(intent)
    }) {
      Text("restart")
    }

    Button(onClick = {
      val intent = Intent(context, ClientForegroundService::class.java)
        .setAction(ClientForegroundService.Actions.STOP.toString())
      context.startService(intent)
    }) {
      Text("stop")
    }

    Button(onClick = {
      val outboundGatewayPacket = OutboundGatewayPacket.DataMqtt(
        topic = "ffedbf40-01fb-46c9-bb58-1df1c6eb62f6",
        requestId = UUID.randomUUID().toString(),
        level2CipherPacket = Level2CipherPacket(
          mlKemData = Level2CipherPacket.MLKEMData(
            encapsulation = "test"
          ),
          hkdfData = Level2CipherPacket.HKDFData(
            salt = "test",
            info = "test"
          ),
          aesGcmData = Level2CipherPacket.AESGCMData(
            ciphertext = "lol",
            iv = "lol"
          )
        )
      )
      val rawOutboundGatewayPacket =
        JsonInstances.jsonInstance.encodeToString<OutboundGatewayPacket>(outboundGatewayPacket)
      println("Payload: $rawOutboundGatewayPacket")
      ClientForegroundService.send(rawOutboundGatewayPacket)
    }) {
      Text("send")
    }
  }
}