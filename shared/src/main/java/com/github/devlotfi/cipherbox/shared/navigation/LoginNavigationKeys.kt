package com.github.devlotfi.cipherbox.shared.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface LoginNavigationKeys {
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
}