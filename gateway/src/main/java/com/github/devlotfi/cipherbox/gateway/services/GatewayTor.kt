package com.github.devlotfi.cipherbox.gateway.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import net.freehaven.tor.control.TorControlCommands
import net.freehaven.tor.control.TorControlConnection
import org.torproject.jni.TorService
import kotlin.concurrent.thread

/**
 * Keeps tor-android's TorService running and publishes the local Ktor server as an onion service,
 * all on its own thread:
 * write the torrc, bind (which starts tor), wait for the control connection, wait for bootstrap to
 * reach 100%, publish the onion service, then keep polling. If the binding drops, tor reports an
 * error, or tor never finishes bootstrapping, it is unbound and bound again, forever until stop()
 * is called.
 *
 * The onion service is ephemeral (tor keeps it only while our control connection lives), so it is
 * published again every time tor becomes ready. The private key is kept through
 * loadOnionKey/saveOnionKey, which gives the same .onion address after every restart.
 *
 * start() and stop() must only be called from one thread at a time (the service guarantees this
 * by running every command on its single commands thread). Never call them from the main thread:
 * stop() joins the loop thread.
 */
class GatewayTor(
  private val context: Context,
  private val saveOnionKey: (String) -> Unit,
  private val onReadyChanged: (Boolean) -> Unit,
  private val onOnionAddressChanged: (String?) -> Unit = {},
  private val onError: (String) -> Unit = {},
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  // Latest status broadcast by TorService (TorService.STATUS_OFF / STARTING / ON / STOPPING)
  @Volatile
  private var torStatus: String = TorService.STATUS_OFF

  var currentOnionKey: String? = null
    private set

  // Set when TorService broadcasts ACTION_ERROR (for example a torrc that tor rejects)
  @Volatile
  private var failed = false

  @Volatile
  private var torService: TorService? = null

  @Volatile
  private var bound = false

  /** The torrc text tor was (or is being) started with. Null or blank = no custom torrc. */
  @Volatile
  var currentTorRc: String? = null
    private set

  @Volatile
  var isReady = false
    private set

  /** "xxxx.onion" while the onion service is published, null otherwise. */
  @Volatile
  var onionAddress: String? = null
    private set

  val isAlive: Boolean get() = loopThread?.isAlive == true

  private val torReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      when (intent.action) {
        TorService.ACTION_STATUS -> {
          torStatus = intent.getStringExtra(TorService.EXTRA_STATUS) ?: return
          Log.d(TAG, "Tor status: $torStatus")
        }

        TorService.ACTION_ERROR -> {
          val message = intent.getStringExtra(Intent.EXTRA_TEXT) ?: "unknown error"
          Log.e(TAG, "Tor error: $message")
          failed = true
          onError(message)
        }
      }
    }
  }

  private val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
      torService = (service as TorService.LocalBinder).service
    }

    // Called when the TorService process/instance died unexpectedly. The loop rebinds.
    override fun onServiceDisconnected(name: ComponentName?) {
      Log.w(TAG, "TorService disconnected")
      torService = null
      bound = false
    }
  }

  /**
   * @param serverPort the local Ktor port the onion service forwards to
   * @param torRc extra tor configuration, written to TorService's torrc file before every start.
   *   Do not set DataDirectory, CacheDirectory, ControlSocket or CookieAuthentication: TorService
   *   passes those on the command line, and the command line wins over the torrc.
   */
  fun start(torRc: String? = null, onionKey: String?) {
    stop()
    currentTorRc = torRc
    currentOnionKey = onionKey
    running = true

    loopThread = thread(name = "tor-loop") {
      // Registered BEFORE binding so the first STARTING/ON broadcasts are not missed
      ContextCompat.registerReceiver(
        context,
        torReceiver,
        IntentFilter().apply {
          addAction(TorService.ACTION_STATUS)
          addAction(TorService.ACTION_ERROR)
        },
        ContextCompat.RECEIVER_NOT_EXPORTED,
      )

      try {
        var boundSince = 0L

        while (running) {
          if (!bound) {
            boundSince = System.currentTimeMillis()
            failed = false
            bindTor()
            if (!bound) {
              Thread.sleep(RETRY_DELAY_MS)
              continue
            }
          }

          // Tor rejected its config or died while starting. Retrying fast cannot fix a bad
          // torrc, so back off for longer.
          if (failed) {
            Log.w(TAG, "Tor failed, restarting in ${ERROR_RETRY_DELAY_MS / 1000}s")
            unbindTor()
            Thread.sleep(ERROR_RETRY_DELAY_MS)
            continue
          }

          updateReady()

          // Tor never got ready in time: tear it down and start it again
          if (!isReady && System.currentTimeMillis() - boundSince > STARTUP_TIMEOUT_MS) {
            Log.w(TAG, "Tor did not become ready in ${STARTUP_TIMEOUT_MS / 1000}s, restarting")
            unbindTor()
            Thread.sleep(RETRY_DELAY_MS)
            continue
          }

          Thread.sleep(POLL_DELAY_MS)
        }
      } catch (_: InterruptedException) {
        // stop() interrupted the sleep, fall through to cleanup
      } finally {
        unbindTor()
        try {
          context.unregisterReceiver(torReceiver)
        } catch (_: Exception) {
        }
      }
    }
  }

  fun stop() {
    running = false
    loopThread?.interrupt()
    loopThread?.join()
    loopThread = null
  }

  /**
   * TorService reads this file once, when tor starts (`-f <dir>/torrc --ignore-missing-torrc`),
   * so it has to be written before every bind. With no custom torrc the file is deleted, so an
   * old one from a previous run cannot stay active.
   */
  private fun writeTorRc() {
    val file = TorService.getTorrc(context)
    val content = currentTorRc
    if (content.isNullOrBlank()) {
      file.delete()
    } else {
      file.writeText(content.trim() + "\n")
    }
  }

  private fun bindTor() {
    bound = try {
      writeTorRc()
      // BIND_AUTO_CREATE creates TorService, and TorService starts tor when it is created
      context.bindService(
        Intent(context, TorService::class.java),
        connection,
        Context.BIND_AUTO_CREATE,
      )
    } catch (e: Exception) {
      Log.e(TAG, "Starting tor failed", e)
      false
    }
  }

  // Unbinding the last client destroys TorService, which shuts tor (and the onion service) down
  private fun unbindTor() {
    if (bound) {
      try {
        context.unbindService(connection)
      } catch (e: Exception) {
        Log.w(TAG, "unbindService failed: ${e.message}")
      }
    }
    bound = false
    torService = null
    // Do not keep a stale ON from the previous run, it would make a fresh tor look ready
    torStatus = TorService.STATUS_OFF
    setReady(false)
  }

  /** Ready = status ON, control connection available and bootstrap at 100%. */
  private fun updateReady() {
    val control = torService?.torControlConnection
    if (control == null || torStatus != TorService.STATUS_ON) {
      setReady(false)
      return
    }

    try {
      val bootstrap = control.getInfo("status/bootstrap-phase") ?: ""
      if (!bootstrap.contains("PROGRESS=100")) {
        setReady(false)
        return
      }

      setReady(true)
      if (onionAddress == null) publishOnion(control)
    } catch (e: Exception) {
      // The control connection is not usable (yet, or any more)
      Log.w(TAG, "Tor control query failed: ${e.message}")
      setReady(false)
    }
  }

  /**
   * Forwards onion port 80 to the local Ktor server. Reuses the stored key if there is one, so
   * the address stays the same; otherwise tor generates a new ED25519-V3 key which is saved.
   * On failure onionAddress stays null and the next poll tries again.
   */
  private fun publishOnion(control: TorControlConnection) {
    try {
      val key = currentOnionKey ?: "NEW:ED25519-V3"
      val result = control.addOnion(key, mapOf(ONION_PORT to "127.0.0.1:8080"), emptyList())

      // Tor only returns the private key when it generated a new one
      result[TorControlCommands.HS_PRIVKEY]?.let(saveOnionKey)

      val address = result[TorControlCommands.HS_ADDRESS]
        ?: throw IllegalStateException("ADD_ONION returned no address")
      setOnionAddress("$address.onion")
      Log.i(TAG, "Onion service published: $address.onion -> 127.0.0.1:8080")
    } catch (e: Exception) {
      Log.e(TAG, "Publishing onion service failed, retrying", e)
    }
  }

  private fun setReady(ready: Boolean) {
    // Not ready = tor is down or restarting = the ephemeral onion service is gone with it
    if (!ready) setOnionAddress(null)
    if (isReady != ready) {
      isReady = ready
      onReadyChanged(ready)
    }
  }

  private fun setOnionAddress(address: String?) {
    if (onionAddress != address) {
      onionAddress = address
      onOnionAddressChanged(address)
    }
  }

  private companion object {
    const val TAG = "GatewayTor"
    const val ONION_PORT = 80
    const val POLL_DELAY_MS = 1_000L
    const val RETRY_DELAY_MS = 3_000L
    const val ERROR_RETRY_DELAY_MS = 30_000L
    const val STARTUP_TIMEOUT_MS = 180_000L
  }
}