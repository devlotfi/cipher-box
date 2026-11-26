package com.github.devlotfi.cipherbox.client.data

import androidx.room3.ColumnTypeConverter
import com.github.devlotfi.cipherbox.shared.global.JsonInstances
import com.github.devlotfi.cipherbox.shared.transport.message.MessageData
import org.example.contact.SequencedEncryptionKeyPair
import org.example.contact.SequencedEncryptionPublicKey

class SqlCipherDbConverters {
  @ColumnTypeConverter
  fun fromPublicKey(value: SequencedEncryptionPublicKey): String =
    JsonInstances.jsonInstance.encodeToString(SequencedEncryptionPublicKey.serializer(), value)

  @ColumnTypeConverter
  fun toPublicKey(value: String): SequencedEncryptionPublicKey =
    JsonInstances.jsonInstance.decodeFromString(SequencedEncryptionPublicKey.serializer(), value)

  @ColumnTypeConverter
  fun fromKeyPair(value: SequencedEncryptionKeyPair): String =
    JsonInstances.jsonInstance.encodeToString(SequencedEncryptionKeyPair.serializer(), value)

  @ColumnTypeConverter
  fun toKeyPair(value: String): SequencedEncryptionKeyPair =
    JsonInstances.jsonInstance.decodeFromString(SequencedEncryptionKeyPair.serializer(), value)

  @ColumnTypeConverter
  fun fromPayload(value: MessageData): String =
    JsonInstances.jsonInstance.encodeToString(MessageData.serializer(), value)

  @ColumnTypeConverter
  fun toPayload(value: String): MessageData =
    JsonInstances.jsonInstance.decodeFromString(MessageData.serializer(), value)
}
