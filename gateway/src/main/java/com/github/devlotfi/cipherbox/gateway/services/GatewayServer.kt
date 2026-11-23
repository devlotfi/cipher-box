package com.github.devlotfi.cipherbox.gateway.services

import android.util.Log
import com.github.devlotfi.cipherbox.gateway.ktor.configureServer
import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import io.ktor.server.engine.EmbeddedServer
import kotlin.concurrent.thread

/**
 * Runs the Ktor server on its own thread and keeps retrying to start it until stop() is called.
 *
 * start() and stop() must only be called from one thread at a time (the service guarantees this
 * by running every command on its single commands thread).
 */
class GatewayServer(
  private val debug: Boolean,
  private val onTransportPacket: (TransportPacket.DataTor) -> Unit,
  private val onRunningChanged: (Boolean) -> Unit,
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  val isAlive: Boolean get() = loopThread?.isAlive == true

  fun start() {
    stop()
    running = true

    loopThread = thread(name = "server-loop") {
      var server: EmbeddedServer<*, *>? = null

      while (running) {
        if (server == null) {
          try {
            server = configureServer(
              onTransportPacket = onTransportPacket,
              debug = debug,
            ).start(wait = false)
            onRunningChanged(true)
          } catch (e: Exception) {
            Log.e(TAG, "Server start failed, retrying", e)
            Thread.sleep(RETRY_DELAY_MS)
          }
        }

        Thread.sleep(200)
      }

      onRunningChanged(false)
      try {
        server?.stop(gracePeriodMillis = 500, timeoutMillis = 2000)
      } catch (e: Exception) {
        Log.e(TAG, "Error stopping server", e)
      }
    }
  }

  fun stop() {
    running = false
    loopThread?.join()
    loopThread = null
  }

  private companion object {
    const val TAG = "GatewayServer"
    const val RETRY_DELAY_MS = 1_000L
  }
}