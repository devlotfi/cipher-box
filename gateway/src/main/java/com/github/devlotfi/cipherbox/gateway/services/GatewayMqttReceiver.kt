package com.github.devlotfi.cipherbox.gateway.services

import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.packet.InboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import com.github.devlotfi.cipherbox.shared.transport.toInboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.utils.IdentifiedRequestQueue
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class GatewayMqttReceiver(
  val onBluetoothSend: (String) -> Unit,
  val onMqttSend: (topic: String, data: String) -> Unit
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  @Volatile
  private var currentTopic: String? = null

  private val queue = IdentifiedRequestQueue<TransportPacket>()

  suspend fun receive(transportPacket: TransportPacket) {
    val topic = currentTopic ?: return
    if (transportPacket is TransportPacket.Ack) {
      val inboundGatewayPacket = transportPacket.toInboundGatewayPacket()
      val rawInboundGatewayPacket =
        JsonInstances.jsonInstance.encodeToString<InboundGatewayPacket>(inboundGatewayPacket)
      onBluetoothSend(rawInboundGatewayPacket)
    } else {
      queue.enqueue(transportPacket)
      if (currentTopic != null) {
        val ackTransportPacket =
          TransportPacket.Ack(requestId = transportPacket.requestId)
        val rawAckTransportPacket =
          JsonInstances.jsonInstance.encodeToString<TransportPacket>(ackTransportPacket)
        onMqttSend(topic, rawAckTransportPacket)
      }
    }
  }

  suspend fun ack(outboundGatewayPacket: OutboundGatewayPacket.Ack) {
    queue.dequeue(outboundGatewayPacket.requestId)
  }

  fun start(topic: String) {
    stop()
    currentTopic = topic
    running = true

    loopThread = thread(name = "mqtt-receiver") {
      while (running) {
        try {
          queue.drainSignal()

          val transportPacket = runBlocking { queue.peek() }
          if (transportPacket != null) {
            val inboundGatewayPacket = when (transportPacket) {
              is TransportPacket.DataTor -> transportPacket.toInboundGatewayPacket()
              is TransportPacket.DataMqtt -> transportPacket.toInboundGatewayPacket()
              is TransportPacket.Ack -> {
                runBlocking { queue.dequeue(transportPacket.requestId) }
                continue
              }
            }
            val rawInboundGatewayPacket =
              JsonInstances.jsonInstance.encodeToString<InboundGatewayPacket>(inboundGatewayPacket)

            try {
              onBluetoothSend(rawInboundGatewayPacket)
            } catch (e: InterruptedException) {
              throw e // let the outer catch exit the loop
            } catch (e: Exception) {
              e.printStackTrace() // fall through to the wait below
            }
          }

          runBlocking { queue.awaitEnqueue(RETRY_INTERVAL_MS) }
        } catch (e: InterruptedException) {
          break
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun stop() {
    running = false
    loopThread?.let {
      it.interrupt() // otherwise join() waits out the remaining retry interval
      it.join()
    }
    loopThread = null
    currentTopic = null
  }

  private companion object {
    const val RETRY_INTERVAL_MS = 1000L
  }
}