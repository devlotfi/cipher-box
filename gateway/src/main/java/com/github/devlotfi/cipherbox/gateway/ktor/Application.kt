package com.github.devlotfi.cipherbox.gateway.ktor

import io.ktor.server.application.Application

fun Application.rootModule(
  sendViaBluetooth: (String) -> Boolean,
  gatewayKeyHash: String,
  debug: Boolean,
) {
  configureHttp(debug)
  configureRouting(
    sendViaBluetooth = sendViaBluetooth,
    gatewayKeyHash = gatewayKeyHash,
  )
}
