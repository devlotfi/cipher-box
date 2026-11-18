package com.github.devlotfi.cipherbox.shared.provider

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.shared.data.LoginDb
import com.github.devlotfi.cipherbox.shared.utils.DbManager

class LoginDbProviderViewModel : ViewModel() {
  val loginDbManager = DbManager(
    databaseClass = LoginDb::class,
    databaseName = "login.db"
  )

  @SuppressLint("EmptySuperCall")
  override fun onCleared() {
    loginDbManager.close()
    super.onCleared()
  }
}

val LocalLoginDb = compositionLocalOf<LoginDbProviderViewModel>() {
  error("Not init")
}

@Composable
fun LoginDbProvider(
  viewModel: LoginDbProviderViewModel = viewModel(),
  content: @Composable () -> Unit,
) {
  return CompositionLocalProvider(
    LocalLoginDb provides viewModel
  ) {
    content()
  }
}