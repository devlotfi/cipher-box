package com.github.devlotfi.cipherbox.gateway.ktor

import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer

fun configureServer(
  port: Int,
  sendViaBluetooth: (String) -> Boolean,
  debug: Boolean = false,
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
  return embeddedServer(
    factory = CIO,
    port = port,
    host = "0.0.0.0",
    module = {
      rootModule(
        sendViaBluetooth = sendViaBluetooth,
        debug = debug
      )
    }
  )
}
