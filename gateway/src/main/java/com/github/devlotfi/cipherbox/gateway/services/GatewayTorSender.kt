package com.github.devlotfi.cipherbox.gateway.services

import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.utils.IdentifiedRequestQueue
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class GatewayTorSender(
  val onBluetoothSend: (String) -> Unit
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  private val queue = IdentifiedRequestQueue<OutboundGatewayPacket.DataTor>()

  suspend fun enqueue(transportPacket: OutboundGatewayPacket.DataTor) {
    queue.enqueue(transportPacket)
  }

  fun start() {
    stop()
    running = true

    loopThread = thread(name = "tor-sender") {
      while (running) {
        val outboundGatewayPacket = runBlocking { queue.dequeue() }


        Thread.sleep(1000)
      }
    }
  }

  fun stop() {
    running = false
    loopThread?.join()
    loopThread = null
  }
}