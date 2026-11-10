package com.github.devlotfi.cipherbox.shared.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.useReaderConnection
import net.zetetic.database.sqlcipher.driver.SQLCipherDriver
import kotlin.reflect.KClass

class SqlCipherDatabaseManager<T : RoomDatabase>(
  private val databaseClass: KClass<T>,
  private val databaseName: String
) {
  var db by mutableStateOf<T?>(null)
    private set

  suspend fun init(context: Context, password: String): Boolean {
    close()

    return try {
      System.loadLibrary("sqlcipher")

      val appContext = context.applicationContext
      val databaseFile = appContext.getDatabasePath(databaseName)

      val driver = SQLCipherDriver(
        password.toByteArray(Charsets.UTF_8),
        null,
        null
      )

      val database = Room.databaseBuilder(
        appContext,
        databaseClass.java,
        databaseFile.absolutePath
      )
        .setDriver(driver)
        .build()

      try {
        database.useReaderConnection { connection ->
          connection.usePrepared("SELECT 1") { statement ->
            check(statement.step())
          }
        }

        db = database
        true
      } catch (e: Exception) {
        database.close()
        false
      }
    } catch (e: Exception) {
      false
    }
  }

  fun exists(context: Context): Boolean =
    context.applicationContext
      .getDatabasePath(databaseName)
      .exists()

  fun close() {
    db?.close()
    db = null
  }

  fun delete(context: Context): Boolean {
    close()

    return context.applicationContext
      .deleteDatabase(databaseName)
  }
}