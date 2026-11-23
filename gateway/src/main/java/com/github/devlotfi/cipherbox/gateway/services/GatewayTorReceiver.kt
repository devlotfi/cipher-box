package com.github.devlotfi.cipherbox.gateway.services

import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.packet.InboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import com.github.devlotfi.cipherbox.shared.transport.toInboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.utils.IdentifiedRequestQueue
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class GatewayTorReceiver(
  val onBluetoothSend: (String) -> Unit,
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  private val queue = IdentifiedRequestQueue<TransportPacket>()

  suspend fun receive(transportPacket: TransportPacket) {
    if (transportPacket is TransportPacket.Ack) {
      return
    } else {
      queue.enqueue(transportPacket)
    }
  }

  suspend fun ack(outboundGatewayPacket: OutboundGatewayPacket.Ack) {
    queue.dequeue(outboundGatewayPacket.requestId)
  }

  fun start() {
    stop()
    running = true

    loopThread = thread(name = "tor-receiver") {
      while (running) {
        try {
          val transportPacket = runBlocking { queue.peek() }

          if (transportPacket != null) {

            val inboundGatewayPacket = when (transportPacket) {
              is TransportPacket.DataTor -> transportPacket.toInboundGatewayPacket()
              is TransportPacket.DataMqtt -> transportPacket.toInboundGatewayPacket()
              is TransportPacket.Ack -> return@thread
            }
            val rawInboundGatewayPacket =
              JsonInstances.jsonInstance.encodeToString<InboundGatewayPacket>(inboundGatewayPacket)
            onBluetoothSend(rawInboundGatewayPacket)
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }

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