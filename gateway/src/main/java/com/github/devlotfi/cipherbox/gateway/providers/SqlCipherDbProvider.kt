package com.github.devlotfi.cipherbox.gateway.providers

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.data.SqlCipherDb
import com.github.devlotfi.cipherbox.shared.utils.SqlCipherDbManager

class SqlCipherDbProviderViewModel : ViewModel() {
  val sqlCipherDbManager = SqlCipherDbManager(
    databaseClass = SqlCipherDb::class,
    databaseName = "sqlcipher-app.db"
  )

  @SuppressLint("EmptySuperCall")
  override fun onCleared() {
    sqlCipherDbManager.close()
    super.onCleared()
  }
}

val LocalSqlCipherDb = compositionLocalOf<SqlCipherDbProviderViewModel>() {
  error("Not init")
}

@Composable
fun SqlCipherDbProvider(
  viewModel: SqlCipherDbProviderViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalSqlCipherDb provides viewModel
  ) {
    content()
  }
}