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
    if (transportPacket !is TransportPacket.Ack) queue.enqueue(transportPacket)
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
  }

  private companion object {
    const val RETRY_INTERVAL_MS = 1000L
  }
}