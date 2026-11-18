package com.github.devlotfi.cipherbox.shared.transport

import androidx.annotation.Keep
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

class SerialTinyProtoLayer(
  val mtu: Int = TransportGlobals.MTU,
  val window: Int = TransportGlobals.WINDOW,
  private val onFrame: (addr: Int, data: ByteArray) -> Unit,
  private val transportWrite: (ByteArray) -> Unit,
) : AutoCloseable {
  companion object {
    private const val SEND_TIMEOUT_MS = 5_000L
  }

  private val lock = ReentrantReadWriteLock()

  @Volatile
  private var running = true
  private var handle: Long = nativeCreate(mtu, window)
  private val sendQueue = LinkedBlockingQueue<ByteArray>()

  private val senderThread = thread(name = "tinyproto-sender") {
    while (running) {
      val frame = sendQueue.poll(100, TimeUnit.MILLISECONDS) ?: continue
      if (!writeWithRetry(frame)) {
        println("dropped a ${frame.size}-byte frame after ${SEND_TIMEOUT_MS}ms")
      }
    }
  }

  private val txPump = thread(name = "tinyproto-tx") {
    while (running) {
      val chunk = lock.read { if (handle != 0L) nativePollTx(handle) else null }
      if (chunk != null) transportWrite(chunk) else Thread.sleep(2)
    }
  }

  /** Enqueue a frame and return immediately. Frames are sent in order. */
  fun send(data: ByteArray) {
    sendQueue.put(data)
  }

  /** Pushes one frame into the protocol, retrying while its window is full. */
  private fun writeWithRetry(frame: ByteArray): Boolean {
    val deadline = System.nanoTime() + SEND_TIMEOUT_MS * 1_000_000
    while (running && System.nanoTime() < deadline) {
      val rc = lock.read { if (handle != 0L) nativeSend(handle, frame) else -1 }
      if (rc >= 0) return true
      println("write returned $rc, retrying")
      Thread.sleep(2)
    }
    return false
  }

  fun onBytesReceived(data: ByteArray, length: Int = data.size) {
    lock.read { if (handle != 0L) nativeFeed(handle, data, length) }
  }

  override fun close() {
    running = false
    senderThread.join()
    txPump.join()
    lock.write {
      if (handle != 0L) {
        nativeDestroy(handle); handle = 0L
      }
    }
  }

  @Keep
  private fun onNativeFrame(addr: Int, data: ByteArray) = onFrame(addr, data)

  private external fun nativeCreate(mtu: Int, window: Int): Long
  private external fun nativeDestroy(handle: Long)
  private external fun nativeSend(handle: Long, data: ByteArray): Int
  private external fun nativeFeed(handle: Long, data: ByteArray, len: Int): Int
  private external fun nativePollTx(handle: Long): ByteArray?
}