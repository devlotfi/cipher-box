package com.github.devlotfi.cipherbox.client.data.message

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.github.devlotfi.cipherbox.shared.transport.message.MessageData
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
  indices = [Index(value = ["fromUserId", "messageId"], unique = true)]
)
@Serializable
data class MessageEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,                                   // local ordering only
  val verified: Boolean,
  val messageId: String = UUID.randomUUID().toString(), // dedup, sent inside the payload
  val fromUserId: String,
  val toUserId: String,
  val data: MessageData
)