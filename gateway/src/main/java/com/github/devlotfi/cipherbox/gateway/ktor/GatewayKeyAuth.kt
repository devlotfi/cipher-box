package com.github.devlotfi.cipherbox.gateway.ktor

import com.github.devlotfi.cipherbox.shared.crypto.SHAUtils
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall

fun ApplicationCall.hasValidApiKey(
  gatewayKeyHash: String
): Boolean {
  val authorization = request.headers[HttpHeaders.Authorization]
    ?: return false

  val apiKey = authorization
    .takeIf { it.startsWith("Bearer ", ignoreCase = true) }
    ?.substringAfter("Bearer ")
    ?.trim()
    ?: return false

  return apiKey.isNotBlank() && runCatching {
    SHAUtils.verify(apiKey, gatewayKeyHash)
  }.getOrDefault(false)
}

