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
  val onMqttSend: (data: String, topic: String) -> Unit
) {
  @Volatile
  private var loopThread: Thread? = null

  @Volatile
  private var running = false

  @Volatile
  private var currentTopic: String? = null

  private val queue = IdentifiedRequestQueue<TransportPacket>()

  suspend fun receive(transportPacket: TransportPacket) {
    if (transportPacket is TransportPacket.Ack) {
      val inboundGatewayPacket = transportPacket.toInboundGatewayPacket()
      val rawInboundGatewayPacket =
        JsonInstances.jsonInstance.encodeToString<InboundGatewayPacket>(inboundGatewayPacket)
      onBluetoothSend(rawInboundGatewayPacket)
    } else {
      queue.enqueue(transportPacket)
      if (currentTopic != null) {
        val ackTransportPacket = TransportPacket.Ack(requestId = transportPacket.requestId)
        val rawAckTransportPacket =
          JsonInstances.jsonInstance.encodeToString<TransportPacket>(ackTransportPacket)
        onMqttSend(currentTopic!!, rawAckTransportPacket)
      }
    }
  }

  suspend fun ack(outboundGatewayPacket: OutboundGatewayPacket.Ack) {
    queue.dequeue(outboundGatewayPacket.requestId)
  }

  fun start(topic: String) {
    currentTopic = topic
    stop()
    running = true

    loopThread = thread(name = "mqtt-receiver") {
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
    currentTopic = null
  }
}