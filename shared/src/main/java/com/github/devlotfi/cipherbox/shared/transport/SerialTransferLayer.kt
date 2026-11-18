package com.github.devlotfi.cipherbox.shared.transport

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

class SerialTransferLayer(
  val mtu: Int = TransportGlobals.MTU,
  val window: Int = TransportGlobals.WINDOW,
  private val transportWrite: (ByteArray) -> Unit,
  private val onTransferData: (String) -> Unit,
) : AutoCloseable {
  private companion object {
    const val STALE_AFTER_MS = 60_000L
  }

  private val receivePool = mutableMapOf<String, ReceivePoolEntry>()
  private val serialTinyProtoLayer = SerialTinyProtoLayer(
    mtu = mtu,
    window = window,
    onFrame = { _, data ->
      receiveFrame(data)
    },
    transportWrite = transportWrite
  )

  fun send(data: String) {
    val chunks = chunkMessage(mtu - 16, data)
    chunks.forEach {
      serialTinyProtoLayer.send(it.toByteArray(Charsets.UTF_8))
    }
  }

  fun onBytesReceived(data: ByteArray, length: Int = data.size) {
    serialTinyProtoLayer.onBytesReceived(data, length)
  }

  private fun receiveFrame(data: ByteArray) {
    val frame = try {
      Json.decodeFromString<ReceivePoolEntry.SerialTinyProtoLayerFrame>(data.toString(Charsets.UTF_8))
    } catch (e: Exception) {
      println("dropping malformed frame: $e")
      return
    }
    if (frame.lastFrameId < 0 || frame.frameId !in 0..frame.lastFrameId) {
      println("dropping out-of-range frame ${frame.frameId}/${frame.lastFrameId}")
      return
    }

    val completed: String? = synchronized(receivePool) {
      val now = System.currentTimeMillis()
      receivePool.values.removeAll { now - it.lastActivity > STALE_AFTER_MS }

      val entry = receivePool.getOrPut(frame.transferId) { ReceivePoolEntry(frame.lastFrameId) }
      entry.receiveFrame(frame)

      if (entry.isCompleted()) {
        receivePool.remove(frame.transferId)
        entry.buildCompletedTransfer()
      } else null
    }

    // Outside the lock: user code can take its time without blocking reassembly
    if (completed != null) onTransferData(completed)
  }

  override fun close() {
    serialTinyProtoLayer.close()
  }

  fun chunkMessage(maxSize: Int, data: String): List<String> {
    fun jsonSize(ch: String): Int {
      val c = ch[0]
      if (ch.length == 1 && c.code in 0x20..0x7E && c != '"' && c != '\\') return 1   // fast path: plain ASCII
      return Json.encodeToString(ch)
        .encodeToByteArray().size - 2                      // minus the surrounding quotes
    }

    val transferId = UUID.randomUUID().toString()

    // Everything except the data text, measured once with worst-case digits
    val overhead = Json.encodeToString(
      ReceivePoolEntry.SerialTinyProtoLayerFrame(transferId, Int.MAX_VALUE, Int.MAX_VALUE, "")
    ).encodeToByteArray().size
    val budget = maxSize - overhead
    require(budget > 0) { "maxSize is too small for the frame header" }

    val chunks = mutableListOf<String>()
    val current = StringBuilder()
    var used = 0

    var i = 0
    while (i < data.length) {
      val ch = String(Character.toChars(data.codePointAt(i)))
      i += ch.length

      val size = jsonSize(ch)
      if (size > budget) error("maxSize is too small to fit a single character")
      if (used + size > budget) {
        chunks.add(current.toString())
        current.setLength(0)
        used = 0
      }
      current.append(ch)
      used += size
    }
    if (current.isNotEmpty() || chunks.isEmpty()) chunks.add(current.toString())

    val last = chunks.size - 1
    return chunks.mapIndexed { index, chunk ->
      Json.encodeToString(
        ReceivePoolEntry.SerialTinyProtoLayerFrame(
          transferId,
          index,
          last,
          chunk
        )
      )
    }
  }

  class ReceivePoolEntry(val lastFrameId: Int) {
    private val parts = mutableMapOf<Int, String>()

    @Volatile
    var lastActivity = System.currentTimeMillis()
      private set

    fun receiveFrame(frame: SerialTinyProtoLayerFrame) {
      if (frame.frameId in 0..lastFrameId) parts[frame.frameId] = frame.data
      lastActivity = System.currentTimeMillis()
    }

    fun isCompleted() = parts.size == lastFrameId + 1

    fun buildCompletedTransfer(): String = buildString {
      for (i in 0..lastFrameId) append(parts.getValue(i))
    }

    @Serializable
    data class SerialTinyProtoLayerFrame(
      val transferId: String,
      val frameId: Int,
      val lastFrameId: Int,
      val data: String,
    )
  }
}