package com.github.devlotfi.cipherbox.gateway

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Navigation {
  @Serializable
  data object Init : NavKey

  @Serializable
  data object Pin : NavKey

  @Serializable
  data object SetPin : NavKey

  @Serializable
  data class ConfirmPin(val pin: String) : NavKey

  @Serializable
  data object Dashboard : NavKey
}