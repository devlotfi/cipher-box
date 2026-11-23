package com.github.devlotfi.cipherbox.gateway.ktor

import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer

fun configureServer(
  onTransportPacket: (TransportPacket.DataTor) -> Unit,
  debug: Boolean = false,
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
  return embeddedServer(
    factory = CIO,
    port = 8080,
    host = "0.0.0.0",
    module = {
      rootModule(
        onTransportPacket = onTransportPacket,
        debug = debug
      )
    }
  )
}
