package com.github.devlotfi.cipherbox.shared.global

import kotlinx.serialization.json.Json

object JsonInstances {
  val jsonInstance = Json {
    ignoreUnknownKeys = true
  }
  val discriminatorJsonInstance = Json {
    classDiscriminator = "type"
    ignoreUnknownKeys = true
  }
}