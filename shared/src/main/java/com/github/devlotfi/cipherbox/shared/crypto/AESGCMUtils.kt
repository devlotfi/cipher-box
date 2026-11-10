package com.github.devlotfi.cipherbox.shared.crypto

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import kotlin.io.encoding.Base64

object AESGCMUtils {
  private val random = SecureRandom()
  private const val KEY_SIZE = 32
  private const val IV_SIZE = 12
  private const val TAG_SIZE = 128

  data class EncryptResult(
    val ciphertext: String,
    val iv: String
  )

  fun generateKey(): String {
    val key = ByteArray(KEY_SIZE)
    random.nextBytes(key)
    return Base64.encode(key)
  }

  // Encrypt a String
  fun encrypt(
    data: String,
    key: String
  ): EncryptResult {
    val messageBytes = data.toByteArray(StandardCharsets.UTF_8)
    val iv = ByteArray(IV_SIZE)
    random.nextBytes(iv)
    val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
    cipher.init(
      true,
      AEADParameters(
        KeyParameter(
          Base64.decode(key)
        ),
        TAG_SIZE,
        iv
      )
    )
    val output = ByteArray(cipher.getOutputSize(messageBytes.size))
    val length = cipher.processBytes(
      messageBytes,
      0,
      messageBytes.size,
      output,
      0
    )
    val finalLength = cipher.doFinal(output, length)
    val ciphertext = output.copyOf(length + finalLength)
    return EncryptResult(
      ciphertext = Base64.encode(ciphertext),
      iv = Base64.encode(iv)
    )
  }

  // Decrypt a String
  fun decrypt(
    ciphertext: String,
    iv: String,
    key: String
  ): String {

    val ciphertextBytes = Base64.decode(ciphertext)
    val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
    cipher.init(
      false,
      AEADParameters(
        KeyParameter(
          Base64.decode(key)
        ),
        TAG_SIZE,
        Base64.decode(iv)
      )
    )
    val output = ByteArray(cipher.getOutputSize(ciphertextBytes.size))
    val length = cipher.processBytes(
      ciphertextBytes,
      0,
      ciphertextBytes.size,
      output,
      0
    )
    val finalLength = cipher.doFinal(output, length)
    val decryptedBytes = output.copyOf(length + finalLength)
    return String(
      decryptedBytes,
      StandardCharsets.UTF_8
    )
  }
}