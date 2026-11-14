package com.github.devlotfi.cipherbox.gateway.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.github.devlotfi.cipherbox.gateway.ktor.configureServer
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import com.github.devlotfi.cipherbox.shared.R as SharedR

class GatewayForegroundService : Service() {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val serverLock = Mutex()
  private var server: EmbeddedServer<*, *>? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      Actions.START.toString() -> {
        val serverPort = intent.getIntExtra(Extra.SERVER_PORT, 8080)

        enterForeground()
        scope.launch {
          serverLock.withLock {
            startServerLocked(
              serverPort = serverPort
            )
          }
        }
      }

      Actions.RESTART.toString() -> {
        val serverPort = intent.getIntExtra(Extra.SERVER_PORT, 8080)

        enterForeground()
        scope.launch {
          serverLock.withLock {
            stopServerLocked()
            startServerLocked(serverPort)
          }
        }
      }

      Actions.STOP.toString() -> {
        scope.launch {
          serverLock.withLock { stopServerLocked() }
          withContext(Dispatchers.Main) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
          }
        }
      }
    }
    return START_NOT_STICKY
  }

  private suspend fun startServerLocked(
    serverPort: Int
  ) {
    if (server != null) return

    // Retry a few times: after a restart the OS may take a moment to free the port
    repeat(START_ATTEMPTS) { attempt ->
      try {
        server = configureServer(
          port = serverPort,
          debug = (applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        ).start(wait = false)

        isRunning = true
        return
      } catch (e: Exception) {
        Log.e(TAG, "Start attempt ${attempt + 1} failed", e)
        server = null
        delay(500.milliseconds)
      }
    }

    // All attempts failed: don't leave a fake "running" service around
    isRunning = false
    withContext(Dispatchers.Main) {
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
    }
  }

  private fun stopServerLocked() {
    val current = server ?: return
    server = null
    try {
      // Blocks until the port is released; we are on Dispatchers.IO so that's fine
      current.stop(gracePeriodMillis = 500, timeoutMillis = 2000)
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping server", e)
    }
    isRunning = false
  }

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
    isRunning = false
    val leftover = server
    server = null
    if (leftover != null) {
      Thread {
        try {
          leftover.stop(gracePeriodMillis = 500, timeoutMillis = 2000)
        } catch (e: Exception) {
          Log.e(TAG, "Error stopping server", e)
        }
      }.start()
    }

    scope.cancel()
    super.onDestroy()
  }

  companion object {
    private const val TAG = "GatewayService"
    private const val START_ATTEMPTS = 3
    private const val CHANNEL_ID = "foreground_service_channel"
    private const val NOTIFICATION_ID = 1

    var isRunning by mutableStateOf(false)
      private set
  }

  object Extra {
    const val SERVER_PORT = "SERVER_PORT"
  }

  enum class Actions {
    START,
    STOP,
    RESTART
  }
}