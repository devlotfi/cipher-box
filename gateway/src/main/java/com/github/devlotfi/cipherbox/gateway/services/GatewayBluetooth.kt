package com.github.devlotfi.cipherbox.gateway.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.devlotfi.cipherbox.shared.transport.SerialTransferLayer
import com.github.devlotfi.cipherbox.shared.transport.TransportGlobals
import java.util.UUID
import kotlin.concurrent.thread

/**
 * Keeps a Bluetooth (SPP) connection to one device alive on its own thread:
 * connect, read until the link drops, wait, reconnect, forever until stop() is called.
 *
 * start() and stop() must only be called from one thread at a time (the service guarantees this
 * by running every command on its single commands thread). send() can be called from any thread.
 */
class GatewayBluetooth(
  private val context: Context,
  private val onConnectedChanged: (Boolean) -> Unit,
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  @Volatile
  private var socket: BluetoothSocket? = null

  @Volatile
  private var proto: SerialTransferLayer? = null

  var currentMac: String? = null
    private set

  val isAlive: Boolean get() = loopThread?.isAlive == true

  // Lazy: only touched once the service is running, never in a constructor
  private val adapter: BluetoothAdapter? by lazy {
    context.getSystemService(BluetoothManager::class.java)?.adapter
  }

  fun start(mac: String) {
    stop()
    currentMac = mac
    running = true

    loopThread = thread(name = "bluetooth-loop") {
      while (running) {
        if (hasPermission() && adapter?.isEnabled == true) {
          try {
            connectAndRead(mac)
          } catch (e: Exception) {
            Log.w(TAG, "Bluetooth session ended: ${e.message}")
          }
        }
        onConnectedChanged(false)

        Thread.sleep(RETRY_DELAY_MS)
      }
    }
  }

  fun stop() {
    running = false
    closeSocket()
    loopThread?.join()
    loopThread = null
  }

  /** Returns false if there is no connection right now or the send failed. */
  fun send(data: String): Boolean {
    val p = proto ?: return false // read the CURRENT layer on every call
    return try {
      p.send(data)
      true
    } catch (e: Exception) {
      false
    }
  }

  private fun connectAndRead(mac: String) {
    val s = adapter!!.getRemoteDevice(mac).createRfcommSocketToServiceRecord(SPP_UUID)
    socket = s

    if (!running) {
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
      onConnectedChanged(true)

      val buf = ByteArray(TransportGlobals.MTU)
      while (running) {
        val n = s.inputStream.read(buf)
        if (n < 0) break
        layer.onBytesReceived(buf, n)
      }
    } finally {
      onConnectedChanged(false)
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

  // BLUETOOTH_CONNECT is a runtime permission from Android 12 (API 31)
  private fun hasPermission(): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
      PackageManager.PERMISSION_GRANTED

  private companion object {
    const val TAG = "GatewayBluetooth"
    const val RETRY_DELAY_MS = 3_000L
    val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
  }
}