package com.github.devlotfi.cipherbox.gateway.ktor

import io.ktor.server.application.Application

fun Application.rootModule(
  debug: Boolean
) {
  configureHttp(debug)
  configureRouting()
}
