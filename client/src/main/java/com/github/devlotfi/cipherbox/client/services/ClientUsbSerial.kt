package com.github.devlotfi.cipherbox.client.services


import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.SerialTransferLayer
import com.github.devlotfi.cipherbox.shared.transport.packet.InboundGatewayPacket
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlin.concurrent.thread

/**
 * Keeps a USB serial connection to the first USB serial device it finds alive on its own thread:
 * find device, connect, read until the link drops (unplug, error), wait, try again, forever until
 * stop() is called.
 *
 * start() and stop() must only be called from one thread at a time (the service guarantees this
 * by running every command on its single commands thread). send() can be called from any thread.
 */
class ClientUsbSerial(
  context: Context,
  private val onConnectedChanged: (Boolean) -> Unit,
  // Human readable, e.g. "No USB serial device found"
  private val onStatusChanged: (String) -> Unit,
  private val onInboundGatewayPacket: (InboundGatewayPacket) -> Unit,
) {
  private val context = context.applicationContext

  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  @Volatile
  private var proto: SerialTransferLayer? = null

  // Device we already showed the permission dialog for, so we don't re-ask every few seconds
  private var askedPermissionForDeviceId: Int? = null

  val isAlive: Boolean get() = loopThread?.isAlive == true

  // Lazy: only touched once the service is running, never in a constructor
  private val usbManager: UsbManager by lazy {
    context.getSystemService(Context.USB_SERVICE) as UsbManager
  }

  fun start() {
    stop()
    running = true

    loopThread = thread(name = "usb-serial-loop") {
      while (running) {
        try {
          connectAndRead() // blocks until the connection drops (or returns at once if no device)
        } catch (e: Exception) {
          Log.w(TAG, "USB session ended: ${e.message}")
          onStatusChanged("Connection lost: ${e.message}")
        }
        onConnectedChanged(false)

        if (!running) break
        Thread.sleep(RETRY_DELAY_MS)
      }
      onStatusChanged("Disconnected")
    }
  }

  fun stop() {
    // No socket to close here: connectAndRead() reads with a short timeout,
    // so it notices the flag by itself within READ_TIMEOUT_MS.
    running = false
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

  private fun connectAndRead() {
    val driver = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager).firstOrNull()
    if (driver == null) {
      onStatusChanged("No USB serial device found")
      return
    }

    val device = driver.device
    if (!usbManager.hasPermission(device)) {
      onStatusChanged("USB permission required")
      requestPermissionOnce(device)
      return // the loop checks again in RETRY_DELAY_MS, and sees the permission once granted
    }

    val connection = usbManager.openDevice(device)
    if (connection == null) {
      onStatusChanged("Could not open device")
      return
    }

    val port = driver.ports[0]
    var layer: SerialTransferLayer? = null
    try {
      port.open(connection)
      port.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

      // Must exist before we start reading
      layer = SerialTransferLayer(
        onTransferData = { data ->
          try {
            val inboundGatewayPacket =
              JsonInstances.jsonInstance.decodeFromString<InboundGatewayPacket>(data)
            onInboundGatewayPacket(inboundGatewayPacket)
          } catch (e: Exception) {
            Log.e(TAG, "onMessage failed", e)
          }
        },
        transportWrite = { bytes ->
          try {
            port.write(bytes, WRITE_TIMEOUT_MS)
          } catch (_: Exception) {
            // link is gone; the read loop below throws and ends the session
          }
        },
      )
      proto = layer
      onConnectedChanged(true)
      onStatusChanged("Connected: ${device.productName ?: device.deviceName}")

      val buf = ByteArray(READ_BUFFER_SIZE)
      while (running) {
        // Returns after READ_TIMEOUT_MS with 0 if nothing arrived; throws if the device is gone
        val n = port.read(buf, READ_TIMEOUT_MS)
        if (n > 0) layer.onBytesReceived(buf, n)
      }
    } finally {
      onConnectedChanged(false)
      proto = null
      try {
        port.close()
      } catch (_: Exception) {
      }
      try {
        connection.close() // port.close() does this too, but not if open() failed
      } catch (_: Exception) {
      }
      // close() joins the tx thread and may wait on a blocked send, so keep it off this thread
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

  private fun requestPermissionOnce(device: UsbDevice) {
    if (askedPermissionForDeviceId == device.deviceId) return
    askedPermissionForDeviceId = device.deviceId

    // Must be mutable so the system can add the EXTRA_PERMISSION_GRANTED extra.
    // Nobody listens for the result: the loop polls hasPermission() instead.
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      0,
      Intent(ACTION_USB_PERMISSION).setPackage(context.packageName),
      PendingIntent.FLAG_MUTABLE
    )
    usbManager.requestPermission(device, pendingIntent)
  }

  private companion object {
    const val TAG = "ClientUsbSerial"
    const val ACTION_USB_PERMISSION = "com.github.devlotfi.cipherbox.client.USB_PERMISSION"
    const val BAUD_RATE = 921600
    const val READ_BUFFER_SIZE = 4096
    const val READ_TIMEOUT_MS = 200
    const val WRITE_TIMEOUT_MS = 2000
    const val RETRY_DELAY_MS = 2_000L
  }
}