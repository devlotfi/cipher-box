package com.github.devlotfi.cipherbox.shared.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

class KeyStoreManager(
  val keyAlias: String
) {
  private val keystoreProvider = "AndroidKeyStore"

  private fun getKeyStore(): KeyStore {
    return KeyStore.getInstance(keystoreProvider).apply {
      load(null)
    }
  }

  fun getOrCreateKey(): SecretKey {
    val keyStore = getKeyStore()

    val existingKey = keyStore.getKey(keyAlias, null)

    if (existingKey is SecretKey) {
      return existingKey
    }

    val keyGenerator = KeyGenerator.getInstance(
      KeyProperties.KEY_ALGORITHM_AES,
      keystoreProvider
    )

    val spec = KeyGenParameterSpec.Builder(
      keyAlias,
      KeyProperties.PURPOSE_ENCRYPT or
        KeyProperties.PURPOSE_DECRYPT
    )
      .setKeySize(256)
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(
        KeyProperties.ENCRYPTION_PADDING_NONE
      )
      .setRandomizedEncryptionRequired(true)
      .setUserAuthenticationRequired(true)
      .build()

    keyGenerator.init(spec)

    return keyGenerator.generateKey()
  }

  fun exists(): Boolean {
    return getKeyStore().containsAlias(keyAlias)
  }

  fun delete() {
    getKeyStore().deleteEntry(keyAlias)
  }

  data class EncryptedData(
    val iv: String,
    val ciphertext: String
  )

  fun encrypt(data: String): EncryptedData {
    val key = getOrCreateKey()
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val ciphertext = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    return EncryptedData(
      iv = Base64.encode(cipher.iv),
      ciphertext = Base64.encode(ciphertext)
    )
  }

  fun decrypt(ciphertext: String, iv: String): String {
    val iv = Base64.decode(iv)
    val ciphertext = Base64.decode(ciphertext)
    val key = getOrCreateKey()
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
      Cipher.DECRYPT_MODE,
      key,
      GCMParameterSpec(128, iv)
    )
    val decrypted = cipher.doFinal(ciphertext)
    return String(decrypted, Charsets.UTF_8)
  }
}
