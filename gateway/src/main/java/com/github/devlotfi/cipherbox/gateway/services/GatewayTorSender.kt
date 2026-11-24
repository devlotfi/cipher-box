package com.github.devlotfi.cipherbox.gateway.services

import com.github.devlotfi.cipherbox.openapi.apis.DefaultApi
import com.github.devlotfi.cipherbox.openapi.models.SendBody
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.packet.InboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.OutboundGatewayPacket
import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import com.github.devlotfi.cipherbox.shared.transport.toTransportPacket
import com.github.devlotfi.cipherbox.shared.utils.IdentifiedRequestQueue
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class GatewayTorSender(
  val onBluetoothSend: (String) -> Unit
) {
  private companion object {
    const val REQUEST_TIMEOUT_MS = 20_000L
  }

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
      val engine = OkHttp.create {
        proxy = ProxyBuilder.socks(host = "127.0.0.1", port = 9050)
      }

      try {
        runBlocking {
          while (running && isActive) {
            val packet = queue.dequeueAwait()
            launch { handle(engine, packet) }
          }
        }
      } catch (e: InterruptedException) {
        // stop() was called
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        engine.close()
      }
    }
  }

  private suspend fun handle(
    engine: HttpClientEngine,
    outboundGatewayPacket: OutboundGatewayPacket.DataTor
  ) {
    try {
      val api = DefaultApi(
        baseUrl = "http://${outboundGatewayPacket.url}",
        httpClientEngine = engine,
        httpClientConfig = { config ->
          config.install(ContentNegotiation) { json(JsonInstances.jsonInstance) }
          config.install(HttpTimeout) { requestTimeoutMillis = REQUEST_TIMEOUT_MS }
        },
      )
      val transportPacket = outboundGatewayPacket.toTransportPacket()
      val rawTransportPacket =
        JsonInstances.jsonInstance.encodeToString<TransportPacket>(transportPacket)
      val res = api.send(
        SendBody(
          rawTransportPacket = rawTransportPacket
        )
      )
      if (!res.success) throw Exception("Request failed")

      val inboundGatewayPacket = InboundGatewayPacket.Ack(
        requestId = outboundGatewayPacket.requestId,
        success = true
      )
      val rawInboundGatewayPacket =
        JsonInstances.jsonInstance.encodeToString<InboundGatewayPacket>(inboundGatewayPacket)
      onBluetoothSend(rawInboundGatewayPacket)
    } catch (e: CancellationException) {
      throw e // stop() cancels in-flight requests; never swallow this
    } catch (e: Exception) {
      e.printStackTrace()
      val inboundGatewayPacket = InboundGatewayPacket.Ack(
        requestId = outboundGatewayPacket.requestId,
        success = false
      )
      val rawInboundGatewayPacket =
        JsonInstances.jsonInstance.encodeToString<InboundGatewayPacket>(inboundGatewayPacket)
      onBluetoothSend(rawInboundGatewayPacket)
    }
  }

  fun stop() {
    running = false
    loopThread?.let {
      it.interrupt()
      it.join()
    }
    loopThread = null
  }
}