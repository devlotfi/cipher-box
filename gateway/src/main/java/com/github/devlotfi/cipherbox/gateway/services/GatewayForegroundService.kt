package com.github.devlotfi.cipherbox.gateway.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class GatewayForegroundService : Service() {
  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      Actions.START.toString() -> {
        startInForeground()
      }

      Actions.STOP.toString() -> {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private fun startInForeground() {
    createNotificationChannel()

    val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Service running")
      .setContentText("My foreground service is active")
      .setSmallIcon(android.R.drawable.ic_popup_sync)
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

    isRunning = true

    // TODO: do your real work here (coroutine, location updates, etc.)
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Foreground Service",
        NotificationManager.IMPORTANCE_LOW
      )
      getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    isRunning = false
    super.onDestroy()
  }

  companion object {
    private const val CHANNEL_ID = "foreground_service_channel"
    private const val NOTIFICATION_ID = 1
    var isRunning by mutableStateOf(false)
      private set
  }

  enum class Actions {
    START,
    STOP
  }
}