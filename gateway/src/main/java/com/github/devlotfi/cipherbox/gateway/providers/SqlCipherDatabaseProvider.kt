package com.github.devlotfi.cipherbox.gateway.providers

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.data.GatewayDatabase
import com.github.devlotfi.cipherbox.shared.utils.SqlCipherDatabaseManager

class SqlCipherDatabaseViewModel : ViewModel() {
  val sqlCipherDatabaseManager = SqlCipherDatabaseManager(
    databaseClass = GatewayDatabase::class,
    databaseName = "gateway.db"
  )

  @SuppressLint("EmptySuperCall")
  override fun onCleared() {
    sqlCipherDatabaseManager.close()
    super.onCleared()
  }
}

val LocalSqlCipherDatabase = compositionLocalOf<SqlCipherDatabaseViewModel>() {
  error("Not init")
}

@Composable
fun SqlCipherDatabaseProvider(
  viewModel: SqlCipherDatabaseViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalSqlCipherDatabase provides viewModel
  ) {
    content()
  }
}