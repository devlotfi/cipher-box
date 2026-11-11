package com.github.devlotfi.cipherbox.shared.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

class KeyStoreAuthenticator(
  private val activity: FragmentActivity
) {

  private val executor =
    ContextCompat.getMainExecutor(activity)

  fun authenticate(
    cipher: Cipher,
    onSuccess: (Cipher) -> Unit,
    onError: (String) -> Unit
  ) {
    val prompt = BiometricPrompt(
      activity,
      executor,
      object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationSucceeded(
          result: BiometricPrompt.AuthenticationResult
        ) {
          val authenticatedCipher =
            result.cryptoObject?.cipher

          if (authenticatedCipher != null) {
            onSuccess(authenticatedCipher)
          } else {
            onError("Authenticated cipher unavailable")
          }
        }

        override fun onAuthenticationError(
          errorCode: Int,
          errString: CharSequence
        ) {
          onError(errString.toString())
        }

        override fun onAuthenticationFailed() {
          // The biometric did not match.
          // The prompt remains active.
        }
      }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle("CipherBox Authentication")
      .setSubtitle("Authenticate to access your encryption key")
      .setAllowedAuthenticators(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
          BiometricManager.Authenticators.DEVICE_CREDENTIAL
      )
      .build()

    prompt.authenticate(
      promptInfo,
      BiometricPrompt.CryptoObject(cipher)
    )
  }
}
