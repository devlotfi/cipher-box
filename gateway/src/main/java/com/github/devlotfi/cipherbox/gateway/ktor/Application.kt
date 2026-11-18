package com.github.devlotfi.cipherbox.gateway.ktor

import io.ktor.server.application.Application

fun Application.rootModule(
  sendViaBluetooth: (String) -> Boolean,
  debug: Boolean,
) {
  configureHttp(debug)
  configureRouting(sendViaBluetooth)
}
