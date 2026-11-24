package com.github.devlotfi.cipherbox.gateway.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
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
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import com.github.devlotfi.cipherbox.shared.R as SharedR

/**
 * Owns the foreground notification, the command queue and the busy state.
 * The actual work lives in GatewayServer and GatewayBluetooth.
 */
class GatewayForegroundService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val commands = Executors.newSingleThreadExecutor()
  private val mainHandler = Handler(Looper.getMainLooper())
  private lateinit var tor: GatewayTor
  private lateinit var bluetooth: GatewayBluetooth
  private lateinit var server: GatewayServer
  private lateinit var mqtt: GatewayMqtt
  private lateinit var torReceiver: GatewayTorReceiver
  private lateinit var mqttReceiver: GatewayMqttReceiver
  private lateinit var senderTor: GatewayTorSender
  private lateinit var senderMqtt: GatewayMqttSender

  override fun onCreate() {
    super.onCreate()

    tor = GatewayTor(
      context = this,
      saveOnionKey = { currentOnionKey = it },
      onReadyChanged = { isTorReady = it },
      onOnionAddressChanged = { onionAddress = it },
    )
    bluetooth = GatewayBluetooth(
      context = this,
      onConnectedChanged = { isBluetoothConnected = it },
      onOutboundGatewayPacket = {
        println("GATEWAY BLUETOOTH OUTBOUND: $it")
        when (it) {
          is OutboundGatewayPacket.DataTor -> serviceScope.launch(Dispatchers.IO) {
            senderTor.enqueue(it)
          }

          is OutboundGatewayPacket.DataMqtt -> serviceScope.launch(Dispatchers.IO) {
            senderMqtt.enqueue(it)
          }

          is OutboundGatewayPacket.Ack -> serviceScope.launch(Dispatchers.IO) {
            torReceiver.ack(it)
            mqttReceiver.ack(it)
          }
        }
      }
    )
    server = GatewayServer(
      debug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
      onRunningChanged = { isServerRunning = it },
      onTransportPacket = {
        println("GATEWAY SERVER RECEIVED: $it")
        serviceScope.launch(Dispatchers.IO) {
          torReceiver.receive(it)
        }
      },
    )
    mqtt = GatewayMqtt(
      onConnectedChanged = { isMqttConnected = it },
      onTransportPacket = {
        println("GATEWAY MQTT RECEIVED: $it")
        serviceScope.launch(Dispatchers.IO) {
          mqttReceiver.receive(it)
        }
      }
    )
    torReceiver = GatewayTorReceiver(
      onBluetoothSend = {
        bluetooth.send(it)
      },
    )
    mqttReceiver = GatewayMqttReceiver(
      onBluetoothSend = {
        println("GATEWAY BLUETOOTH INBOUND: $it")
        bluetooth.send(it)
      },
      onMqttSend = { topic, data ->
        println("GATEWAY MQTT OUTBOUND (RECEIVER): $data")
        mqtt.send(topic, data)
      }
    )
    senderTor = GatewayTorSender(
      onBluetoothSend = {
        bluetooth.send(it)
      }
    )
    senderMqtt = GatewayMqttSender(
      onMqttSend = { topic, data ->
        println("GATEWAY MQTT OUTBOUND (SENDER): $data")
        mqtt.send(topic, data)
      }
    )
  }

  override fun onBind(intent: Intent?): IBinder? = null

  data class IntentExtras(
    val onionKey: String?,
    val torRc: String?,
    val bluetoothMac: String,
    val mqttHost: String,
    val mqttPort: Int = 8883,
    val mqttUsername: String? = null,
    val mqttPassword: String? = null,
    val mqttTopic: String
  ) {
    fun toMqttConfig(): GatewayMqtt.Config = GatewayMqtt.Config(
      host = mqttHost,
      port = mqttPort,
      topic = mqttTopic,
      username = mqttUsername,
      password = mqttPassword
    )
  }

  fun getIntentExtras(intent: Intent): IntentExtras = IntentExtras(
    onionKey = intent.getStringExtra(Extra.ONION_KEY.toString()),
    torRc = intent.getStringExtra(Extra.TOR_RC.toString()),
    bluetoothMac = intent.getStringExtra(Extra.BLUETOOTH_MAC.toString())
      ?: throw Exception("Missing data"),
    mqttHost = intent.getStringExtra(Extra.MQTT_HOST.toString())
      ?: throw Exception("Missing data"),
    mqttPort = intent.getIntExtra(Extra.MQTT_PORT.toString(), DEFAULT_MQTT_PORT),
    mqttUsername = intent.getStringExtra(Extra.MQTT_USERNAME.toString()),
    mqttPassword = intent.getStringExtra(Extra.MQTT_PASSWORD.toString()),
    mqttTopic = intent.getStringExtra(Extra.MQTT_TOPIC.toString())
      ?: throw Exception("Missing data")
  )

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent == null) return START_NOT_STICKY

    val action = intent.action
    when (action) {
      Actions.START.toString() -> {
        enterForeground()

        val intentExtras = getIntentExtras(intent)

        runCommand(Busy.STARTING) {
          if (!tor.isAlive) tor.start(
            torRc = intentExtras.torRc,
            onionKey = intentExtras.onionKey
          )
          if (!server.isAlive) server.start()
          if (!bluetooth.isAlive) bluetooth.start(
            intentExtras.bluetoothMac
          )
          if (!mqtt.isAlive) mqtt.start(intentExtras.toMqttConfig())
          torReceiver.start()
          mqttReceiver.start(intentExtras.mqttTopic)
          senderTor.start()
          senderMqtt.start(intentExtras.mqttTopic)
        }
      }

      Actions.RESTART.toString() -> {
        enterForeground()

        val intentExtras = getIntentExtras(intent)

        runCommand(Busy.RESTARTING) {
          try {
            tor.stop()
            server.stop()
            bluetooth.stop()
            mqtt.stop()
            torReceiver.stop()
            mqttReceiver.stop()
            senderTor.stop()
            senderMqtt.stop()
          } catch (e: Exception) {
          }

          tor.start(
            torRc = intentExtras.torRc,
            onionKey = intentExtras.onionKey
          )
          server.start()
          bluetooth.start(intentExtras.bluetoothMac)
          mqtt.start(intentExtras.toMqttConfig())
          torReceiver.start()
          mqttReceiver.start(intentExtras.mqttTopic)
          senderTor.start()
          senderMqtt.start(intentExtras.mqttTopic)
        }
      }

      Actions.STOP.toString() -> {
        runCommand(Busy.STOPPING, releaseWhenDone = false) {
          try {
            server.stop()
            bluetooth.stop()
            tor.stop()
            mqtt.stop()
            torReceiver.stop()
            mqttReceiver.stop()
            senderTor.stop()
            senderMqtt.stop()
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

  private fun enterForeground() {
    createNotificationChannel()

    val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Gateway running")
      .setContentText("Gateway services are active")
      .setSmallIcon(SharedR.drawable.cipher_box_gateway_logo)
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
      "Gateway Service",
      NotificationManager.IMPORTANCE_LOW
    )
    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  override fun onDestroy() {
    serviceScope.cancel()

    isTorReady = false
    isServerRunning = false
    isBluetoothConnected = false

    commands.execute {
      tor.stop()
      server.stop()
      bluetooth.stop()
    }

    commands.shutdown()
    // The service is gone: nothing can be busy any more. Tokens still held by queued
    // commands are removed again later, which is a harmless no-op.
    busyTokens.clear()
    super.onDestroy()
  }

  enum class Busy {
    STARTING,
    RESTARTING,
    STOPPING
  }

  enum class Extra {
    ONION_KEY,
    TOR_RC,
    SERVER_PORT,
    GATEWAY_KEY_HASH,
    BLUETOOTH_MAC,
    MQTT_HOST,
    MQTT_PORT,
    MQTT_TOPIC,
    MQTT_USERNAME,
    MQTT_PASSWORD
  }

  enum class Actions {
    START,
    STOP,
    RESTART
  }

  companion object {
    private const val TAG = "GatewayService"
    private const val DEFAULT_SERVER_PORT = 8080
    private const val DEFAULT_MQTT_PORT = 8080
    private const val CHANNEL_ID = "foreground_service_channel"
    private const val NOTIFICATION_ID = 1

    // One token per running or queued command. Empty list = not busy.
    // A Compose state list, so reading isBusy in a composable recomposes it when tokens change.
    val busyTokens = mutableStateListOf<Busy>()

    val isBusy: Boolean get() = busyTokens.isNotEmpty()

    var onionAddress by mutableStateOf<String?>(null)
      private set

    var isTorReady by mutableStateOf(false)
      private set

    var isServerRunning by mutableStateOf(false)
      private set

    var isBluetoothConnected by mutableStateOf(false)
      private set

    var isMqttConnected by mutableStateOf(false)
      private set

    var currentOnionKey by mutableStateOf<String?>(null)
      private set

    fun putIntentExtras(intent: Intent, intentExtras: IntentExtras) {
      intent.apply {
        putExtra(Extra.ONION_KEY.toString(), intentExtras.onionKey)
        putExtra(Extra.TOR_RC.toString(), intentExtras.torRc)
        putExtra(Extra.BLUETOOTH_MAC.toString(), intentExtras.bluetoothMac)
        putExtra(Extra.MQTT_HOST.toString(), intentExtras.mqttHost)
        putExtra(Extra.MQTT_PORT.toString(), intentExtras.mqttPort)
        putExtra(Extra.MQTT_USERNAME.toString(), intentExtras.mqttUsername)
        putExtra(Extra.MQTT_PASSWORD.toString(), intentExtras.mqttPassword)
        putExtra(Extra.MQTT_TOPIC.toString(), intentExtras.mqttTopic)
      }
    }
  }
}