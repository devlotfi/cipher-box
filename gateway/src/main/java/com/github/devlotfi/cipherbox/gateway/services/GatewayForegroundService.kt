package com.github.devlotfi.cipherbox.gateway.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.github.devlotfi.cipherbox.gateway.ktor.configureServer
import com.github.devlotfi.cipherbox.shared.transport.SerialTransferLayer
import com.github.devlotfi.cipherbox.shared.transport.TransportGlobals
import io.ktor.server.engine.EmbeddedServer
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import com.github.devlotfi.cipherbox.shared.R as SharedR

class GatewayForegroundService : Service() {

  private val commands = Executors.newSingleThreadExecutor()
  private val mainHandler = Handler(Looper.getMainLooper())

  @Volatile
  private var serverThread: Thread? = null

  @Volatile
  private var serverRunningFlag = false

  @Volatile
  private var bluetoothThread: Thread? = null

  @Volatile
  private var bluetoothRunningFlag = false
  private var currentPort: Int? = null
  private var currentMac: String? = null

  private val adapter: BluetoothAdapter? by lazy {
    getSystemService(BluetoothManager::class.java)?.adapter
  }

  @Volatile
  private var socket: BluetoothSocket? = null

  @Volatile
  private var proto: SerialTransferLayer? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent == null) return START_NOT_STICKY

    val action = intent.action
    when (action) {
      Actions.START.toString() -> {
        enterForeground()

        val port = intent.getIntExtra(Extra.SERVER_PORT, DEFAULT_PORT)
        val mac = intent.getStringExtra(Extra.BLUETOOTH_MAC) ?: throw Exception("Missing mac")

        runCommand(Busy.STARTING) {
          if (serverThread?.isAlive != true || currentPort != port) startServer(port)
          if (bluetoothThread?.isAlive != true || currentMac != mac) startBluetooth(mac)
        }
      }

      Actions.RESTART.toString() -> {
        enterForeground()

        val port = intent.getIntExtra(Extra.SERVER_PORT, DEFAULT_PORT)
        val mac = intent.getStringExtra(Extra.BLUETOOTH_MAC) ?: throw Exception("Missing mac")

        runCommand(Busy.RESTARTING) {
          try {
            stopServer()
            stopBluetooth()
          } catch (e: Exception) {
          }

          if (serverThread?.isAlive != true || currentPort != port) startServer(port)
          if (bluetoothThread?.isAlive != true || currentMac != mac) startBluetooth(mac)
        }
      }

      Actions.STOP.toString() -> {
        runCommand(Busy.STOPPING, releaseWhenDone = false) {
          try {
            stopServer()
            stopBluetooth()
          } finally {
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

  private fun startServer(port: Int) {
    stopServer()
    currentPort = port
    serverRunningFlag = true
    val debug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    serverThread = thread(name = "server-loop") {
      var server: EmbeddedServer<*, *>? = null

      while (serverRunningFlag) {
        if (server == null) {
          try {
            server = configureServer(
              port = port,
              sendViaBluetooth = ::sendViaBluetooth,
              debug = debug,
            ).start(wait = false)
            isServerRunning = true
          } catch (e: Exception) {
            Log.e(TAG, "Server start failed, retrying", e)
            Thread.sleep(SERVER_RETRY_DELAY_MS)
          }
        }

        Thread.sleep(200)
      }

      isServerRunning = false
      try {
        server?.stop(gracePeriodMillis = 500, timeoutMillis = 2000)
      } catch (e: Exception) {
        Log.e(TAG, "Error stopping server", e)
      }
    }
  }

  private fun stopServer() {
    serverRunningFlag = false
    serverThread?.join()
    serverThread = null
  }

  private fun startBluetooth(mac: String) {
    stopBluetooth()
    currentMac = mac
    bluetoothRunningFlag = true
    bluetoothThread = thread(name = "bluetooth-loop") {
      while (bluetoothRunningFlag) {
        if (hasBluetoothPermission() && adapter?.isEnabled == true) {
          try {
            connectAndRead(mac)
          } catch (e: Exception) {
            Log.w(TAG, "Bluetooth session ended: ${e.message}")
          }
        }
        isBluetoothConnected = false

        Thread.sleep(BLUETOOTH_RETRY_DELAY_MS)
      }
    }
  }

  private fun stopBluetooth() {
    bluetoothRunningFlag = false
    closeSocket()
    bluetoothThread?.join()
    bluetoothThread = null
  }

  private fun sendViaBluetooth(data: String): Boolean {
    val p = proto ?: return false
    try {
      p.send(data)
      return true
    } catch (e: Exception) {
      return false
    }
  }

  private fun connectAndRead(mac: String) {
    val s = adapter!!.getRemoteDevice(mac).createRfcommSocketToServiceRecord(SPP_UUID)
    socket = s

    if (!bluetoothRunningFlag) {
      closeSocket()
      return
    }

    var layer: SerialTransferLayer? = null
    try {
      s.connect()

      layer = SerialTransferLayer(
        onTransferData = { data ->
          println(data)
        },
        transportWrite = { bytes ->
          try {
            s.outputStream.write(bytes)
            s.outputStream.flush()
          } catch (_: Exception) {
          }
        },
      )
      proto = layer
      isBluetoothConnected = true

      val buf = ByteArray(TransportGlobals.MTU)
      while (bluetoothRunningFlag) {
        val n = s.inputStream.read(buf)
        if (n < 0) break
        layer.onBytesReceived(buf, n)
      }
    } finally {
      isBluetoothConnected = false
      proto = null
      closeSocket()
      layer?.let { l ->
        thread(name = "tinyproto-close") {
          try {
            l.close()
          } catch (_: Exception) {
          }
        }
      }
    }
  }

  private fun closeSocket() {
    try {
      socket?.close()
    } catch (_: Exception) {
    }
    socket = null
  }

  private fun hasBluetoothPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
      ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
      PackageManager.PERMISSION_GRANTED

  private fun enterForeground() {
    createNotificationChannel()

    val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Gateway running")
      .setContentText("Server running")
      .setSmallIcon(SharedR.drawable.cipher_box_gateway_logo)
      .setOngoing(true)
      .build()

    ServiceCompat.startForeground(
      this,
      NOTIFICATION_ID,
      notification,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
      else 0
    )
  }

  private fun createNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      "Gateway Service",
      NotificationManager.IMPORTANCE_LOW
    )
    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  override fun onDestroy() {
    isServerRunning = false
    isBluetoothConnected = false
    commands.execute {
      stopServer()
      stopBluetooth()
    }
    commands.shutdown()
    // The service is gone: nothing can be busy any more. Tokens still held by queued
    // commands are removed again later, which is a harmless no-op.
    busyTokens.clear()
    super.onDestroy()
  }

  companion object {
    private const val TAG = "GatewayService"
    private const val DEFAULT_PORT = 8080
    private const val CHANNEL_ID = "foreground_service_channel"
    private const val NOTIFICATION_ID = 1
    private const val SERVER_RETRY_DELAY_MS = 1_000L
    private const val BLUETOOTH_RETRY_DELAY_MS = 3_000L
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // One token per running or queued command. Empty list = not busy.
    // A Compose state list, so reading isBusy in a composable recomposes it when tokens change.
    val busyTokens = mutableStateListOf<Busy>()

    val isBusy: Boolean get() = busyTokens.isNotEmpty()

    var isServerRunning by mutableStateOf(false)
      private set

    var isBluetoothConnected by mutableStateOf(false)
      private set
  }

  enum class Busy {
    STARTING,
    RESTARTING,
    STOPPING
  }

  object Extra {
    const val SERVER_PORT = "SERVER_PORT"
    const val BLUETOOTH_MAC = "BLUETOOTH_MAC"
  }

  enum class Actions {
    START,
    STOP,
    RESTART
  }
}