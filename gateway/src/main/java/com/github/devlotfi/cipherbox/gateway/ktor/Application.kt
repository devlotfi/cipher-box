package com.github.devlotfi.cipherbox.gateway.ktor

import com.github.devlotfi.cipherbox.shared.transport.packet.TransportPacket
import io.ktor.server.application.Application

fun Application.rootModule(
  onTransportPacket: (TransportPacket.DataTor) -> Unit,
  debug: Boolean,
) {
  configureHttp(debug)
  configureRouting(
    onTransportPacket = onTransportPacket,
  )
}
