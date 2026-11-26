package com.github.devlotfi.cipherbox.client.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.github.devlotfi.cipherbox.client.data.contact.ContactDao
import com.github.devlotfi.cipherbox.client.data.contact.ContactEntity
import com.github.devlotfi.cipherbox.client.data.message.MessageDao
import com.github.devlotfi.cipherbox.client.data.message.MessageEntity
import com.github.devlotfi.cipherbox.client.data.user.UserDao
import com.github.devlotfi.cipherbox.client.data.user.UserEntity

@Database(
  version = 1,
  exportSchema = false,
  entities = [
    UserEntity::class,
    ContactEntity::class,
    MessageEntity::class
  ],
)
@ColumnTypeConverters(SqlCipherDbConverters::class)
abstract class SqlCipherDb : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun contactDao(): ContactDao
  abstract fun messageDao(): MessageDao
}