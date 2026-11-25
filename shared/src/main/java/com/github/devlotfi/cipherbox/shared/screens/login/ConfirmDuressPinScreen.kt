package com.github.devlotfi.cipherbox.shared.screens.login

import android.content.pm.ApplicationInfo
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.devlotfi.cipherbox.shared.crypto.AESGCMUtils
import com.github.devlotfi.cipherbox.shared.data.logindata.LoginDataEntity
import com.github.devlotfi.cipherbox.shared.global.KeyStoreKeyAliases
import com.github.devlotfi.cipherbox.shared.icons.outlined.delete_forever
import com.github.devlotfi.cipherbox.shared.navigation.LoginNavigationKeys
import com.github.devlotfi.cipherbox.shared.provider.LocalLoginDb
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreAuthenticator
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreManager
import com.github.devlotfi.composequery.QueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberMutation
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.utils.ARGON2IDUtils

class ConfirmDuressPinScreenViewModel : ViewModel() {
  val dataStore = DataStore()
  val queryCache = QueryCache()
  val queryClient = QueryClient(viewModelScope, queryCache)
  var error by mutableStateOf<String?>(null)
}

@Composable
fun ConfirmDuressPinScreen(
  pin: String,
  duressPin: String,
  modifier: Modifier = Modifier,
  viewModel: ConfirmDuressPinScreenViewModel = viewModel(),
  backStack: NavBackStack<NavKey>,
  setupSqlCipherDb: suspend (password: String) -> Unit,
  closeSqlCipherDb: suspend () -> Unit,
  appName: String,
  @DrawableRes logo: Int
) {
  val context = LocalContext.current
  val activity = context as FragmentActivity
  val localLoginDb = LocalLoginDb.current
  val authenticator = remember { KeyStoreAuthenticator(activity) }
  val sqlCipherDbKey = remember { KeyStoreManager(KeyStoreKeyAliases.SQLCIPHER_DB_KEY) }

  val saveKeyGenResultMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("SAVE_KEY_GEN_RESULT")
    mutateFn { params: LoginDataEntity ->
      val loginDb = requireNotNull(localLoginDb.loginDbManager.db)
      loginDb.loginDataDao().save(
        params
      )
      closeSqlCipherDb()
      localLoginDb.loginDbManager.close()
    }
    onSuccess {
      println("Save key gen success")
      backStack.clear()
      backStack.add(LoginNavigationKeys.Pin)
    }
    onError = {
      it.printStackTrace()
      viewModel.error = "An error occured"
    }
  }

  val keyGenMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("KEY_GEN")
    mutateFn { params: Unit ->
      val sqlCipherPassword =
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
          "test"
        } else AESGCMUtils.generateKey()
      println("password: ${sqlCipherPassword}")

      setupSqlCipherDb(sqlCipherPassword)

      localLoginDb.loginDbManager.delete(context)
      localLoginDb.loginDbManager.init(context)

      val salt = ARGON2IDUtils.generateSalt()

      val pinAesKey = withContext(Dispatchers.Default) {
        val aesKey = ARGON2IDUtils.deriveKey(pin, salt)
        return@withContext aesKey
      }

      val pinEncryptionResult = AESGCMUtils.encrypt(sqlCipherPassword, pinAesKey)
      val cipher = sqlCipherDbKey.createEncryptionCipher()
      authenticator.authenticate(
        cipher = cipher,
        onSuccess = { authenticatedCipher ->
          try {
            val encrypted = sqlCipherDbKey.encrypt(
              pinEncryptionResult.ciphertext,
              authenticatedCipher
            )
            val loginDataEntity = LoginDataEntity(
              duressPin = duressPin,
              aesGcmData = LoginDataEntity.AesGcmData(
                ciphertext = encrypted.ciphertext,
                pinIv = pinEncryptionResult.iv,
                ketStoreIv = encrypted.iv
              ),
              argon2IdData = LoginDataEntity.Argon2IdData(
                salt = salt
              )
            )
            saveKeyGenResultMutation.mutate(loginDataEntity)
          } catch (e: Exception) {
            e.printStackTrace()
            viewModel.error = "An error occured"
          }
        },
        onError = { error ->
          println(error)
          viewModel.error = "An error occured"
        }
      )
    }
    onSuccess {
      println("Key gen success")
    }
    onError = {
      it.printStackTrace()
      viewModel.error = "An error occured"
    }
  }

  GenericPinScreen(
    modifier = modifier,
    dataStore = viewModel.dataStore,
    logo = logo,
    appName = appName,
    title = "Confirm Duress PIN",
    titleIcon = {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          delete_forever,
          contentDescription = null,
          modifier = Modifier.size(30.dp),
          tint = MaterialTheme.colorScheme.onError
        )
      }
    },
    error = viewModel.error,
    clearError = {
      viewModel.error = null
    },
    loading = keyGenMutation.loading || saveKeyGenResultMutation.loading,
    onPin = {
      if (it != duressPin) {
        viewModel.error = "Duress pins dont match"
      } else {
        keyGenMutation.mutate(Unit)
      }
    }
  )
}