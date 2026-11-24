package com.github.devlotfi.cipherbox.gateway.services

import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import com.github.devlotfi.cipherbox.shared.transport.toTransportPacket
import com.github.devlotfi.cipherbox.shared.utils.IdentifiedRequestQueue
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class GatewayMqttSender(
  val onMqttSend: (topic: String, data: String) -> Unit
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  private val queue = IdentifiedRequestQueue<OutboundGatewayPacket.DataMqtt>()

  suspend fun enqueue(transportPacket: OutboundGatewayPacket.DataMqtt) {
    queue.enqueue(transportPacket)
  }

  fun start(topic: String) {
    stop()
    running = true

    loopThread = thread(name = "mqtt-sender") {
      while (running) {
        try {
          val outboundGatewayPacket = runBlocking { queue.dequeue() }

          if (outboundGatewayPacket != null) {
            val transportPacket = outboundGatewayPacket.toTransportPacket(topic)
            val rawTransportPacket =
              JsonInstances.jsonInstance.encodeToString<TransportPacket>(transportPacket)
            onMqttSend(topic, rawTransportPacket)
          }

          Thread.sleep(1000)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun stop() {
    running = false
    loopThread?.join()
    loopThread = null
  }
}