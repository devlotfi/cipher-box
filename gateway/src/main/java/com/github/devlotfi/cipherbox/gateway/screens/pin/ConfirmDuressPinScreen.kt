package com.github.devlotfi.cipherbox.gateway.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.devlotfi.cipherbox.gateway.NavigationKeys
import com.github.devlotfi.cipherbox.gateway.R
import com.github.devlotfi.cipherbox.gateway.data.appdatabase.appdata.AppDataEntity
import com.github.devlotfi.cipherbox.gateway.data.appdatabase.appdata.LoginData
import com.github.devlotfi.cipherbox.gateway.providers.LocalAppDatabase
import com.github.devlotfi.cipherbox.gateway.providers.LocalKeystore
import com.github.devlotfi.cipherbox.gateway.providers.LocalNavigation
import com.github.devlotfi.cipherbox.gateway.providers.LocalSqlCipherAppDatabase
import com.github.devlotfi.cipherbox.shared.crypto.AESGCMUtils
import com.github.devlotfi.cipherbox.shared.screens.GenericPinScreen
import com.github.devlotfi.cipherbox.shared.utils.KeyStoreAuthenticator
import com.github.devlotfi.composequery.QueryCache
import com.github.devlotfi.composequery.QueryClient
import com.github.devlotfi.composequery.rememberMutation
import com.github.devlotfi.composevstore.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.utils.ARGON2IDUtils
import com.github.devlotfi.cipherbox.shared.R as SharedR

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
) {
  val context = LocalContext.current
  val activity = context as FragmentActivity
  val localKeyStore = LocalKeystore.current
  val localAppDatabase = LocalAppDatabase.current
  val localSqlCipherAppDatabase = LocalSqlCipherAppDatabase.current
  val localNavigation = LocalNavigation.current
  val authenticator = remember {
    KeyStoreAuthenticator(activity)
  }

  val saveKeyGenResultMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("SAVE_KEY_GEN_RESULT")
    mutateFn { params: LoginData ->
      if (localAppDatabase.appDatabaseManager.db == null) throw Exception("Error")
      localAppDatabase.appDatabaseManager.db?.appDataDao()?.save(
        AppDataEntity(
          loginData = params
        )
      )
      localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.close()
      localAppDatabase.appDatabaseManager.close()
    }
    onSuccess {
      println("Save key gen success")
      localNavigation.backStack.clear()
      localNavigation.backStack.add(NavigationKeys.Pin)
    }
    onError = {
      it.printStackTrace()
      viewModel.error = "An error occured"
    }
  }

  val keyGenMutation = viewModel.queryClient.rememberMutation {
    mutateKey = listOf("KEY_GEN")
    mutateFn { params: Unit ->
      val sqlCipherPassword = AESGCMUtils.generateKey()
      println("password: ${sqlCipherPassword}")

      localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.delete(context)
      localSqlCipherAppDatabase.sqlCipherAppDatabaseManager.init(context, sqlCipherPassword)
      localAppDatabase.appDatabaseManager.delete(context)
      localAppDatabase.appDatabaseManager.init(context)

      val salt = ARGON2IDUtils.generateSalt()

      val pinAesKey = withContext(Dispatchers.Default) {
        val aesKey = ARGON2IDUtils.deriveKey(pin, salt)
        return@withContext aesKey
      }

      val pinEncryptionResult = AESGCMUtils.encrypt(sqlCipherPassword, pinAesKey)
      val cipher = localKeyStore.sqlCipherDatabaseKey.createEncryptionCipher()
      authenticator.authenticate(
        cipher = cipher,
        onSuccess = { authenticatedCipher ->
          try {
            val encrypted = localKeyStore.sqlCipherDatabaseKey.encrypt(
              pinEncryptionResult.ciphertext,
              authenticatedCipher
            )
            val loginData = LoginData(
              duressPin = duressPin,
              aesGcmData = LoginData.AesGcmData(
                ciphertext = encrypted.ciphertext,
                pinIv = pinEncryptionResult.iv,
                ketStoreIv = encrypted.iv
              ),
              argon2IdData = LoginData.Argon2IdData(
                salt = salt
              )
            )
            saveKeyGenResultMutation.mutate(loginData)
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
    logo = SharedR.drawable.cipher_box_gateway_logo,
    appName = stringResource(R.string.app_name),
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
          com.github.devlotfi.cipherbox.shared.icons.outlined.delete_forever,
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