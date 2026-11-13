package com.github.devlotfi.cipherbox.gateway

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface NavigationKeys {
  @Serializable
  data object Init : NavKey

  @Serializable
  data object DeviceNotSecure : NavKey

  @Serializable
  data object Welcome : NavKey

  @Serializable
  data object Pin : NavKey

  @Serializable
  data object SetPin : NavKey

  @Serializable
  data class ConfirmPin(val pin: String) : NavKey

  @Serializable
  data class SetDuressPin(val pin: String) : NavKey

  @Serializable
  data class ConfirmDuressPin(val pin: String, val duressPin: String) : NavKey

  @Serializable
  data object Dashboard : NavKey
}