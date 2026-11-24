package com.github.devlotfi.cipherbox.client.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.github.devlotfi.cipherbox.shared.crypto.SHAUtils
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.packet.InboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import java.util.concurrent.Executors
import com.github.devlotfi.cipherbox.shared.R as SharedR

/**
 * Owns the foreground notification, the command queue and the busy state.
 * The actual work lives in ClientUsbSerial.
 */
class ClientForegroundService : Service() {

  private val commands = Executors.newSingleThreadExecutor()
  private val mainHandler = Handler(Looper.getMainLooper())
  private lateinit var usb: ClientUsbSerial

  override fun onCreate() {
    super.onCreate()

    usb = ClientUsbSerial(
      context = this,
      onConnectedChanged = { isUsbConnected = it },
      onStatusChanged = { usbStatus = it },
      onInboundGatewayPacket = {
        if (it is InboundGatewayPacket.Data) {
          println("Recived hash: ${SHAUtils.hash(it.level2CipherPacket.mlKemData.encapsulation)}")
        }
        val raw = JsonInstances.jsonInstance.encodeToString<InboundGatewayPacket>(it)
        println("CLIENT Recived by serial: $raw")
        if (it !is InboundGatewayPacket.Ack) {
          val ack = OutboundGatewayPacket.Ack(
            requestId = it.requestId
          )
          val rawAck = JsonInstances.jsonInstance.encodeToString<OutboundGatewayPacket>(ack)
          usb.send(rawAck)
        }
      },
    )
    usbInstance = usb
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent == null) return START_NOT_STICKY

    val action = intent.action
    when (action) {
      Actions.START.toString() -> {
        // startForegroundService() demands startForeground() within seconds,
        // so do it before anything that can fail or return early.
        enterForeground()

        runCommand(Busy.STARTING) {
          if (!usb.isAlive) usb.start()
        }
      }

      Actions.RESTART.toString() -> {
        enterForeground()

        runCommand(Busy.RESTARTING) {
          try {
            usb.stop()
          } catch (e: Exception) {
          }

          if (!usb.isAlive) usb.start()
        }
      }

      Actions.STOP.toString() -> {
        // The STOPPING token is not released here: it stays until the service is really
        // destroyed (onDestroy clears it), so the UI can't start anything in between.
        runCommand(Busy.STOPPING, releaseWhenDone = false) {
          try {
            usb.stop()
          } finally {
            // Always shut the service down, even if stopping threw
            mainHandler.post {
              stopForeground(STOP_FOREGROUND_REMOVE)
              stopSelf()
            }
          }
        }
      }
    }
    return START_NOT_STICKY
  }

  // Runs a command on the commands thread and keeps a busy token for as long as it takes.
  // The token is added here, on the calling (main) thread, so the UI sees "busy" immediately,
  // not only once the commands thread gets around to the command.
  private fun runCommand(operation: Busy, releaseWhenDone: Boolean = true, block: () -> Unit) {
    if (isBusy) {
      Log.w(TAG, "Busy ($busyTokens), ignoring $operation")
      return
    }
    busyTokens.add(operation)
    commands.execute {
      try {
        block()
      } catch (e: Exception) {
        Log.e(TAG, "$operation failed", e)
      } finally {
        // finally: the token is released even if the command threw
        if (releaseWhenDone) busyTokens.remove(operation)
      }
    }
  }

  private fun enterForeground() {
    createNotificationChannel()

    val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Client running")
      .setContentText("USB serial connection active")
      .setSmallIcon(SharedR.drawable.cipher_box_logo)
      .setOngoing(true)
      .build()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      ServiceCompat.startForeground(
        this,
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
      )
    } else {
      ServiceCompat.startForeground(
        this,
        NOTIFICATION_ID,
        notification,
        0
      )
    }
  }

  private fun createNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      "Client Service",
      NotificationManager.IMPORTANCE_LOW
    )
    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  override fun onDestroy() {
    usbInstance = null
    isUsbConnected = false
    usbStatus = "Disconnected"
    commands.execute {
      usb.stop()
    }
    commands.shutdown()
    // The service is gone: nothing can be busy any more. Tokens still held by queued
    // commands are removed again later, which is a harmless no-op.
    busyTokens.clear()
    super.onDestroy()
  }

  companion object {
    private const val TAG = "ClientService"
    private const val CHANNEL_ID = "client_service_channel"
    private const val NOTIFICATION_ID = 2

    // The running service's USB manager, so the UI can send without holding the service
    @Volatile
    private var usbInstance: ClientUsbSerial? = null

    /** Sends a message to the device. Returns false if not connected or the send failed. */
    fun send(data: String): Boolean = usbInstance?.send(data) ?: false

    // One token per running or queued command. Empty list = not busy.
    // A Compose state list, so reading isBusy in a composable recomposes it when tokens change.
    val busyTokens = mutableStateListOf<Busy>()

    val isBusy: Boolean get() = busyTokens.isNotEmpty()

    var isUsbConnected by mutableStateOf(false)
      private set

    var usbStatus by mutableStateOf("Disconnected")
      private set
  }

  enum class Busy {
    STARTING,
    RESTARTING,
    STOPPING
  }

  enum class Actions {
    START,
    STOP,
    RESTART
  }
}