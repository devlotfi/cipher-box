package com.github.devlotfi.cipherbox.gateway.providers

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.data.appdatabase.AppDatabase
import com.github.devlotfi.cipherbox.shared.utils.DatabaseManager

class AppDatabaseViewModel : ViewModel() {
  val appDatabaseManager = DatabaseManager(
    databaseClass = AppDatabase::class,
    databaseName = "app.db"
  )

  @SuppressLint("EmptySuperCall")
  override fun onCleared() {
    appDatabaseManager.close()
    super.onCleared()
  }
}

val LocalAppDatabase = compositionLocalOf<AppDatabaseViewModel>() {
  error("Not init")
}

@Composable
fun AppDatabaseProvider(
  viewModel: AppDatabaseViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalAppDatabase provides viewModel
  ) {
    content()
  }
}