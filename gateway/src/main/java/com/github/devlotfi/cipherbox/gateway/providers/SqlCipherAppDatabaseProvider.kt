package com.github.devlotfi.cipherbox.gateway.providers

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.data.sqlcipherappdatabase.SqlCipherAppDatabase
import com.github.devlotfi.cipherbox.shared.utils.SqlCipherDatabaseManager

class SqlCipherAppDatabaseViewModel : ViewModel() {
  val sqlCipherAppDatabaseManager = SqlCipherDatabaseManager(
    databaseClass = SqlCipherAppDatabase::class,
    databaseName = "sqlcipher-app.db"
  )

  @SuppressLint("EmptySuperCall")
  override fun onCleared() {
    sqlCipherAppDatabaseManager.close()
    super.onCleared()
  }
}

val LocalSqlCipherAppDatabase = compositionLocalOf<SqlCipherAppDatabaseViewModel>() {
  error("Not init")
}

@Composable
fun SqlCipherAppDatabaseProvider(
  viewModel: SqlCipherAppDatabaseViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalSqlCipherAppDatabase provides viewModel
  ) {
    content()
  }
}