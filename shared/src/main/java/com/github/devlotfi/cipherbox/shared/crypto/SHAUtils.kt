package com.github.devlotfi.cipherbox.shared.crypto

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.io.encoding.Base64

object SHAUtils {

  private val random = SecureRandom()

  private const val KEY_SIZE = 32
  private const val SHA_ALGORITHM = "SHA-256"

  // Generate a random 256-bit key
  fun generateKey(): String {
    val key = ByteArray(KEY_SIZE)
    random.nextBytes(key)

    return Base64.UrlSafe
      .withPadding(Base64.PaddingOption.ABSENT)
      .encode(key)
  }

  // Hash a String using SHA-256
  fun hash(data: String): String {
    val digest = MessageDigest.getInstance(SHA_ALGORITHM)
      .digest(data.toByteArray(StandardCharsets.UTF_8))

    return Base64.encode(digest)
  }

  // Verify a String against its SHA-256 hash
  fun verify(
    data: String,
    hash: String
  ): Boolean {
    return MessageDigest.isEqual(
      MessageDigest.getInstance(SHA_ALGORITHM)
        .digest(data.toByteArray(StandardCharsets.UTF_8)),
      Base64.decode(hash)
    )
  }
}
