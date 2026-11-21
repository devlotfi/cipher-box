package com.github.devlotfi.cipherbox.gateway.ktor

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.openapi.registerBearerAuthSecurityScheme
import io.ktor.server.routing.routing

fun Application.configureHttp(
  debug: Boolean
) {
  registerBearerAuthSecurityScheme(
    name = "GatewayKeyAuth",
    description = "Gateway key",
    bearerFormat = "API Key"
  )
  install(ContentNegotiation) {
    json()
  }
  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)
    allowHeader(HttpHeaders.Authorization)
    anyHost()
  }
  if (debug) {
    routing {
      swaggerUI(path = "openapi") {
        info = OpenApiInfo("Gateway API", "1.0.0")
      }
    }
  }
}
